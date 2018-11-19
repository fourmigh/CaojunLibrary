package org.caojun.areapicker.model

class DistrictModel {
    var name: String = ""

    constructor() : super()

    constructor(name: String) : super() {
        this.name = name
    }

    override fun toString(): String {
        return "DistrictModel [name=$name]"
    }

}
