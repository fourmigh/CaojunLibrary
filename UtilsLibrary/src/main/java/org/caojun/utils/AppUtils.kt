package org.caojun.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.PackageManager
import android.os.Build


object AppUtils {

    fun getPackageInfo(context: Context): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: Exception) {
            null
        }
    }

    fun getVersionName(): String {
//        val packageInfo = getPackageInfo(context)
//        return packageInfo?.versionName?:""
        return BuildConfig.VERSION_NAME
    }

    fun getVersionCode(): Int {
//        val packageInfo = getPackageInfo(context)
//        return packageInfo?.versionCode?:0
        return BuildConfig.VERSION_CODE
    }

    fun getAppName(context: Context): String {
        val packageInfo = getPackageInfo(context)
        val labelRes = packageInfo?.applicationInfo?.labelRes ?: return ""
        return context.resources.getString(labelRes)
    }
}