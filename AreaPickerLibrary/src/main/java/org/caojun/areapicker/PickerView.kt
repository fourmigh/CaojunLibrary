package org.caojun.areapicker

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

class PickerView(private val context: Activity, private val pickerData: PickerData) : PopupWindow(context), View.OnClickListener {
    private var rbProvince: RadioButton? = null
    private var rbCity: RadioButton? = null
    private var rbDistrict: RadioButton? = null
    private var groupSelect: RadioGroup? = null
    private var pickerList: ListView? = null
    private var emptyView: TextView? = null
    private var pickerTitleName: TextView? = null
    private var pickerConfirm: TextView? = null
    private var index = 1
    private var adapter: DataAdapter? = null
    private var listener: OnPickerClickListener? = null

    init {
        this.height = pickerData.height
        if (height == 0) {
            height = getScreenH(context) / 2
        }
        initPicker()
        initView()
    }

    fun setOnPickerClickListener(listener: OnPickerClickListener) {
        this.listener = listener
    }

    private fun initPicker() {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        this.contentView = inflater.inflate(R.layout.picker_view, null)
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.isFocusable = true
        this.animationStyle = R.style.AnimBottom
        val dw = ColorDrawable(-0x50000000)
        this.setBackgroundDrawable(dw)
        setOnDismissListener {
            rbProvince!!.isChecked = true
            index = 1
            val lp = context.window.attributes
            lp.alpha = 1f
            context.window.attributes = lp
        }
    }

    private fun initView() {
        pickerTitleName = contentView.findViewById(R.id.pickerTitleName)
        pickerConfirm = contentView.findViewById(R.id.pickerConfirm)
        groupSelect = contentView.findViewById(R.id.groupSelect)
        rbProvince = contentView.findViewById(R.id.rbProvince)
        rbCity = contentView.findViewById(R.id.rbCity)
        rbDistrict = contentView.findViewById(R.id.rbDistrict)
        pickerList = contentView.findViewById(R.id.pickerList)
        emptyView = contentView.findViewById(R.id.empty_data_hints)
        pickerList?.emptyView = contentView.findViewById(R.id.picker_list_empty_data)
        rbProvince?.setOnClickListener(this)
        rbCity?.setOnClickListener(this)
        rbDistrict?.setOnClickListener(this)
        pickerConfirm?.setOnClickListener(this)
        if (!TextUtils.isEmpty(pickerData.pickerTitleName)) {
            pickerTitleName?.text = pickerData.pickerTitleName
        }
    }

    fun show(view: View) {
        val lp = context.window.attributes
        lp.alpha = 0.7f
        context.window.attributes = lp
        initData()
        showAtLocation(view, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 0)
    }

    private fun initData() {
        val currData = pickerData.getCurrDatas(index, "")
        adapter = DataAdapter(context, currData)
        pickerList?.adapter = adapter
        if (currData.isEmpty()) {
            emptyView?.visibility = View.VISIBLE
        } else {
            emptyView?.visibility = View.GONE
        }
        if (pickerData.province != null) {
            rbProvince?.text = pickerData.province?.name
            if (pickerData.city != null) {
                rbCity?.text = pickerData.city?.name
                if (pickerData.district != null) {
                    rbDistrict?.text = pickerData.district?.name
                }
            }
            rbProvince?.isChecked = true
        }
        pickerList?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val currText = currData[position]
            pickerData.clearSelectText(index)
            rbProvince?.text = pickerData.provinceText
            rbCity?.text = pickerData.cityText
            rbDistrict?.text = pickerData.districtText
            when (index) {
                1 -> {
                    //省
                    pickerData.setCurrDatas(index, currText)
                    rbProvince?.text = currText
                    groupSelect?.check(rbProvince!!.id)
                    UpdateData(currText, pickerData.hasSubData(index)).invoke()
                }
                2 -> {
                    //市
                    pickerData.setCurrDatas(index, currText)
                    rbCity?.text = currText
                    groupSelect?.check(rbCity!!.id)
                    UpdateData(currText, pickerData.hasSubData(index)).invoke()
                }
                3 -> {
                    //区
                    pickerData.setCurrDatas(index, currText)
                    rbDistrict?.text = currText
                    groupSelect?.check(rbDistrict!!.id)
                    UpdateData(currText, pickerData.hasSubData(index)).invoke()
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.rbProvince -> {
                index = 1
                adapter?.setList(pickerData.getCurrDatas(index, ""))
                val index = pickerData.getListIndex(index, rbProvince!!.text.toString())
                pickerList?.smoothScrollToPosition(index)
            }
            R.id.rbCity -> {
                index = 2
                adapter?.setList(pickerData.getCurrDatas(index, rbProvince!!.text.toString()))
                val index = pickerData.getListIndex(index, rbCity!!.text.toString())
                pickerList?.smoothScrollToPosition(index)
            }
            R.id.rbDistrict -> {
                index = 3
                adapter?.setList(pickerData.getCurrDatas(index, rbCity!!.text.toString()))
                val index = pickerData.getListIndex(index, rbDistrict!!.text.toString())
                pickerList?.smoothScrollToPosition(index)
            }
            R.id.pickerConfirm -> {
                dismiss()
                listener?.onPickerConfirmClick(pickerData)
            }
        }
    }


    private inner class UpdateData(private val text: String, private val hasSubData: Boolean) {

        operator fun invoke() {
            if (hasSubData) {
                val data = pickerData.getCurrDatas(index + 1, text)
                if (data.isNotEmpty()) {
                    adapter?.setList(data)
                    index++
                } else {
                    listener?.onPickerClick(pickerData)
                }

            } else {
                listener?.onPickerClick(pickerData)
            }
        }
    }


    fun getScreenW(aty: Context): Int {
        val dm = aty.resources.displayMetrics
        return dm.widthPixels
    }


    fun getScreenH(aty: Context): Int {
        val dm = aty.resources.displayMetrics
        return dm.heightPixels
    }
}
