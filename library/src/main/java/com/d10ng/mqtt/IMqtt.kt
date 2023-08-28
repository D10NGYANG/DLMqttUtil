package com.d10ng.mqtt

import android.content.Context
import com.d10ng.mqtt.bean.MqttClientOptions

interface IMqtt {

    /**
     * 连接MQTT
     * @param context Context
     * @param options MqttClientOptions
     */
    fun connect(context: Context, options: MqttClientOptions)

    /**
     * 断开连接
     */
    fun disconnect()

    /**
     * 发送消息
     * @param topic String
     * @param message String
     * @return Boolean
     */
    suspend fun publish(topic: String, message: String): Boolean
}