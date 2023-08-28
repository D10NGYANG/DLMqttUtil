package com.d10ng.mqtt.demo

import android.app.Application
import com.d10ng.mqtt.MqttManager

class App: Application() {

    companion object {
        // 全局上下文
        lateinit var instant: Application
    }

    override fun onCreate() {
        super.onCreate()
        instant = this
        // 开启调试模式
        MqttManager.setDebug()
    }
}