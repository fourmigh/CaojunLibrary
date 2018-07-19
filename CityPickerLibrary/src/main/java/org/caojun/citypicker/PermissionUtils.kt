package org.caojun.citypicker

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import org.jetbrains.anko.toast
import java.io.File

/**
 * Created by CaoJun on 2017/9/5.
 */
object PermissionUtils {

    interface RequestPermissionListener {
        fun onSuccess()
        fun onFail()
    }

    private var requestPermissionListener: RequestPermissionListener? = null

    fun checkSelfPermission(activity: Activity, permission: String, listener: RequestPermissionListener): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissionListener = listener
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 0)
            return false
        }
        listener.onSuccess()
        return true
    }
}