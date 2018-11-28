package org.caojun.utils

import android.text.TextUtils
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import java.util.regex.Pattern

object FormatUtils {

    /**
     * 每三位数字用逗号隔开
     */
    fun numberFormat(number: Any) = NumberFormat.getInstance(Locale.CHINA).format(number)

    /**
     * 补足两位小数
     */
    fun amountFormat(amount: Any) = DecimalFormat("#.00").format(amount)

    /**
     * 金额
     */
    fun amount(value: Any): String {
        var amount = amountFormat(value)
        if (amount == ".00") {
            amount = "0.00"
        }
        val index = amount.indexOf('.')
        val number = numberFormat(amount.substring(0, index).toDouble())
        return "$number${amount.substring(index)}"
    }

    fun isMobileNo(mobile: String): Boolean {
        try {
            val p = Pattern.compile("^1[3|4|5|7|8][0-9]\\d{8}$")
            val m = p.matcher(mobile)
            return m.matches()
        } catch (e: Exception) {
        }
        return false
    }

    fun maskMobile(mobile: String): String {
        return if (!isMobileNo(mobile)) {
            mobile
        } else mobile.substring(0, 3) + "****" + mobile.substring(mobile.length - 4)
    }

    fun isBankCardNo(cardNo: String): Boolean {
        if (TextUtils.isEmpty(cardNo) || cardNo.length < 15 || cardNo.length > 19) {
            return false
        }
        var sum = 0
        var odd = false
        for (i in cardNo.length - 1 downTo 0) {
            val c = cardNo[i] - '0'
            if (odd) {
                val v = c * 2
                sum += if (v > 9) v - 9 else v
            } else {
                sum += c
            }
            odd = !odd
        }
        return sum % 10 == 0
    }

    fun maskBankCardNo(cardNo: String): String {
        return if (!isBankCardNo(cardNo)) {
            cardNo
        } else cardNo.substring(0, 4) + " **** **** " + cardNo.substring(cardNo.length - 4)
    }

    fun bankCardNoFormat(cardNo: String): String {
        if (!isBankCardNo(cardNo)) {
            return cardNo
        }
        val sb = StringBuffer()
        var index = 0
        while (index < cardNo.length) {
            val c = cardNo[index]
            if (index > 0 && index % 4 == 0) {
                sb.append(' ')
            }
            sb.append(c)
            index++
        }
        return sb.toString()
    }
}