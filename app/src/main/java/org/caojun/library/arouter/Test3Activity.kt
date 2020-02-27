package org.caojun.library.arouter

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import kotlinx.android.synthetic.main.arouter_activity_test3.*
import org.caojun.arouter.LoginInterceptor
import org.caojun.arouter.NeedLoginBaseActivity
import org.caojun.library.R

@Route(path = Const.PATH_TEST3)
class Test3Activity : NeedLoginBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arouter_activity_test3)

        btnLogout.setOnClickListener {
            LoginInterceptor.isLogin = false
            finish()
        }
    }
}