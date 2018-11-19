package org.caojun.areapicker.model

import java.util.ArrayList

class CityModel {
    var name: String = ""
    var districtList = ArrayList<DistrictModel>()

    constructor() : super()

    constructor(name: String, districtList: ArrayList<DistrictModel>) : super() {
        this.name = name
        this.districtList = districtList
    }

    override fun toString(): String {
        return ("CityModel [name=" + name + ", districtList=" + districtList
                + "]")
    }

}
