package com.d10ng.mqtt.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * 网络工具
 */
internal object NetUtil {

    // 网络特征流
    private var networkCapabilitiesFlow = MutableStateFlow<NetworkCapabilities?>(null)
    // 网络是否可用
    val netStatusFlow = networkCapabilitiesFlow.map { isNetworkAvailable(it) }

    private var manager: ConnectivityManager? = null

    /**
     * 开始监听网络状态
     * @param context Context
     */
    fun startNetStatusListener(context: Context) {
        if (null == manager) {
            manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        } else {
            // 取消上一次监听任务
            manager?.unregisterNetworkCallback(callback)
        }
        manager?.apply {
            // 获取初始值
            networkCapabilitiesFlow.value = getNetworkCapabilities(activeNetwork)
            // 开始监听变化
            val request = NetworkRequest.Builder().build()
            registerNetworkCallback(request, callback)
        }
    }

    private val callback = object: ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            networkCapabilitiesFlow.value = null
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            networkCapabilitiesFlow.value = networkCapabilities
        }
    }

    /**
     * 网络是否可用
     * @return Boolean
     */
    fun isNetworkAvailable(capabilities: NetworkCapabilities? = networkCapabilitiesFlow.value): Boolean {
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    }
}