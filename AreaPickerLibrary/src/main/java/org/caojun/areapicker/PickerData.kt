package org.caojun.areapicker

import org.caojun.areapicker.model.City
import org.caojun.areapicker.model.District
import org.caojun.areapicker.model.Province

class PickerData(private val provinces: ArrayList<Province>) {
    var pickerTitleName = ""
    var height = 0

    private var province: Province? = null
    private var city: City? = null
    private var district: District? = null

    val provinceName: String
        get() = if(province != null) province!!.name else ""

    val cityName: String
        get() = if(city != null) city!!.name else ""

    val districtName: String
        get() = if(district != null) district!!.name else ""

    val selectText: String
        get() = provinceName + cityName + districtName

    val adCode: String?
        get() {
            return when {
                district != null -> district!!.adCode
                city != null -> city!!.adCode
                province != null -> province!!.adCode
                else -> null
            }
        }

    val areaCode: String?
        get() {
            return when {
                district != null -> district!!.areaCode
                city != null -> city!!.areaCode
                province != null -> province!!.areaCode
                else -> null
            }
        }

    val zipCode: String?
        get() {
            return when {
                district != null -> district!!.zipCode
                city != null -> city!!.zipCode
                province != null -> province!!.zipCode
                else -> null
            }
        }

    /**
     * 获取当前的列表
     *
     * @param index    当前层级
     * @param currText 当前选中的文字key
     * @return 返回当前的数据数组
     */
    fun getCurrDatas(index: Int): ArrayList<String> {
        val list = ArrayList<String>()
        when (index) {
            1 -> {
                for (i in provinces.indices) {
                    list.add(provinces[i].name)
                }
            }
            2 -> {
                for (i in province!!.cities.indices) {
                    list.add(province!!.cities[i].name)
                }
            }
            3 -> {
                for (i in city!!.districts.indices) {
                    list.add(city!!.districts[i].name)
                }
            }
        }
        return list
    }

    fun getListIndex(index: Int, name: String): Int {
        when (index) {
            1 -> {
                for (i in provinces.indices) {
                    if (name == provinces[i].name) {
                        return i
                    }
                }
            }
            2 -> {
                if (province != null) {
                    for (i in province!!.cities.indices) {
                        if (name == province!!.cities[i].name) {
                            return i
                        }
                    }
                }
            }
            3 -> {
                if (city != null) {
                    for (i in city!!.districts.indices) {
                        if (name == city!!.districts[i].name) {
                            return i
                        }
                    }
                }
            }
        }
        return 0
    }

    fun hasSubData(index: Int): Boolean {
        when (index) {
            1 -> {
                if (province == null) {
                    return false
                }
                return province!!.cities.isNotEmpty()
            }
            2 -> {
                if (city == null) {
                    return false
                }
                return city!!.districts.isNotEmpty()
            }
            else -> return false
        }
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
