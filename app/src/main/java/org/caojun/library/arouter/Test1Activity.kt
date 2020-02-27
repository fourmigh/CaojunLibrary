package org.caojun.library.arouter

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route
import org.caojun.library.R

@Route(path = Const.PATH_TEST1)
class Test1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arouter_activity_test1)
    }
}