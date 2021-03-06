package org.caojun.utils

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
object ActivityUtils {

    interface RequestPermissionListener {
        fun onSuccess()
        fun onFail()
    }

    private var requestPermissionListener: RequestPermissionListener? = null

    fun onRequestPermissionsResult(grantResults: IntArray) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestPermissionListener?.onSuccess()
        } else {
            requestPermissionListener?.onFail()
        }
        requestPermissionListener = null
    }

    fun checkSelfPermission(activity: Activity, permission: String, listener: RequestPermissionListener): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissionListener = listener
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 0)
            return false
        }
        listener.onSuccess()
        return true
    }

    fun startApplication(context: Context, packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun gotoMarket(context: Context, packageName: String) {
        val uri = Uri.parse("market://details?id=$packageName")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun gotoPermission(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }

    fun getIMEI(activity: Activity, listener: RequestPermissionListener) {
        checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE, listener)
    }

    fun call(context: Context, number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun getSharedPreferences(context: Context, name: String, key: String, defValue: Boolean): Boolean {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE).getBoolean(key, defValue)
    }

    fun getSharedPreferences(context: Context, name: String, key: String, defValue: String): String {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE).getString(key, defValue)?:""
    }

    fun getSharedPreferences(context: Context, name: String, key: String, defValue: Int): Int {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE).getInt(key, defValue)
    }

    fun shareImage(context: Context, title: String, resId: Int) {
        val resources = context.resources
        val path = ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId) + "/" + resources.getResourceTypeName(resId) + "/" + resources.getResourceEntryName(resId)
        shareImage(context, title, Uri.parse(path))
    }

    fun shareImage(context: Context, title: String, file: File) {
        shareImage(context, title, Uri.fromFile(file))
    }

    private fun shareImage(context: Context, title: String, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpeg"

        intent.putExtra(Intent.EXTRA_STREAM, uri)
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun shareText(context: Context, title: String, msg: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"

        intent.putExtra(Intent.EXTRA_SUBJECT, title)
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun copyClipboardData(context: Context, clipboardData: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(null, clipboardData)
        clipboardManager.primaryClip = clipData
        context.toast(context.getString(R.string.clipboard_alert, clipboardData))
    }

    private fun getResId(context: Context, name: String, defType: String): Int {
        return context.resources.getIdentifier(name, defType, context.packageName)
    }

    fun getRowResId(context: Context, name: String): Int? {
        val resId = getResId(context, name, "raw")
        return if (resId > 0) {
            resId
        } else {
            null
        }
    }

    fun getDrawableResId(context: Context, name: String): Int? {
        val resId = getResId(context, name, "drawable")
        return if (resId > 0) {
            resId
        } else {
            null
        }
    }

    fun getStringResId(context: Context, name: String): Int? {
        val resId = getResId(context, name, "string")
        return if (resId > 0) {
            resId
        } else {
            null
        }
    }

    fun startActivity(context: Context, packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }
}