package org.caojun.areapicker.model

/**
 * 区
 */
class District: BaseRegion {

    constructor(name: String, adCode: String?, areaCode: String?, zipCode: String?): super(name) {
        this.adCode = adCode
        this.areaCode = areaCode
        this.zipCode = zipCode
    }
}
