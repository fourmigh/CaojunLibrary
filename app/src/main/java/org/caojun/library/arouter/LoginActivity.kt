package org.caojun.library.arouter

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.caojun.arouter.LoginInterceptor
import kotlinx.android.synthetic.main.arouter_activity_login.*
import org.caojun.library.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arouter_activity_login)

        btnLogin.setOnClickListener {
            LoginInterceptor.loginSuccess(this)
        }
    }

    override fun onBackPressed() {
    }
}