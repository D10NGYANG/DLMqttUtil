package com.d10ng.mqtt

import android.content.Context
import com.d10ng.mqtt.bean.MqttClientOptions
import com.d10ng.mqtt.constant.MqttConnectStatus
import com.d10ng.mqtt.util.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

internal class MqttWorker : IMqtt {

    companion object {
        /** 单例 */
        val instance: MqttWorker by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            MqttWorker()
        }
    }

    /** MQTT客户端 */
    private var mClient: MqttAndroidClient? = null

    /** MQTT服务器连接配置 */
    private var mOptions: MqttClientOptions? = null

    /** 上下文 */
    private var weakContext: WeakReference<Context> = WeakReference(null)

    /** 发布消息自增ID */
    private val publishMessageIdAtomic = AtomicInteger(0)

    /** 发布消息结果写入协程域 */
    private val publishMessageResultScope = CoroutineScope(Dispatchers.IO)

    /** 发布消息结果流 */
    private val publishMessageResultIdFlow = MutableSharedFlow<Int>(extraBufferCapacity = 100)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            // 监听连接状态，如果连接成功则执行订阅主题
            MqttManager.getConnectStatusFlow().collect { status ->
                if (status == MqttConnectStatus.CONNECTED) {
                    mClient?.subscribeTopic()
                }
            }
        }
    }

    override fun connect(context: Context, options: MqttClientOptions) {
        weakContext = WeakReference(context)
        mOptions = options
        doConnect()
    }

    override fun disconnect() {
        try {
            mClient?.apply {
                if (isConnected) disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mClient?.unregisterResources()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            mClient?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mClient = null
        mOptions = null
        // 更改MQTT状态
        MqttManager.changeConnectStatus(MqttConnectStatus.DISCONNECTED)
    }

    @OptIn(FlowPreview::class)
    override suspend fun publish(topic: String, message: String): Boolean =
        withContext(Dispatchers.IO) {
            LogUtil.d("MQTT push message prepare, topic=$topic, message=$message")
            // 如果客户端不存在或者已断开连接，则不再执行推送
            if (mClient == null || !mClient!!.isConnected) {
                LogUtil.e("MQTT push message failed, client is null or disconnected!")
                return@withContext false
            }
            // 创建新的消息ID
            val messageId = publishMessageIdAtomic.incrementAndGet()
            // 封装消息
            val mqttMessage = MqttMessage(message.toByteArray()).apply {
                id = messageId
            }
            // 启动结果监听
            val waitJob = async {
                try {
                    publishMessageResultIdFlow.takeWhile { it == messageId }.take(1)
                        .timeout(5.seconds).first()
                } catch (e: Exception) {
                    e.printStackTrace()
                    LogUtil.e("MQTT push message failed, timeout, $e!")
                    null
                }
            }
            // 发布消息
            try {
                mClient!!.publish(topic, mqttMessage)
                waitJob.await() != null
            } catch (e: Exception) {
                e.printStackTrace()
                waitJob.cancel()
                LogUtil.e("MQTT push message failed, $e!")
                false
            }
        }

    /**
     * 执行连接
     */
    private fun doConnect() {
        // 如果已连接或者上一个连接任务正在连接中，则不再重复连接
        if (MqttManager.getCurrentConnectStatus() != MqttConnectStatus.DISCONNECTED) return
        // 如果context不存在则不连接
        val context = weakContext.get() ?: return
        // 如果配置不存在则不连接
        val options = mOptions ?: return
        // 更改MQTT状态
        MqttManager.changeConnectStatus(MqttConnectStatus.CONNECTING)
        // 断开旧连接
        disconnect()
        LogUtil.i("MQTT start connect, options=$options")
        mOptions = options
        val clientId = "${options.clientId}_${System.currentTimeMillis()}_${(0..100).random()}"
        try {
            // 创建MQTT客户端
            mClient = MqttAndroidClient(context, options.host, clientId).apply {
                // 设置MQTT连接配置
                val connectOptions = options.toMqttConnectOptions()
                // 设置回调信息监听
                setCallback(mCallBack)
                // 建立连接
                connect(connectOptions, null, mConnectListener)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 回调信息监听
     */
    private val mCallBack = object : MqttCallbackExtended {
        override fun connectionLost(cause: Throwable?) {
            // 连接丢失
            LogUtil.e("MQTT connection lost, $cause")
            // 自动重连，修改状态为连接中
            MqttManager.changeConnectStatus(MqttConnectStatus.CONNECTING)
        }

        override fun messageArrived(topic: String?, message: MqttMessage?) {
            // 接收到的消息
            message ?: return
            topic ?: return
            LogUtil.i("MQTT msg receive, topic=$topic, message=$message")
            MqttManager.receiveMessage(topic, message.toString())
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
            token ?: return
            // publish消息完成
            LogUtil.i("MQTT push message success! id=${token.message.id}")
            publishMessageResultScope.launch { publishMessageResultIdFlow.emit(token.message.id) }
        }

        override fun connectComplete(reconnect: Boolean, serverURI: String?) {
            // 连接完成
            LogUtil.i("MQTT connected, isReconnect=$reconnect, serverURI=$serverURI")
            MqttManager.changeConnectStatus(MqttConnectStatus.CONNECTED)
        }
    }

    /**
     * 连接监听
     */
    private val mConnectListener = object : IMqttActionListener {
        override fun onSuccess(asyncActionToken: IMqttToken?) {
            // 连接成功
            LogUtil.i("MQTT connect success!")
            MqttManager.changeConnectStatus(MqttConnectStatus.CONNECTED)
        }

        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            // 连接失败
            LogUtil.e("MQTT connect failed, $exception")
            MqttManager.changeConnectStatus(MqttConnectStatus.CONNECTING)
        }
    }

    /**
     * 订阅主题
     * @receiver MqttAndroidClient
     */
    private fun MqttAndroidClient.subscribeTopic() {
        mOptions?.topics?.forEach { topic ->
            try {
                subscribe(
                    topic.topic,
                    topic.qos.value,
                    null,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            // 订阅成功
                            LogUtil.i("MQTT subscribe topic success, topic=$topic")
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken?,
                            exception: Throwable?
                        ) {
                            exception?.printStackTrace()
                            // 订阅失败
                            LogUtil.e("MQTT subscribe topic failed, topic=$topic")
                        }
                    })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}