package org.caojun.areapicker.model

import java.util.ArrayList

class ProvinceModel {
    var name: String = ""
    var cityList = ArrayList<CityModel>()

//    constructor() : super()
//
//    constructor(name: String, cityList: ArrayList<CityModel>) : super() {
//        this.name = name
//        this.cityList = cityList
//    }

    override fun toString(): String {
        return "ProvinceModel [name=$name, cityList=$cityList]"
    }

}
