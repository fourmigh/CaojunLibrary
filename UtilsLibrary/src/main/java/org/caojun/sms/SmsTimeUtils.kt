package org.caojun.sms

import android.os.Handler
import android.os.Message
import android.widget.TextView
import java.util.*


/**
 * 短信倒计时
 * Created by CaoJun on 2018-2-2.
 */
object SmsTimeUtils {
    /*倒计时时长  单位：秒*/
    private val COUNT = 60
    /*当前做*/
    private var CURR_COUNT = 0
    /*设置提现账户  标识
      发送验证码*/
    val SETTING_FINANCE_ACCOUNT_TIME = 1
    val REGISTER_TIME = 2
    val FORGET_PASSWORD_TIME = 3
    val WITHDRAW_CASH = 4

    /*设置提现账户
      预计结束的时间*/
    private var SETTING_FINANCE_ACCOUNT_TIME_END: Long = 0
    /*注册*/
    private var REGISTER_TIME_END: Long = 0
    /*忘记密码*/
    private var FORGET_PASSWORD_TIME_END: Long = 0
    /*提现*/
    private var WITHDRAW_CASH_END: Long = 0


    private var countdownTimer: Timer? = null
    private var tvSendCode: TextView? = null

    private var text = "获取验证码"

    /**
     * 检查是否超过60秒
     * 给当前要从多少开始倒数赋值
     * http://blog.csdn.net/qq_16965811
     * @param type  1，设置提现账户 2，注册3，忘记密码
     * @param first true 表示第一次   false不是
     * @return 是否需要调用startCountdown(TextView textView)，主要用于判断在重新打开页，需不需要继续倒计时
     */
    fun check(type: Int, first: Boolean, text: String): Boolean {
        this.text = text
        val data = System.currentTimeMillis()
        var time: Long = 0
        when (type) {
            REGISTER_TIME -> time = REGISTER_TIME_END
            FORGET_PASSWORD_TIME -> time = FORGET_PASSWORD_TIME_END
            SETTING_FINANCE_ACCOUNT_TIME -> time = SETTING_FINANCE_ACCOUNT_TIME_END
            WITHDRAW_CASH -> time = WITHDRAW_CASH_END
        }
        if (data > time) {
            /*主要是区别于是否是第一次进入。第一次进入不需要赋值*/
            if (!first) {
                CURR_COUNT = COUNT
                time = data + COUNT * 1000
                when (type) {
                    REGISTER_TIME -> REGISTER_TIME_END = time
                    FORGET_PASSWORD_TIME -> FORGET_PASSWORD_TIME_END = time
                    SETTING_FINANCE_ACCOUNT_TIME -> SETTING_FINANCE_ACCOUNT_TIME_END = time
                    WITHDRAW_CASH -> WITHDRAW_CASH_END = time
                }
            }
            return false
        } else {
            val the_difference = (time - data).toInt() / 1000
            CURR_COUNT = the_difference
            return true
        }
    }

    /**
     * 开始倒计时
     * http://blog.csdn.net/qq_16965811
     * @param textView 控制倒计时的view
     */
    fun startCountdown(textView: TextView) {
        tvSendCode = textView
        if (countdownTimer == null) {
            countdownTimer = Timer()
            countdownTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    val msg = Message()
                    msg.what = CURR_COUNT--
                    handler.sendMessage(msg)
                }
            }, 0, 1000)
        }
    }


    private val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what === 0) {
                if (countdownTimer != null) {
                    countdownTimer!!.cancel()
                    countdownTimer = null
                }
                tvSendCode!!.text = text
                tvSendCode!!.isEnabled = true
            } else {
                tvSendCode!!.text = msg.what.toString() + "s"
                tvSendCode!!.isEnabled = false
            }
            super.handleMessage(msg)
        }
    }
}