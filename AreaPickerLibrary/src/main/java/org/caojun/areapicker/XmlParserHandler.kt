package org.caojun.areapicker

import org.caojun.areapicker.model.CityModel
import org.caojun.areapicker.model.DistrictModel
import org.caojun.areapicker.model.ProvinceModel
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

import java.util.ArrayList


class XmlParserHandler : DefaultHandler() {
    private val provinceList = ArrayList<ProvinceModel>()

    val dataList: List<ProvinceModel>
        get() = provinceList

    internal var provinceModel = ProvinceModel()
    internal var cityModel = CityModel()
    internal var districtModel = DistrictModel()

    override fun startDocument() {

    }

    override fun startElement(uri: String, localName: String, qName: String,
                              attributes: Attributes) {
        when (qName) {
            "province" -> {
                provinceModel = ProvinceModel()
                provinceModel.name = attributes.getValue(0)
                provinceModel.cityList = ArrayList()
            }
            "city" -> {
                cityModel = CityModel()
                cityModel.name = attributes.getValue(0)
                cityModel.districtList = ArrayList()
            }
            "district" -> {
                districtModel = DistrictModel()
                districtModel.name = attributes.getValue(0)
            }
        }
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        when (qName) {
            "district" -> cityModel.districtList.add(districtModel)
            "city" -> provinceModel.cityList.add(cityModel)
            "province" -> provinceList.add(provinceModel)
        }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {

    }

}
