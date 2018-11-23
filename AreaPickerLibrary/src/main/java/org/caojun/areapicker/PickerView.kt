package org.caojun.areapicker

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView


class PickerView(private val context: Activity, private val pickerData: PickerData) : PopupWindow(context), View.OnClickListener {
    private var mTextFirst: RadioButton? = null
    private var mTextSecond: RadioButton? = null
    private var mTextThird: RadioButton? = null
    private var mTextFourth: RadioButton? = null
    private var groupSelect: RadioGroup? = null
    private var pickerList: ListView? = null
    private var emptyView: TextView? = null
    private var pickerTitleName: TextView? = null
    private var pickerConfirm: TextView? = null
    private var view: View? = null
    private var index = 1
    private var currData: Array<String?>? = null
    private var adapter: DataAdapter? = null
    private var listener: OnPickerClickListener? = null
//    private var height: Int = 0

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
        view = inflater.inflate(R.layout.picker_view, null)
        this.contentView = view
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
//        this.setHeight(height)
        this.isFocusable = true
        this.animationStyle = R.style.AnimBottom
        val dw = ColorDrawable(-0x50000000)
        this.setBackgroundDrawable(dw)
        setOnDismissListener {
            mTextFirst!!.isChecked = true
            index = 1
            val lp = context.window.attributes
            lp.alpha = 1f
            context.window.attributes = lp
        }
    }

    private fun initView() {
        pickerTitleName = view!!.findViewById(R.id.pickerTitleName)
        pickerConfirm = view!!.findViewById(R.id.pickerConfirm)
        groupSelect = view!!.findViewById(R.id.groupSelect)
        mTextFirst = view!!.findViewById(R.id.mTextFirst)
        mTextSecond = view!!.findViewById(R.id.mTextSecond)
        mTextThird = view!!.findViewById(R.id.mTextThird)
        mTextFourth = view!!.findViewById(R.id.mTextFourth)
        pickerList = view!!.findViewById(R.id.pickerList)
        emptyView = view!!.findViewById(R.id.empty_data_hints)
        pickerList!!.emptyView = view!!.findViewById(R.id.picker_list_empty_data)
        mTextFirst!!.setOnClickListener(this)
        mTextSecond!!.setOnClickListener(this)
        mTextThird!!.setOnClickListener(this)
        pickerConfirm!!.setOnClickListener(this)
        if (!TextUtils.isEmpty(pickerData.pickerTitleName)) {
            pickerTitleName!!.text = pickerData.pickerTitleName
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
        currData = pickerData.getCurrDatas(index, "")
        adapter = DataAdapter(context, currData)
        pickerList!!.adapter = adapter
        if (currData == null)
            emptyView!!.visibility = View.VISIBLE
        else
            emptyView!!.visibility = View.GONE
        if (!TextUtils.isEmpty(pickerData.firstText)) {
            mTextFirst!!.text = pickerData.firstText
            if (!TextUtils.isEmpty(pickerData.secondText)) {
                mTextSecond!!.text = pickerData.secondText
                if (!TextUtils.isEmpty(pickerData.thirdText)) {
                    mTextThird!!.text = pickerData.thirdText
                    if (!TextUtils.isEmpty(pickerData.fourthText))
                        mTextFourth!!.text = pickerData.fourthText
                }
            }
            mTextFirst!!.isChecked = true
        }
        pickerList!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val currText = currData!![position]?:""
            pickerData.clearSelectText(index)
            mTextFirst!!.text = pickerData.firstText
            mTextSecond!!.text = pickerData.secondText
            mTextThird!!.text = pickerData.thirdText
            if (index == 1) {
                pickerData.firstText = currText
                mTextFirst!!.text = currText
                groupSelect!!.check(mTextFirst!!.id)
                UpdateData(currText, pickerData.secondDatas).invoke()
            } else if (index == 2) {
                pickerData.secondText = currText
                mTextSecond!!.text = currText
                groupSelect!!.check(mTextSecond!!.id)
                UpdateData(currText, pickerData.secondDatas).invoke()
            } else if (index == 3) {
                pickerData.thirdText = currText
                mTextThird!!.text = currText
                groupSelect!!.check(mTextThird!!.id)
                UpdateData(currText, pickerData.secondDatas).invoke()
            } else if (index == 4) {
                pickerData.fourthText = currText
                mTextFourth!!.text = currText
                groupSelect!!.check(mTextFourth!!.id)
                if (listener != null) {
                    listener!!.OnPickerClick(pickerData)
                }
            }
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.mTextFirst) {
            index = 1
            currData = pickerData.getCurrDatas(index, "")
            adapter!!.setList(currData)
        } else if (id == R.id.mTextSecond) {
            index = 2
            currData = pickerData.getCurrDatas(index, mTextFirst!!.text.toString())
            adapter!!.setList(currData)
        } else if (id == R.id.mTextThird) {
            index = 3
            currData = pickerData.getCurrDatas(index, mTextSecond!!.text.toString())
            adapter!!.setList(currData)
        } else if (id == R.id.mTextFourth) {
            index = 4
            currData = pickerData.getCurrDatas(index, mTextFourth!!.text.toString())
            adapter!!.setList(currData)
        } else if (id == R.id.pickerConfirm) {
            if (listener != null) {
                dismiss()
                listener!!.OnPickerConfirmClick(pickerData)
            }
        }
    }


    private inner class UpdateData(private val text: String, private val data: Map<String, Array<String?>>?) {

        operator fun invoke() {
            if (data != null && !data.isEmpty()) {
                val data = pickerData.getCurrDatas(index + 1, text)
                if (data != null && data.isNotEmpty()) {
                    currData = data
                    adapter!!.setList(currData)
                    pickerList!!.smoothScrollToPosition(0)
                    index++
                } else {
                    if (listener != null) {
                        listener!!.OnPickerClick(pickerData)
                    }
                }

            } else {
                if (listener != null) {
                    listener!!.OnPickerClick(pickerData)
                }
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
