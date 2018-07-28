package org.caojun.sms

import android.content.Context
import android.util.SparseArray
import android.widget.TextView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*


object SmsTimeUtils {

    /*倒计时时长  单位：秒*/
    private val COUNT = 60
    /*当前做*/
    private var CURR_COUNT = 0

    private var countdownTimer: Timer? = null

    private val typeTime = SparseArray<Long>()

    /**
     * 检查是否超过60秒
     * 给当前要从多少开始倒数赋值
     * http://blog.csdn.net/qq_16965811
     * @param type  计时器类型
     * @param first true 表示第一次   false不是
     * @return 是否需要调用startCountdown(TextView textView)，主要用于判断在重新打开页，需不需要继续倒计时
     */
    fun check(type: Int, first: Boolean): Boolean {
        val data = System.currentTimeMillis()
        var time = typeTime[type]?:0
        return if (data > time) {
            /*主要是区别于是否是第一次进入。第一次进入不需要赋值*/
            if (!first) {
                CURR_COUNT = COUNT
                time = data + COUNT * 1000
                typeTime.put(type, time)
            }
            false
        } else {
            CURR_COUNT = (time - data).toInt() / 1000
            true
        }
    }

    /**
     * 开始倒计时
     * @param context Context
     * @param textView 控制倒计时的view
     * @param resIdSend 例如文字“获取验证码”
     * @param resIdWait 例如文字“%s秒后重试”
     */
    fun startCountdown(context: Context, textView: TextView, resIdSend: Int, resIdWait: Int) {

        if (countdownTimer == null) {
            countdownTimer = Timer()
            countdownTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    CURR_COUNT--
                    if (CURR_COUNT == 0) {
                        countdownTimer?.cancel()
                        countdownTimer = null
                        doAsync {
                            uiThread {
                                textView.setText(resIdSend)
                                textView.isEnabled = true
                            }
                        }
                    } else {
                        doAsync {
                            uiThread {
                                textView.text = context.getString(resIdWait, CURR_COUNT)
                                textView.isEnabled = false
                            }
                        }
                    }
                }
            }, 0, 1000)
        }
    }
}