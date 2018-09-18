package org.caojun.utils

import java.math.BigDecimal

/**
 * 大写数字
 */
object ChineseNumberUtils {
    private const val NA = "N/A"
    private val ChineseNumbers = arrayOf(
            "零",//0
            "壹",//1
            "贰",//2
            "叁",//3
            "肆",//4
            "伍",//5
            "陆",//6
            "柒",//7
            "捌",//8
            "玖",//9
            "拾",//10
            "佰",//11
            "仟",//12
            "万",//13
            "亿",//14
            "元",//15
            "角",//16
            "分",//17
            "整",//18
            "人民币")//19
    private const val IndexShi = 10
    private const val IndexBai = IndexShi + 1
    private const val IndexQian = IndexBai + 1
    private const val IndexWan = IndexQian + 1
    private const val IndexYi = IndexWan + 1
    private const val IndexYuan = IndexYi + 1
    private const val IndexJiao = IndexYuan + 1
    private const val IndexFen = IndexJiao + 1
    private const val IndexZheng = IndexFen + 1
    private const val IndexRMB = IndexZheng + 1
    private val Bits = arrayOf(
            "仟亿",//12
            "佰亿",//11
            "拾亿",//10
            "亿",//9
            "仟万",//8
            "佰万",//7
            "拾万",//6
            "万",//5
            "仟",//4
            "佰",//3
            "拾",//2
            "元",//1
            "角",
            "分")

    /**
     * 数字转大写
     * @param number 数字，最多两位小数，最大位数到千亿
     * @param hasRMB 是否以“人民币”开头
     */
    fun getChineseNumber(number: Double, hasRMB: Boolean = false): String {
        if (number >= 1000000000000 || number < 0) {
            return NA
        }
        var rmb: String
        if (number == 0.toDouble()) {
            rmb = "${ChineseNumbers[0]}${ChineseNumbers[IndexYuan]}${ChineseNumbers[IndexZheng]}"
        } else {
            val bd = BigDecimal(number * 100)
            val sn = bd.toString()
            var indexPoint = sn.indexOf('.')
            if (indexPoint < 0) {
                indexPoint = sn.length
            }
            val ca = sn.substring(0, indexPoint).toCharArray()
            val array = IntArray(Bits.size)
            val index = Bits.size - ca.size
            for (i in 0 until Bits.size) {
                if (i < index) {
                    array[i] = 0
                } else {
                    array[i] = ca[i - index] - '0'
                }
            }
            val numYi = arrayOf(array[0], array[1], array[2], array[3])
            val numWan = arrayOf(array[4], array[5], array[6], array[7])
            val numYuan = arrayOf(array[8], array[9], array[10], array[11])
            val numJF = arrayOf(array[12], array[13])

            val yi = getGroup(numYi, ChineseNumbers[IndexYi], false)
            val wan = getGroup(numWan, ChineseNumbers[IndexWan], yi.isNotEmpty())
            val yuan = getGroup(numYuan, ChineseNumbers[IndexYuan], yi.isNotEmpty() || wan.isNotEmpty())
            val integer = sn.substring(0, sn.length - 2)
            val jf = getJF(numJF, integer)
            rmb = "$yi$wan$yuan$jf"
        }
        if (rmb.isNotEmpty() && hasRMB) {
            rmb = "${ChineseNumbers[IndexRMB]}$rmb"
        }
        return rmb
    }

    /**
     * 每4位一组数
     * @param unit 单位：亿，万，元
     * @param canFirstZero 首位可以为零
     */
    private fun getGroup(array: Array<Int>, unit: String, canFirstZero: Boolean): String {
        val bits = arrayOf(ChineseNumbers[IndexQian], ChineseNumbers[IndexBai], ChineseNumbers[IndexShi], unit)
        val result = StringBuilder()
        for (i in 0 until bits.size) {
            val num = array[i]
            if (num > 0) {
                result.append("${ChineseNumbers[num]}${bits[i]}")
            } else if ((canFirstZero && result.isEmpty()) || (result.isNotEmpty() && result[result.length - 1].toString() != ChineseNumbers[0]) && i < bits.size - 1) {
                result.append(ChineseNumbers[0])
            }
        }
        if (result.isEmpty()) {
            return ""
        }
        if (result[result.length - 1].toString() != unit) {
            if (result[result.length - 1].toString() == ChineseNumbers[0]) {
                //去除多余的零
                result.deleteCharAt(result.length - 1)
            }
            result.append(unit)
        }
        return result.toString()
    }

    /**
     * 角分
     * @param integer 整数部分，整数部分和角分之间有0，且角分有值，需加上零
     */
    private fun getJF(array: Array<Int>, integer: String): String {
        val bits = arrayOf(ChineseNumbers[IndexJiao], ChineseNumbers[IndexFen])
        val result = StringBuilder()
        for (i in 0 until bits.size) {
            val num = array[i]
            if (num > 0) {
                result.append("${ChineseNumbers[num]}${bits[i]}")
            } else if (i < bits.size - 1) {
                result.append(ChineseNumbers[0])
            } else {
                if (result[result.length - 1].toString() == ChineseNumbers[0]) {
                    //去除多余的零
                    result.deleteCharAt(result.length - 1)
                }
                result.append(ChineseNumbers[IndexZheng])
            }
        }
        var hasZero = false
        for (i in integer.length - 1 downTo 0) {
            if (integer[i] == '0') {
                hasZero = true
                break
            }
        }
        if (result.length > 1 && hasZero && result[0].toString() != ChineseNumbers[0]) {
            result.insert(0, ChineseNumbers[0])
        }
        return result.toString()
    }
}