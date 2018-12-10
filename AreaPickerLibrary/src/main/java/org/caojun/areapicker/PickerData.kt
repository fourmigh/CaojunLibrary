package org.caojun.areapicker

import org.caojun.areapicker.model.City
import org.caojun.areapicker.model.District
import org.caojun.areapicker.model.Province

class PickerData {
    var pickerTitleName = ""
    var height = 0

    var provinces = ArrayList<Province>()

    var province: Province? = null
    var city: City? = null
    var district: District? = null

    val provinceText: String
        get() = if(province != null) province!!.name else ""

    val cityText: String
        get() = if(city != null) city!!.name else ""

    val districtText: String
        get() = if(district != null) district!!.name else ""

    val selectText: String
        get() = provinceText + cityText + districtText

    /**
     * 获取当前的列表
     *
     * @param index    当前层级
     * @param currText 当前选中的文字key
     * @return 返回当前的数据数组
     */
    fun getCurrDatas(index: Int, currText: String): ArrayList<String> {
        val list = ArrayList<String>()
        when (index) {
            1 -> {
                for (i in provinces.indices) {
                    list.add(provinces[i].name)
                }
            }
            2 -> {
                for (i in provinces.indices) {
                    if (currText == provinces[i].name) {
                        province = provinces[i]
                        break
                    }
                }
                for (i in province!!.cities.indices) {
                    list.add(province!!.cities[i].name)
                }
            }
            3 -> {
                for (i in province!!.cities.indices) {
                    if (currText == province!!.cities[i].name) {
                        city = province!!.cities[i]
                        break
                    }
                }
                for (i in city!!.districts.indices) {
                    list.add(city!!.districts[i].name)
                }
            }
        }
        return list
    }

    fun setCurrDatas(index: Int, currText: String) {
        when (index) {
            1 -> {
                for (i in provinces.indices) {
                    if (currText == provinces[i].name) {
                        province = provinces[i]
                        break
                    }
                }
            }
            2 -> {
                for (i in province!!.cities.indices) {
                    if (currText == province!!.cities[i].name) {
                        city = province!!.cities[i]
                        break
                    }
                }
            }
            3 -> {
                for (i in city!!.districts.indices) {
                    if (currText == city!!.districts[i].name) {
                        district = city!!.districts[i]
                        break
                    }
                }
            }
        }
    }

    fun clearSelectText(index: Int) {
        when (index) {
            1 -> {
                city = null
                district = null
            }
            2 -> {
                district = null
            }
        }
    }
}
