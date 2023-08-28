package com.d10ng.mqtt.constant

/**
 * MQTT连接状态
 */
enum class MqttConnectStatus {

    // 未连接
    DISCONNECTED,

    // 连接中
    CONNECTING,

    // 已连接
    CONNECTED
}