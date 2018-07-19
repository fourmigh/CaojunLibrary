package org.caojun.citypicker

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.support.v7.app.AppCompatDialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import org.caojun.citypicker.adapter.CityListAdapter
import org.caojun.citypicker.adapter.InnerListener
import org.caojun.citypicker.adapter.OnPickListener
import org.caojun.citypicker.adapter.decoration.DividerItemDecoration
import org.caojun.citypicker.adapter.decoration.SectionItemDecoration
import org.caojun.citypicker.db.DBManager
import org.caojun.citypicker.model.City
import org.caojun.citypicker.model.HotCity
import org.caojun.citypicker.model.LocateState
import org.caojun.citypicker.model.LocatedCity
import org.caojun.citypicker.view.SideIndexBar
import java.util.ArrayList

class CityPickerDialogFragment: AppCompatDialogFragment(), TextWatcher,
        View.OnClickListener, SideIndexBar.OnIndexTouchedChangedListener, InnerListener {

    private var mContentView: View? = null
    private var mRecyclerView: RecyclerView? = null
    private var mEmptyView: View? = null
    private var mOverlayTextView: TextView? = null
    private var mIndexBar: SideIndexBar? = null
    private var mSearchBox: EditText? = null
    private var mCancelBtn: TextView? = null
    private var mClearAllBtn: ImageView? = null

    private var mLayoutManager: LinearLayoutManager? = null
    private var mAdapter: CityListAdapter? = null
    private var mAllCities: MutableList<City>? = null
    private var mHotCities: MutableList<HotCity>? = null
    private var mResults: MutableList<City>? = null

    private var dbManager: DBManager? = null

    private var enableAnim = false
    private var mAnimStyle = R.style.DefaultCityPickerAnimation
    private var mLocatedCity: LocatedCity? = null
    private var locateState: Int = 0
    private var mOnPickListener: OnPickListener? = null

    companion object {
        /**
         * 获取实例
         * @param enable 是否启用动画效果
         * @return
         */
        fun newInstance(enable: Boolean): CityPickerDialogFragment {
            val fragment = CityPickerDialogFragment()
            val args = Bundle()
            args.putBoolean("cp_enable_anim", enable)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CityPickerStyle)

        val args = arguments
        if (args != null) {
            enableAnim = args.getBoolean("cp_enable_anim")
        }

        initHotCities()
        initLocatedCity()

        dbManager = DBManager(context!!)
        mAllCities = dbManager!!.getAllCities()
        mAllCities!!.add(0, HotCity("热门城市", "未知", "0"))
        if (mLocatedCity != null) {
            mAllCities!!.add(0, mLocatedCity!!)
        }
        mResults = mAllCities
    }

    private fun initLocatedCity() {
        if (mLocatedCity == null) {
            mLocatedCity = LocatedCity(getString(R.string.cp_locating), "未知", "0")
            locateState = LocateState.FAILURE
        } else {
            locateState = LocateState.SUCCESS
        }
    }

    private fun initHotCities() {
        if (mHotCities == null || mHotCities!!.isEmpty()) {
            mHotCities = ArrayList()
            mHotCities!!.add(HotCity("北京", "北京", "101010100"))
            mHotCities!!.add(HotCity("上海", "上海", "101020100"))
            mHotCities!!.add(HotCity("广州", "广东", "101280101"))
            mHotCities!!.add(HotCity("深圳", "广东", "101280601"))
            mHotCities!!.add(HotCity("天津", "天津", "101030100"))
            mHotCities!!.add(HotCity("杭州", "浙江", "101210101"))
            mHotCities!!.add(HotCity("南京", "江苏", "101190101"))
            mHotCities!!.add(HotCity("成都", "四川", "101270101"))
            mHotCities!!.add(HotCity("武汉", "湖北", "101200101"))
        }
    }

    fun setLocatedCity(location: LocatedCity) {
        mLocatedCity = location
    }

    fun setHotCities(data: MutableList<HotCity>?) {
        if (data != null && !data.isEmpty()) {
            this.mHotCities = data
        }
    }

    fun setAnimationStyle(style: Int) {
        this.mAnimStyle = if (style <= 0) R.style.DefaultCityPickerAnimation else style
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.cp_dialog_city_picker, container, false)

        mRecyclerView = mContentView!!.findViewById(R.id.cp_city_recyclerview)
        mLayoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        mRecyclerView!!.layoutManager = mLayoutManager
        mRecyclerView!!.setHasFixedSize(true)
        mRecyclerView!!.addItemDecoration(SectionItemDecoration(context!!, mAllCities!!), 0)
        mRecyclerView!!.addItemDecoration(DividerItemDecoration(context!!), 1)
        mAdapter = CityListAdapter(context!!, mAllCities!!, mHotCities!!, locateState)
        mAdapter!!.setInnerListener(this)
        mAdapter!!.setLayoutManager(mLayoutManager!!)
        mRecyclerView!!.adapter = mAdapter
        mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                //确保定位城市能正常刷新
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    mAdapter!!.refreshLocationItem()
                }
            }
        })

        mEmptyView = mContentView!!.findViewById(R.id.cp_empty_view)
        mOverlayTextView = mContentView!!.findViewById(R.id.cp_overlay)

        mIndexBar = mContentView!!.findViewById(R.id.cp_side_index_bar)
        mIndexBar!!.setOverlayTextView(mOverlayTextView!!)
                .setOnIndexChangedListener(this)

        mSearchBox = mContentView!!.findViewById(R.id.cp_search_box)
        mSearchBox!!.addTextChangedListener(this)

        mCancelBtn = mContentView!!.findViewById(R.id.cp_cancel)
        mClearAllBtn = mContentView!!.findViewById(R.id.cp_clear_all)
        mCancelBtn!!.setOnClickListener(this)
        mClearAllBtn!!.setOnClickListener(this)

        return mContentView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        val window = dialog.window
        if (window != null) {
            window.decorView.setPadding(0, 0, 0, 0)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            if (enableAnim) {
                window.setWindowAnimations(mAnimStyle)
            }
        }
        return dialog
    }

    /** 搜索框监听  */
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable) {
        val keyword = s.toString()
        if (TextUtils.isEmpty(keyword)) {
            mClearAllBtn!!.visibility = View.GONE
            mEmptyView!!.visibility = View.GONE
            mResults = mAllCities
            (mRecyclerView!!.getItemDecorationAt(0) as SectionItemDecoration).setData(mResults!!)
            mAdapter!!.updateData(mResults!!)
        } else {
            mClearAllBtn!!.visibility = View.VISIBLE
            //开始数据库查找
            mResults = dbManager!!.searchCity(keyword)
            (mRecyclerView!!.getItemDecorationAt(0) as SectionItemDecoration).setData(mResults!!)
            if (mResults == null || mResults!!.isEmpty()) {
                mEmptyView!!.visibility = View.VISIBLE
            } else {
                mEmptyView!!.visibility = View.GONE
                mAdapter!!.updateData(mResults!!)
            }
        }
        mRecyclerView!!.scrollToPosition(0)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.cp_cancel) {
            dismiss(-1, null)
        } else if (id == R.id.cp_clear_all) {
            mSearchBox!!.setText("")
        }
    }

    override fun onIndexChanged(index: String, position: Int) {
        //滚动RecyclerView到索引位置
        mAdapter!!.scrollToSection(index)
    }

    fun locationChanged(location: LocatedCity?, state: Int) {
        mAdapter!!.updateLocateState(location, state)
    }

    override fun dismiss(position: Int, data: City?) {
        dismiss()
        if (mOnPickListener != null) {
            mOnPickListener!!.onPick(position, data)
        }
    }

    override fun locate() {
        if (mOnPickListener != null) {
            mOnPickListener!!.onLocate()
        }
    }

    fun setOnPickListener(listener: OnPickListener) {
        this.mOnPickListener = listener
    }
}