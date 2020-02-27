package org.caojun.library.arouter

import org.caojun.arouter.ARouterApplication
import org.caojun.arouter.LoginInterceptor

class MainApplication : ARouterApplication() {

    override fun onCreate() {
        super.onCreate()
        //设置登录Activity
        LoginInterceptor.loginActivity = LoginActivity::class.java
    }
}