package org.caojun.library

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_demo.*
import org.caojun.widget.RulerView

class DemoActivity: Activity() {

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
    }
}