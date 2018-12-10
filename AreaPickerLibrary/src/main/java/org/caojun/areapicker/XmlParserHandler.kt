package org.caojun.areapicker

import org.caojun.areapicker.model.City
import org.caojun.areapicker.model.District
import org.caojun.areapicker.model.Province
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.util.ArrayList

class XmlParserHandler : DefaultHandler() {

    companion object {
        private const val PROVINCE = "province"
        private const val CITY = "city"
        private const val DISTRICT = "district"
    }

    private val provinces = ArrayList<Province>()

    val dataList: ArrayList<Province>
        get() = provinces

    private var province: Province? = null
    private var city: City? = null
    private var district: District? = null

    override fun startDocument() {

    }

    override fun startElement(uri: String, localName: String, qName: String,
                              attributes: Attributes) {
        val name = attributes.getValue("name")
        val adCode = attributes.getValue("adCode")
        var areaCode = attributes.getValue("areaCode")
        val zipCode = attributes.getValue("zipCode")
        when (qName) {
            PROVINCE -> {
                province = Province(name, adCode, areaCode)
                province?.cities?.clear()
            }
            CITY -> {
                if (areaCode == null) {
                    //区号为空时，说明和省相同
                    areaCode = province?.areaCode
                }
                city = City(name, adCode, areaCode, zipCode)
                city?.districts?.clear()
            }
            DISTRICT -> {
                if (areaCode == null) {
                    //区号为空时，说明和市相同
                    areaCode = city?.areaCode
                }
                district = District(name, adCode, areaCode, zipCode)
            }
        }
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        when (qName) {
            PROVINCE -> provinces.add(province!!)
            CITY -> province?.cities?.add(city!!)
            DISTRICT -> city?.districts?.add(district!!)
        }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {

    }

}
