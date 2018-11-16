package org.caojun.giraffeplayer

import android.app.Activity
import android.content.pm.ActivityInfo
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager

/**
 * Created by tcking on 2017
 */

class UIHelper(private val activity: Activity?) {


    private// if the device's natural orientation is portrait:
    // if the device's natural orientation is landscape or if the device
    // is square:
    val screenOrientation: Int
        get() {
            if (activity == null) {
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            val rotation = activity.windowManager.defaultDisplay.rotation
            val dm = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(dm)
            val width = dm.widthPixels
            val height = dm.heightPixels
            val orientation: Int
            if ((rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) && height > width || (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) && width > height) {
                orientation = when (rotation) {
                    Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            } else {
                orientation = when (rotation) {
                    Surface.ROTATION_0 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    Surface.ROTATION_90 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    Surface.ROTATION_180 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    Surface.ROTATION_270 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                }
            }

            return orientation
        }

    fun requestedOrientation(orientation: Int): UIHelper {
        if (activity == null) {
            return this
        }
        activity.requestedOrientation = orientation
        return this
    }

    fun showActionBar(show: Boolean): UIHelper {
        if (activity == null) {
            return this
        }
        if (activity is AppCompatActivity) {
            val supportActionBar = activity.supportActionBar
            if (supportActionBar != null) {
                try {
                    supportActionBar.setShowHideAnimationEnabled(false)
//                    supportActionBar.javaClass.getDeclaredMethod("setShowHideAnimationEnabled", Boolean::class.javaPrimitiveType).invoke(supportActionBar, false)
                } catch (e: Exception) {

                }

                if (show) {
                    supportActionBar.show()
                } else {
                    supportActionBar.hide()
                }
            }
        }
        return this
    }

    fun fullScreen(fullScreen: Boolean): UIHelper {
        if (activity == null) {
            return this
        }
        val attrs = activity.window.attributes
        if (fullScreen) {
            attrs.flags = attrs.flags or WindowManager.LayoutParams.FLAG_FULLSCREEN
            activity.window.attributes = attrs
            //            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN.inv()
            activity.window.attributes = attrs
            //            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        return this
    }

    companion object {

        fun with(activity: Activity): UIHelper {
            return UIHelper(activity)
        }
    }
}
