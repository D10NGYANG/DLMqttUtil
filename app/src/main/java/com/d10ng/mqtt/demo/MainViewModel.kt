package com.d10ng.mqtt.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d10ng.datelib.curTime
import com.d10ng.datelib.toDateStr
import com.d10ng.mqtt.MqttManager
import com.d10ng.mqtt.bean.MqttClientOptions
import com.d10ng.mqtt.bean.MqttMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    // 输入账号
    val usernameFlow = MutableStateFlow("mqtt-client")
    // 输入密码
    val passwordFlow = MutableStateFlow("1qaz2wsx")
    // 输入主机地址
    val hostFlow = MutableStateFlow("tcp://192.168.200.6:1883")
    // Mqtt连接状态
    val connectStatusFlow = MqttManager.getConnectStatusFlow()
    // 订阅消息列表
    val messageListFlow = MutableStateFlow(listOf<MqttMessage>())
    // 是否启动连接
    val isStartConnectFlow = MutableStateFlow(false)
    // 订阅主题
    val topicsFlow = MutableStateFlow("tocard/#")

    init {
        viewModelScope.launch {
            // 订阅消息
            MqttManager.getMessageFlow().collect {
                val ls = messageListFlow.value.toMutableList()
                ls.add(0, it)
                messageListFlow.value = ls
            }
        }
    }

    /**
     * 点击连接
     */
    fun onClickConnect() {
        MqttManager.connect(
            App.instant,
            MqttClientOptions(
                username = usernameFlow.value,
                password = passwordFlow.value,
                host = hostFlow.value,
                topics = listOf()
            )
        )
        isStartConnectFlow.value = true
    }

    /**
     * 点击断开连接
     */
    fun onClickDisconnect() {
        MqttManager.disconnect()
        isStartConnectFlow.value = false
    }

    /**
     * 点击订阅主题
     */
    fun onClickSubscribe() {
        topicsFlow.value.split(";").forEach {
            MqttManager.subscribe(MqttClientOptions.Topic(it, MqttClientOptions.Topic.Qos.QOS_0))
        }
    }

    /**
     * 点击取消订阅
     */
    fun onClickUnsubscribe() {
        topicsFlow.value.split(";").forEach {
            MqttManager.unsubscribe(it)
        }
    }

    /**
     * 点击发布消息
     */
    fun onClickPublish() {
        CoroutineScope(Dispatchers.IO).launch {
            val isSuccess = MqttManager.publish("topic0", "Hello World! ${curTime.toDateStr()}")
            println("发布消息结果：$isSuccess")
        }
    }
}