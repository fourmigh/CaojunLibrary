package org.caojun.areapicker

import java.util.HashMap

class PickerData {
    var firstDatas: Array<String?>? = null
    var secondDatas: MutableMap<String, Array<String?>>? = HashMap()
    var thirdDatas: MutableMap<String, Array<String?>>? = HashMap()
    var fourthDatas: MutableMap<String, Array<String?>>? = HashMap()
    var firstText = ""
    var secondText = ""
    var thirdText = ""
    var fourthText = ""
    var pickerTitleName = ""
    var height = 0

    val selectText: String
        get() = firstText + secondText + thirdText + fourthText

    /**
     * 获取当前的列表
     *
     * @param index    当前层级
     * @param currText 当前选中的文字key
     * @return 返回当前的数据数组
     */
    fun getCurrDatas(index: Int, currText: String): Array<String?>? {
        var curr: Array<String?>? = arrayOf()
        when (index) {
            1 -> curr = firstDatas
            2 -> curr = secondDatas!![currText]
            3 -> curr = thirdDatas!![currText]
            4 -> curr = fourthDatas!![currText]
        }
        return curr
    }

    fun setInitSelectText(firstText: String) {
        this.firstText = firstText
    }

    fun setInitSelectText(firstText: String, secondText: String) {
        this.firstText = firstText
        this.secondText = secondText
    }

    fun setInitSelectText(firstText: String, secondText: String, thirdText: String) {
        this.firstText = firstText
        this.secondText = secondText
        this.thirdText = thirdText
    }

    fun setInitSelectText(firstText: String, secondText: String, thirdText: String, fourthText: String) {
        this.firstText = firstText
        this.secondText = secondText
        this.thirdText = thirdText
        this.fourthText = fourthText
    }

    fun clearSelectText(index: Int) {
        when (index) {
            1 -> {
                secondText = ""
                thirdText = ""
                fourthText = ""
            }
            2 -> {
                thirdText = ""
                fourthText = ""
            }
            3 -> fourthText = ""
        }
    }
}
