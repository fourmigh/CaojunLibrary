package org.caojun.areapicker.model

import java.util.ArrayList

/**
 * 省、自治区、直辖市、特别行政区
 */
class Province: BaseRegion {
    val cities = ArrayList<City>()

    //省、自治区
    constructor(name: String): super(name)

    //直辖市、特别行政区
    constructor(name: String, adCode: String?, areaCode: String?): super(name) {
        this.adCode = adCode
        this.areaCode = areaCode
    }
}
