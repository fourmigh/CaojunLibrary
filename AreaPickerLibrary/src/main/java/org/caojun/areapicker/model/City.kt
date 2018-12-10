package org.caojun.areapicker.model

import java.util.ArrayList

/**
 * 市、直辖市的区
 */
class City: BaseRegion {
    val districts= ArrayList<District>()

    constructor(name: String): super(name)

    constructor(name: String, adCode: String?, areaCode: String?, zipCode: String?): super(name) {
        this.adCode = adCode
        this.areaCode = areaCode
        this.zipCode = zipCode
    }
}
