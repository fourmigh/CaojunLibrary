package org.caojun.arouter

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter

open class ARouterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ARouter.init(this)
    }
}