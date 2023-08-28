package com.d10ng.mqtt.util

import android.util.Log

internal object LogUtil {

    /**
     * 日志控制开关
     */
    private var DEBUG: Boolean = false

    /**
     * 默认TAG
     */
    private const val TAG = "mqtt"

    /**
     * Application onCreate 中初始化
     * @param b Boolean
     */
    @JvmStatic
    fun init(b: Boolean) {
        DEBUG = b
    }

    /**
     * 是否为打印模式
     * @return Boolean
     */
    @JvmStatic
    fun isDebug() = DEBUG

    @JvmStatic
    fun i(tag: String, msg: String) {
        if (DEBUG) {
            Log.i(tag, msg)
        }
    }

    @JvmStatic
    fun i(msg: String) {
        if (DEBUG) {
            Log.i(TAG, msg)
        }
    }

    @JvmStatic
    fun d(tag: String, msg: String) {
        if (DEBUG) {
            Log.d(tag, msg)
        }
    }

    @JvmStatic
    fun d(msg: String) {
        if (DEBUG) {
            Log.d(TAG, msg)
        }
    }

    @JvmStatic
    fun e(tag: String, msg: String) {
        if (DEBUG) {
            Log.e(tag, msg)
        }
    }

    @JvmStatic
    fun e(msg: String) {
        if (DEBUG) {
            Log.e(TAG, msg)
        }
    }

    @JvmStatic
    fun et(tag: String, msg: String, throwable: Throwable) {
        if (DEBUG) {
            Log.e(tag, msg, throwable)
        }
    }

    @JvmStatic
    fun v(tag: String, msg: String) {
        if (DEBUG) {
            Log.v(tag, msg)
        }
    }

    @JvmStatic
    fun v(msg: String) {
        if (DEBUG) {
            Log.v(TAG, msg)
        }
    }
}