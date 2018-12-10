package org.caojun.areapicker

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.View
import javax.xml.parsers.SAXParserFactory

object AreaPicker {
    private var pickerView: PickerView? = null

    fun init(activity: Activity, view: View, listener: OnPickerClickListener, firstProvince: String? = null) {
        init(activity, null, view, listener, firstProvince)
    }

    fun init(activity: Activity, title: String?, view: View?, listener: OnPickerClickListener, firstProvince: String? = null) {
        if (view == null) {
            return
        }
        synchronized(view) {
            val data = initProvinceDatas(activity as Context, firstProvince) ?: return@synchronized
            data.pickerTitleName = title?:""
            pickerView = PickerView(activity, data)
            view.setOnClickListener {
                //显示选择器
                pickerView?.show(view)
            }
            pickerView?.setOnPickerClickListener(listener)
        }
    }

    fun dismiss() {
        pickerView?.dismiss()
    }

    fun clear() {
        dismiss()
        pickerView = null
    }

    /**
     * 解析省市区的XML数据
     */

    private fun initProvinceDatas(context: Context, firstProvince: String? = null): PickerData? {
        //http://xzqh.mca.gov.cn/map
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
            val provinces = handler.dataList

            if (!TextUtils.isEmpty(firstProvince)) {
                var index = -1
                for (i in provinces.indices) {
                    if (provinces[i].name == firstProvince) {
                        index = i
                        break
                    }
                }
                if (index > 0) {
                    val province = provinces[index]
                    for (i in index downTo 1) {
                        provinces[i] = provinces[i - 1]
                    }
                    provinces[0] = province
                }
            }

            val pickerData = PickerData()
            pickerData.provinces = provinces
            return pickerData
        } catch (e: Throwable) {
            e.printStackTrace()
            return null
        }
    }
}
