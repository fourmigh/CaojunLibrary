package org.caojun.areapicker.model

/**
 * 行政区基类
 */
open class BaseRegion(var name: String) {
    //行政区划代码
    var adCode: String? = null
    //区号
    var areaCode: String? = null
    //邮编
    var zipCode: String? = null
}