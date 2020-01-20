package org.caojun.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.NetworkInfo
import android.net.ConnectivityManager
import android.net.wifi.*
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils

/**
 * 需获取权限android.permission.READ_PHONE_STATE后调用
 */
object NetworkUtils {

    fun getConnectivityManager(context: Context): ConnectivityManager {

        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private fun getActiveNetworkInfo(context: Context): NetworkInfo {

        val cm = getConnectivityManager(context)
        // 获取活动网络连接信息
        return cm.activeNetworkInfo
    }

    fun isAvailable(context: Context): Boolean {
        val networkInfo = getActiveNetworkInfo(context)
        return networkInfo.isAvailable
    }

    fun getTelephonyManager(context: Context): TelephonyManager {
        return context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    }

    private fun getWifiNetworkInfo(context: Context): NetworkInfo {
        val cm = getConnectivityManager(context)
        return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
    }

    fun isWifiConnected(context: Context): Boolean {
        val mWifiInfo = getWifiNetworkInfo(context)
        return mWifiInfo.isConnected
    }

    fun getWifiManager(context: Context): WifiManager {
        return context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun isWifiEnabled(context: Context): Boolean {
        val wifiManager = getWifiManager(context)
        return wifiManager.isWifiEnabled
    }

    fun getScanResult(context: Context): List<ScanResult> {
        val wifiManager = getWifiManager(context)
        val scanResults = wifiManager.scanResults
        if (scanResults.isEmpty()) {
            return scanResults
        }
        val name = getConnectedWifiName(context)
        val list = ArrayList<ScanResult>()
        for (i in 0 until scanResults.size) {
            if (TextUtils.isEmpty(scanResults[i].SSID)) {
                continue
            }
            if (existScanResult(scanResults[i], list)) {
                continue
            }
            if (name == scanResults[i].SSID) {
                list.add(0, scanResults[i])
            } else {
                list.add(scanResults[i])
            }
        }
        return list
    }

    private fun existScanResult(sr: ScanResult, list: ArrayList<ScanResult>): Boolean {
        if (list.isEmpty()) {
            return false
        }
        for (i in 0 until list.size) {
            if (sr.SSID != list[i].SSID) {
                continue
            }
            if (sr.frequency <= list[i].frequency) {
                return true
            }
            list.removeAt(i)
            return false
        }
        return false
    }

    private fun getConnectionInfo(context: Context): WifiInfo? {
        val wifiManager = getWifiManager(context)
        val info = wifiManager.connectionInfo
        if (SupplicantState.COMPLETED != info.supplicantState) {
            return null
        }
        if (-1 == info.networkId) {
            return null
        }
        return if (0 == info.ipAddress) {
            null
        } else {
            info
        }
    }

    fun getConnectedWifiName(context: Context): String {
        val mWifiInfo = getConnectionInfo(context)?:return ""
        return mWifiInfo.ssid.replace("\"", "")
    }

    enum class WifiType {
        WIFI_CIPHER_NO_PASSWORD, WIFI_CIPHER_WEP, WIFI_CIPHER_WPA, WIFI_CIPHER_WPA2
    }

    private fun getType(scanResult: ScanResult): WifiType {
        return when {
            scanResult.capabilities.contains("WPA2") -> WifiType.WIFI_CIPHER_WPA2
            scanResult.capabilities.contains("WPA") -> WifiType.WIFI_CIPHER_WPA
            scanResult.capabilities.contains("WEP") -> WifiType.WIFI_CIPHER_WEP
            else -> WifiType.WIFI_CIPHER_NO_PASSWORD
        }
    }

    /**
     * 创建WifiConfiguration
     * 三个安全性的排序为：WEP<WPA></WPA><WPA2></WPA2>。
     * WEP是Wired Equivalent Privacy的简称，有线等效保密（WEP）协议是对在两台设备间无线传输的数据进行加密的方式，
     * 用以防止非法用户窃听或侵入无线网络
     * WPA全名为Wi-Fi Protected Access，有WPA和WPA2两个标准，是一种保护无线电脑网络（Wi-Fi）安全的系统，
     * 它是应研究者在前一代的系统有线等效加密（WEP）中找到的几个严重的弱点而产生的
     * WPA是用来替代WEP的。WPA继承了WEP的基本原理而又弥补了WEP的缺点：WPA加强了生成加密密钥的算法，
     * 因此即便收集到分组信息并对其进行解析，也几乎无法计算出通用密钥；WPA中还增加了防止数据中途被篡改的功能和认证功能
     * WPA2是WPA的增强型版本，与WPA相比，WPA2新增了支持AES的加密方式
     *
     * @param scanResult
     * @param password
     * @return
     */
    fun createWifiInfo(context: Context, scanResult: ScanResult, password: String): WifiConfiguration {
        var config: WifiConfiguration? = null
        val wifiManager = getWifiManager(context)
        if (wifiManager != null) {
            val existingConfigs = wifiManager.configuredNetworks
            for (existingConfig in existingConfigs) {
                if (existingConfig == null) continue
                if (existingConfig.SSID == "\"" + scanResult.SSID + "\"") {
                    config = existingConfig
                    break
                }
            }
        }
        if (config == null) {
            config = WifiConfiguration()
        }
        config.allowedAuthAlgorithms.clear()
        config.allowedGroupCiphers.clear()
        config.allowedKeyManagement.clear()
        config.allowedPairwiseCiphers.clear()
        config.allowedProtocols.clear()
        config.SSID = "\"" + scanResult.SSID + "\""

        val type = getType(scanResult)
        when (type) {
            WifiType.WIFI_CIPHER_NO_PASSWORD -> {
                config.wepKeys[0] = ""
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                config.wepTxKeyIndex = 0
            }
            WifiType.WIFI_CIPHER_WEP -> {
                config.hiddenSSID = true
                config.wepKeys[0] = "\"" + password + "\""
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                config.wepTxKeyIndex = 0
            }
            WifiType.WIFI_CIPHER_WPA -> {
                config.preSharedKey = "\"" + password + "\""
                config.hiddenSSID = true
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                config.status = WifiConfiguration.Status.ENABLED
            }
            WifiType.WIFI_CIPHER_WPA2 -> {
                config.preSharedKey = "\"" + password + "\""
                config.hiddenSSID = true
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                config.status = WifiConfiguration.Status.ENABLED
            }
        }
        return config
    }

    var wifiListener: WifiListener? = null
    interface WifiListener {
        //已关闭
        fun disabled()

        //正在关闭
        fun disabling()

        //已启用
        fun enabled()

        //启动中
        fun enabling()

        //未知
        fun unknown()

        //连接到wifi
        fun connected(wifiName: String)

        //断开
        fun disconnect()

        //密码错误
        fun passwordError()
    }

    private class WifiBroadCastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0)
            when (wifiState) {
                WifiManager.WIFI_STATE_DISABLED//WIFI已关闭
                -> wifiListener?.disabled()
                WifiManager.WIFI_STATE_DISABLING//WIFI正在关闭中
                -> wifiListener?.disabling()
                WifiManager.WIFI_STATE_ENABLED//WIFI已启用
                -> wifiListener?.enabled()
                WifiManager.WIFI_STATE_ENABLING//WIFI正在启动中
                -> wifiListener?.enabling()
                WifiManager.WIFI_STATE_UNKNOWN//未知WIFI状态
                -> wifiListener?.unknown()
            }

//            if (intent.action == WifiManager.RSSI_CHANGED_ACTION) {
//                Log.i(TAG, "wifi信号强度变化")
//            }

            //wifi连接上与否
            if (intent.action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {

                val info = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                when (info.state) {
                    NetworkInfo.State.DISCONNECTED -> wifiListener?.disconnect()
                    //Log.i(TAG, "wifi断开");
                    NetworkInfo.State.CONNECTED -> {
                        val wifiInfo = getConnectionInfo(context)?:return
                        //获取当前wifi名称
                        wifiListener?.connected(wifiInfo.ssid)
                    }
                }
            }

            if (intent.action == WifiManager.SUPPLICANT_STATE_CHANGED_ACTION) {
                val error = intent.getIntExtra(
                    WifiManager.EXTRA_SUPPLICANT_ERROR,
                    0
                )
                if (WifiManager.ERROR_AUTHENTICATING == error) {
                    wifiListener?.passwordError()
                }
            }
        }
    }

    private var mReceiver: WifiBroadCastReceiver? = null

    /**
     * 注册广播监听wifi状态
     */
    fun registerReceiver(context: Context) {
        val filter = IntentFilter()
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION) //wifi打开关闭
        //连接
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        mReceiver = WifiBroadCastReceiver()
        context.registerReceiver(mReceiver, filter)
    }

    /**
     * 注销广播
     */
    fun unregisterReceiver(context: Context) {
        if (mReceiver != null) {
            context.unregisterReceiver(mReceiver)
            mReceiver = null
        }
    }
}