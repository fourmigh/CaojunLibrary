package org.caojun.library.arouter

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.alibaba.android.arouter.launcher.ARouter
import org.caojun.library.R
import kotlinx.android.synthetic.main.arouter_activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arouter_activity_main)

        btnTest1.setOnClickListener {
            ARouter.getInstance().build(Const.PATH_TEST1).navigation()
        }

        btnTest2.setOnClickListener {
            ARouter.getInstance().build(Const.PATH_TEST2).navigation()
        }
    }
}
