package org.caojun.citypicker.model

class LocatedCity: City {
    constructor(name: String, province: String, code: String): super(name, province, "定位城市", code)

    constructor(name: String, province: String): this(name, province, "")
}