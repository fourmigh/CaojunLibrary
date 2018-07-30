package org.caojun.utils

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import java.util.regex.Pattern

/**
 * 金额输入
 * @param MAX_VALUE 最大输入值
 */
class CashierInputFilter(private val MAX_VALUE: Int = Int.MAX_VALUE) : InputFilter {

    private val mPattern: Pattern = Pattern.compile("([0-9]|\\.)*")

    /**
     * @param source    新输入的字符串
     * @param start     新输入的字符串起始下标，一般为0
     * @param end       新输入的字符串终点下标，一般为source长度-1
     * @param dest      输入之前文本框内容
     * @param dstart    原内容起始坐标，一般为0
     * @param dend      原内容终点坐标，一般为dest长度-1
     * @return          输入内容
     */
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence {
        val sourceText = source.toString()
        val destText = dest.toString()
        //验证删除等按键
        if (TextUtils.isEmpty(sourceText)) {
            return ""
        }
        val matcher = mPattern.matcher(source)
        //已经输入小数点的情况下，只能输入数字
        if (destText.contains(POINTER)) {
            if (!matcher.matches()) {
                return ""
            } else {
                if (POINTER == source) {  //只能输入一个小数点
                    return ""
                }
            }
            //验证小数点精度，保证小数点后只能输入1位
            val index = destText.indexOf(POINTER)
            val length = dend - index
            if (length > POINTER_LENGTH) {
                return dest.subSequence(dstart, dend)
            }
        } else {
            //没有输入小数点的情况下，只能输入小数点和数字，但首位不能输入小数点和0
            if (!matcher.matches()) {
                return ""
            } else {
                if (POINTER == source && TextUtils.isEmpty(destText)) {
                    return ""
                }
                //如果首位为“0”，则只能再输“.”
                if (ZERO == destText) {
                    if (POINTER != sourceText) {
                        return ""
                    }
                }
            }
        }
        //验证输入金额的大小
        val sumText = java.lang.Double.parseDouble(destText + sourceText)
        return if (sumText > MAX_VALUE) {
            dest.subSequence(dstart, dend)
        } else dest.subSequence(dstart, dend).toString() + sourceText
    }

    companion object {
        //小数点后的位数
        private val POINTER_LENGTH = 2
        private val POINTER = "."
        private val ZERO = "0"
    }
}
