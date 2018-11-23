package org.caojun.areapicker

import android.app.Activity
import android.content.Context
import android.content.res.AssetManager
import android.view.View

import org.caojun.areapicker.model.CityModel
import org.caojun.areapicker.model.DistrictModel
import org.caojun.areapicker.model.ProvinceModel

import java.io.InputStream
import java.util.HashMap

import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

object AreaPicker {
    private var pickerView: PickerView? = null


    /**
     * 所有省
     */
    private var mProvinceDatas: Array<String?>? = null
    /**
     * key - 省 value - 市
     */
    private var mCitisDatasMap: MutableMap<String, Array<String?>>? = HashMap()
    /**
     * key - 市 values - 区
     */
    private var mDistrictDatasMap: MutableMap<String, Array<String?>>? = HashMap()

    /**
     * 当前省的名称
     */
    private var mCurrentProviceName: String? = null
    /**
     * 当前市的名称
     */
    private var mCurrentCityName: String? = null
    /**
     * 当前区的名称
     */
    private var mCurrentDistrictName = ""

    fun init(activity: Activity, view: View, listener: OnPickerClickListener) {
        init(activity, null, view, listener)
    }

    fun init(activity: Activity, title: String?, view: View?, listener: OnPickerClickListener) {
        if (view == null) {
            return
        }
        synchronized(view) {
            initProvinceDatas(activity)
            //选择器数据实体类封装
            val data = PickerData()
            //设置数据，有多少层级自己确定
            data.firstDatas = mProvinceDatas
            data.secondDatas = mCitisDatasMap
            data.thirdDatas = mDistrictDatasMap
            data.fourthDatas = HashMap()
            data.pickerTitleName = title?:""
            pickerView = PickerView(activity, data)
            view.setOnClickListener {
                //显示选择器
                pickerView!!.show(view)
            }
            pickerView!!.setOnPickerClickListener(listener)
        }
    }

    fun dismiss() {
        pickerView?.dismiss()
    }

    fun clear() {
        dismiss()
        pickerView = null
        mProvinceDatas = null
        mCitisDatasMap = null
        mDistrictDatasMap = null
    }

    /**
     * 解析省市区的XML数据
     */

    private fun initProvinceDatas(context: Context) {
        //http://xzqh.mca.gov.cn/map
        val provinceList: List<ProvinceModel>?
        val asset = context.applicationContext.assets
        try {
            val input = asset.open("province_data.xml")
            // 创建一个解析xml的工厂对象
            val spf = SAXParserFactory.newInstance()
            // 解析xml
            val parser = spf.newSAXParser()
            val handler = XmlParserHandler()
            parser.parse(input, handler)
            input.close()
            // 获取解析出来的数据
            provinceList = handler.dataList
            //*/ 初始化默认选中的省、市、区
            if (provinceList.isNotEmpty()) {
                mCurrentProviceName = provinceList[0].name
                val cityList = provinceList[0].cityList
                if (cityList.isNotEmpty()) {
                    mCurrentCityName = cityList[0].name
                    val districtList = cityList[0].districtList
                    if (districtList.isNotEmpty()) {
                        mCurrentDistrictName = districtList[0].name
                    }
                }
            }
            //*/
            mProvinceDatas = arrayOfNulls(provinceList.size)
            for (i in provinceList.indices) {
                // 遍历所有省的数据
                mProvinceDatas!![i] = provinceList[i].name
                val cityList = provinceList[i].cityList
                val cityNames = arrayOfNulls<String>(cityList.size)
                for (j in cityList.indices) {
                    // 遍历省下面的所有市的数据
                    cityNames[j] = cityList[j].name
                    val districtList = cityList[j].districtList
                    val distrinctNameArray = arrayOfNulls<String>(districtList.size)
                    val distrinctArray = arrayOfNulls<DistrictModel>(districtList.size)
                    for (k in districtList.indices) {
                        // 遍历市下面所有区/县的数据
                        val districtModel = DistrictModel(districtList[k].name)
                        distrinctArray[k] = districtModel
                        distrinctNameArray[k] = districtModel.name
                    }
                    // 市-区/县的数据，保存到mDistrictDatasMap
                    mDistrictDatasMap!![cityNames[j]!!] = distrinctNameArray
                }
                // 省-市的数据，保存到mCitisDatasMap
                mCitisDatasMap!![provinceList[i].name] = cityNames
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {

        }
    }
}
