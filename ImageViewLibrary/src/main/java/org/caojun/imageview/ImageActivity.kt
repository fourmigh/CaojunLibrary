package org.caojun.imageview

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity: Activity() {

    companion object {
        val Key_Url = "Key_Url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        val url = intent.getStringExtra(Key_Url)
        if (TextUtils.isEmpty(url)) {
            return
        }

        //缩放开关，设置此属性，仅支持双击缩放，不支持触摸缩放
        webView.settings.setSupportZoom(true)
        //设置是否可缩放，会出现缩放工具（若为true则上面的设值也默认为true）
        webView.settings.builtInZoomControls = true
        //隐藏缩放工具
        webView.settings.displayZoomControls = false
        //设置推荐使用的窗口
        webView.settings.useWideViewPort = true
        //设置加载的页面的模式
        webView.settings.loadWithOverviewMode = true

        webView.loadUrl(url)
    }
}