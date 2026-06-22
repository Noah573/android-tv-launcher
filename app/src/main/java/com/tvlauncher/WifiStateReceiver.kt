package com.tvlauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager

class WifiStateReceiver(
    private val onStateChanged: (Boolean) -> Unit
) : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                val wifiManager = context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager
                val isConnected = wifiManager.isWifiEnabled && 
                    wifiManager.connectionInfo.networkId != -1
                onStateChanged(isConnected)
            }
            ConnectivityManager.CONNECTIVITY_ACTION -> {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
                    as ConnectivityManager
                val networkInfo: NetworkInfo? = cm.activeNetworkInfo
                val isConnected = networkInfo?.isConnected == true
                onStateChanged(isConnected)
            }
        }
    }
}
