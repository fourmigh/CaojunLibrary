package org.caojun.library.arouter

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import kotlinx.android.synthetic.main.arouter_activity_test2.*
import org.caojun.arouter.NeedLoginBaseActivity
import org.caojun.library.R

@Route(path = Const.PATH_TEST2)
class Test2Activity : NeedLoginBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arouter_activity_test2)

        btnGotoTest3.setOnClickListener {
            ARouter.getInstance().build(Const.PATH_TEST3).navigation()
        }
    }
}