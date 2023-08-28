package com.d10ng.mqtt.bean

import org.eclipse.paho.client.mqttv3.MqttConnectOptions

/**
 * MQTT客户端配置
 */
data class MqttClientOptions(
    // 客户端ID
    var clientId: String = "android_client",
    // 服务器地址，如：tcp://localhost:1883
    var host: String,
    // 用户名
    var username: String = "",
    // 密码
    var password: String = "",
    // 是否清除会话
    var isCleanSession: Boolean = true,
    // 连接超时时间，单位秒
    var connectionTimeout: Int = 10,
    // 心跳时间，单位秒
    var keepAliveInterval: Int = 20,
    // 自动重连间隔时间，单位秒
    var autoReconnectInterval: Int = 10,
    // 订阅主题列表
    var topics: List<Topic> = listOf(),
) {

    data class Topic(
        var topic: String,
        var qos: Qos = Qos.QOS_0
    ) {
        enum class Qos(val value: Int) {
            // 最多一次，有可能重复或丢失
            QOS_0(0),
            // 至少一次，有可能重复
            QOS_1(1),
            // 只有一次，确保消息只到达一次（用于比较严格的计费系统）
            QOS_2(2)
        }
    }
    fun toMqttConnectOptions() = MqttConnectOptions().apply {
        this.userName = this@MqttClientOptions.username
        this.password = this@MqttClientOptions.password.toCharArray()
        this.isCleanSession = this@MqttClientOptions.isCleanSession
        this.connectionTimeout = this@MqttClientOptions.connectionTimeout
        this.keepAliveInterval = this@MqttClientOptions.keepAliveInterval
        this.isAutomaticReconnect = false
    }
}
