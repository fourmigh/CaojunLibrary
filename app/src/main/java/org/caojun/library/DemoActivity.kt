package org.caojun.library

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.socks.library.KLog
import kotlinx.android.synthetic.main.activity_demo.*
import org.caojun.activity.BaseActivity
import org.caojun.heartrate.HeartRateActivity
import org.caojun.utils.ActivityUtils
import org.caojun.utils.FormatUtils
import org.caojun.widget.RulerView
import org.jetbrains.anko.startActivityForResult

class DemoActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)

//        rulerView.setValue(20000f, 0f, 100000f, 1000f)
//
//        rulerView2.setValue(165f, 80f, 250f, 1f)

        rulerView.setOnValueChangeListener(object : RulerView.OnValueChangeListener {
            override fun onValueChange(value: Float) {
                textView.text = value.toString()
            }

        })

        KLog.d("numberFormat", FormatUtils.numberFormat(123456789.1))
        KLog.d("amountFormat", FormatUtils.amountFormat(123456789.2))
        KLog.d("amount", FormatUtils.amount(123456789.3))

        text_test.setOnClickListener {
            checkSelfPermission(Manifest.permission.CAMERA, object : ActivityUtils.RequestPermissionListener {

                override fun onFail() {
                    finish()
                }

                override fun onSuccess() {
                    startActivityForResult<HeartRateActivity>(1)
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            KLog.d("HeartRateActivity", "value: ${data.getIntExtra("data", 0)}")
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}