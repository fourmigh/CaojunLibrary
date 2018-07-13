package org.caojun.imageview

import android.app.Activity
import org.jetbrains.anko.startActivity

object ImageShow {

    fun show(activity: Activity, url: String) {
        activity.startActivity<ImageActivity>(ImageActivity.Key_Url to url)
    }
}