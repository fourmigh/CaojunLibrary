package org.caojun.listener

import android.view.View
import android.view.MotionEvent

class OnDoubleClickListener(callback: DoubleClickCallback) : View.OnTouchListener {

    private var count = 0
    private var firClick: Long = 0
    private var secClick: Long = 0
    /**
     * 两次点击时间间隔，单位毫秒
     */
    private val interval = 1500
    private val mCallback: DoubleClickCallback? = callback

    interface DoubleClickCallback {
        fun onDoubleClick()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        var result = false
        if (MotionEvent.ACTION_DOWN == event.action) {
            count++
            if (1 == count) {
                firClick = System.currentTimeMillis()
            } else if (2 == count) {
                secClick = System.currentTimeMillis()
                if (secClick - firClick < interval) {
                    mCallback?.onDoubleClick()
                    count = 0
                    firClick = 0
                    result = true
                } else {
                    firClick = secClick
                    count = 0
                }
                secClick = 0
            }
        }
        return result
    }
}