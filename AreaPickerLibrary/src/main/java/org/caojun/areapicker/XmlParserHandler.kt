package org.caojun.areapicker

import org.caojun.areapicker.model.CityModel
import org.caojun.areapicker.model.DistrictModel
import org.caojun.areapicker.model.ProvinceModel
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

import java.util.ArrayList


class XmlParserHandler : DefaultHandler() {

    companion object {
        private const val PROVINCE = "province"
        private const val CITY = "city"
        private const val DISTRICT = "district"
    }

    private val provinceList = ArrayList<ProvinceModel>()

    val dataList: List<ProvinceModel>
        get() = provinceList

    private var provinceModel = ProvinceModel()
    private var cityModel = CityModel()
    private var districtModel = DistrictModel()

    override fun startDocument() {

    }

    override fun startElement(uri: String, localName: String, qName: String,
                              attributes: Attributes) {
        when (qName) {
            PROVINCE -> {
                provinceModel = ProvinceModel()
                provinceModel.name = attributes.getValue(0)
                provinceModel.cityList = ArrayList()
            }
            CITY -> {
                cityModel = CityModel()
                cityModel.name = attributes.getValue(0)
                cityModel.districtList = ArrayList()
            }
            DISTRICT -> {
                districtModel = DistrictModel()
                districtModel.name = attributes.getValue(0)
            }
        }
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        when (qName) {
            PROVINCE -> provinceList.add(provinceModel)
            CITY -> provinceModel.cityList.add(cityModel)
            DISTRICT -> cityModel.districtList.add(districtModel)
        }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {

    }

}
