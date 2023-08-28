package com.d10ng.mqtt

import android.content.Context
import com.d10ng.mqtt.bean.MqttClientOptions
import com.d10ng.mqtt.bean.MqttMessage
import com.d10ng.mqtt.constant.MqttConnectStatus
import com.d10ng.mqtt.util.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * MQTT管理器
 */
object MqttManager: IMqtt {

    // 连接状态
    private val connectStatusFlow = MutableStateFlow(MqttConnectStatus.DISCONNECTED)

    // 订阅消息
    private val messageFlow = MutableSharedFlow<MqttMessage>(extraBufferCapacity = 100)
    private val receiveScope = CoroutineScope(Dispatchers.IO)

    /**
     * 设置是否为调试模式，控制日志输出
     * @param b Boolean
     */
    fun setDebug(b: Boolean = true) {
        LogUtil.init(b)
    }

    /**
     * 修改MQTT连接状态
     * @param status MqttConnectStatus
     */
    @Synchronized
    internal fun changeConnectStatus(status: MqttConnectStatus) {
        connectStatusFlow.value = status
    }

    /**
     * 获取当前MQTT连接状态
     * @return MqttConnectStatus
     */
    fun getCurrentConnectStatus() = connectStatusFlow.value

    /**
     * 获取MQTT连接状态Flow
     * @return StateFlow<MqttConnectStatus>
     */
    fun getConnectStatusFlow() = connectStatusFlow.asStateFlow()

    /**
     * 接收订阅消息
     * @param topic String
     * @param message String
     */
    internal fun receiveMessage(topic: String, message: String) {
        receiveScope.launch { messageFlow.emit(MqttMessage(topic, message)) }
    }

    /**
     * 获取订阅消息Flow
     * @return SharedFlow<MqttMessage>
     */
    fun getMessageFlow() = messageFlow.asSharedFlow()

    /**
     * 连接MQTT
     * @param context Context
     * @param options MqttClientOptions
     */
    override fun connect(context: Context, options: MqttClientOptions) {
        MqttWorker.instance.connect(context, options)
    }

    /**
     * 断开连接
     */
    override fun disconnect() {
        MqttWorker.instance.disconnect()
    }

    /**
     * 发送消息
     * @param topic String
     * @param message String
     * @return Boolean
     */
    override suspend fun publish(topic: String, message: String): Boolean {
        return MqttWorker.instance.publish(topic, message)
    }
}