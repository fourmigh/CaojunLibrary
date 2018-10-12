package org.caojun.library

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.socks.library.KLog
import kotlinx.android.synthetic.main.activity_demo.*
import org.caojun.activity.BaseActivity
import org.caojun.imageview.ImageShow
import org.caojun.utils.AppSignUtils
import org.caojun.utils.ChineseNumberUtils
import org.caojun.utils.FormatUtils
import org.caojun.widget.RulerView
import org.caojun.utils.AppSignUtils.getSingInfo



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
//            checkSelfPermission(Manifest.permission.CAMERA, object : ActivityUtils.RequestPermissionListener {
//
//                override fun onFail() {
//                    finish()
//                }
//
//                override fun onSuccess() {
//                    startActivityForResult<HeartRateActivity>(1)
//                }
//            })

            val url = "https://42f2671d685f51e10fc6-b9fcecea3e50b3b59bdc28dead054ebc.ssl.cf5.rackcdn.com/illustrations/forgot_password_gi2d.svg"
            ImageShow.show(this@DemoActivity, url)
        }

        KLog.d("ChineseNumberUtils", ChineseNumberUtils.getChineseNumber(0.00))
        KLog.d("ChineseNumberUtils", ChineseNumberUtils.getChineseNumber(0.00, true))
        KLog.d("ChineseNumberUtils", ChineseNumberUtils.getChineseNumber(6500.00, true))
        KLog.d("ChineseNumberUtils", ChineseNumberUtils.getChineseNumber(3150.50))
        KLog.d("ChineseNumberUtils", ChineseNumberUtils.getChineseNumber(105000.00))
        KLog.d("ChineseNumberUtils", ChineseNumberUtils.getChineseNumber(60036000.00))
        KLog.d("ChineseNumberUtils", ChineseNumberUtils.getChineseNumber(35000.96))
        KLog.d("ChineseNumberUtils", ChineseNumberUtils.getChineseNumber(35000.06))
        KLog.d("ChineseNumberUtils", ChineseNumberUtils.getChineseNumber(150001.00))

        val signal = AppSignUtils.getSingInfo(applicationContext, "com.allinpay.yunshangtong", AppSignUtils.MD5)
        KLog.d("signal", "signal: $signal")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            KLog.d("HeartRateActivity", "value: ${data.getIntExtra("data", 0)}")
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}