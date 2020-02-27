package org.caojun.arouter

import android.support.v7.app.AppCompatActivity

open class NeedLoginBaseActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        if (!LoginInterceptor.isLogin) {
            LoginInterceptor.gotoLogin(this)
        }
    }
}