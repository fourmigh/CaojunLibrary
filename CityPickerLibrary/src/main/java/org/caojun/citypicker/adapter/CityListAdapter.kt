package org.caojun.citypicker.adapter

import android.content.Context
import android.os.Handler
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import org.caojun.citypicker.R
import org.caojun.citypicker.adapter.decoration.GridItemDecoration
import org.caojun.citypicker.model.City
import org.caojun.citypicker.model.HotCity
import org.caojun.citypicker.model.LocateState
import org.caojun.citypicker.model.LocatedCity

class CityListAdapter(context: Context, data: MutableList<City>, hotData: MutableList<HotCity>, state: Int) : RecyclerView.Adapter<CityListAdapter.BaseViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CURRENT = 10
        private const val VIEW_TYPE_HOT = 11
    }

    private val mContext: Context = context
    private var mData: MutableList<City> = data
    private val mHotData: List<HotCity> = hotData
    private var locateState: Int = state
    private var mInnerListener: InnerListener? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private var stateChanged: Boolean = false

    fun setLayoutManager(manager: LinearLayoutManager) {
        this.mLayoutManager = manager
    }

    fun updateData(data: MutableList<City>) {
        this.mData = data
        notifyDataSetChanged()
    }

    fun updateLocateState(location: LocatedCity?, state: Int) {
        mData.removeAt(0)
        if (location != null) {
            mData.add(0, location)
        }
        stateChanged = locateState != state
        locateState = state
        refreshLocationItem()
    }

    fun refreshLocationItem() {
        //如果定位城市的item可见则进行刷新
        if (stateChanged && mLayoutManager!!.findFirstVisibleItemPosition() == 0) {
            stateChanged = false
            notifyItemChanged(0)
        }
    }

    /**
     * 滚动RecyclerView到索引位置
     * @param index
     */
    fun scrollToSection(index: String) {
        if (mData.isEmpty()) return
        if (TextUtils.isEmpty(index)) return
        val size = mData.size
        for (i in 0 until size) {
            if (TextUtils.equals(index.substring(0, 1), mData[i].getSection().substring(0, 1))) {
                if (mLayoutManager != null) {
                    mLayoutManager!!.scrollToPositionWithOffset(i, 0)
                    if (TextUtils.equals(index.substring(0, 1), "定")) {
                        //防止滚动时进行刷新
                        Handler().postDelayed({ if (stateChanged) notifyItemChanged(0) }, 1000)
                    }
                    return
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view: View
        return when (viewType) {
            VIEW_TYPE_CURRENT -> {
                view = LayoutInflater.from(mContext).inflate(R.layout.cp_list_item_location_layout, parent, false)
                LocationViewHolder(view)
            }
            VIEW_TYPE_HOT -> {
                view = LayoutInflater.from(mContext).inflate(R.layout.cp_list_item_hot_layout, parent, false)
                HotViewHolder(view)
            }
            else -> {
                view = LayoutInflater.from(mContext).inflate(R.layout.cp_list_item_default_layout, parent, false)
                DefaultViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        if (holder is DefaultViewHolder) {
            val pos = holder.getAdapterPosition()
            val data = mData[pos]
            holder.name.text = data.getName()
            holder.name.setOnClickListener {
                if (mInnerListener != null) {
                    mInnerListener!!.dismiss(pos, data)
                }
            }
        }
        //定位城市
        if (holder is LocationViewHolder) {
            val pos = holder.getAdapterPosition()
            val data = mData[pos]
            //设置宽高
            val dm = mContext.resources.displayMetrics
            val screenWidth = dm.widthPixels
            val typedValue = TypedValue()
            mContext.theme.resolveAttribute(R.attr.cpGridItemSpace, typedValue, true)
            val space = mContext.resources.getDimensionPixelSize(R.dimen.cp_grid_item_space)
            val padding = mContext.resources.getDimensionPixelSize(R.dimen.cp_default_padding)
            val indexBarWidth = mContext.resources.getDimensionPixelSize(R.dimen.cp_index_bar_width)
            val itemWidth = (screenWidth - padding - space * (GridListAdapter.SPAN_COUNT - 1) - indexBarWidth) / GridListAdapter.SPAN_COUNT
            val lp = holder.container.layoutParams
            lp.width = itemWidth
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
            holder.container.layoutParams = lp

            when (locateState) {
                LocateState.LOCATING -> holder.current.setText(R.string.cp_locating)
                LocateState.SUCCESS -> holder.current.text = data.getName()
                LocateState.FAILURE -> holder.current.setText(R.string.cp_locate_failed)
            }
            holder.container.setOnClickListener {
                if (locateState == LocateState.SUCCESS) {
                    if (mInnerListener != null) {
                        mInnerListener!!.dismiss(pos, data)
                    }
                } else if (locateState == LocateState.FAILURE) {
                    locateState = LocateState.LOCATING
                    notifyItemChanged(0)
                    if (mInnerListener != null) {
                        mInnerListener!!.locate()
                    }
                }
            }
        }
        //热门城市
        if (holder is HotViewHolder) {
            val mAdapter = GridListAdapter(mContext, mHotData)
            if (mInnerListener != null) {
                mAdapter.setInnerListener(mInnerListener!!)
            }
            holder.mRecyclerView.adapter = mAdapter
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && TextUtils.equals("定", mData[position].getSection().substring(0, 1)))
            return VIEW_TYPE_CURRENT
        return if (position == 1 && TextUtils.equals("热", mData[position].getSection().substring(0, 1))) VIEW_TYPE_HOT else super.getItemViewType(position)
    }

    fun setInnerListener(listener: InnerListener) {
        this.mInnerListener = listener
    }

    open class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class DefaultViewHolder internal constructor(itemView: View) : BaseViewHolder(itemView) {
        internal var name: TextView = itemView.findViewById(R.id.cp_list_item_name)

    }

    class HotViewHolder internal constructor(itemView: View) : BaseViewHolder(itemView) {
        internal var mRecyclerView: RecyclerView = itemView.findViewById(R.id.cp_hot_list)

        init {
            mRecyclerView.setHasFixedSize(true)
            mRecyclerView.layoutManager = GridLayoutManager(itemView.context,
                    GridListAdapter.SPAN_COUNT, LinearLayoutManager.VERTICAL, false)
            val space = itemView.context.resources.getDimensionPixelSize(R.dimen.cp_grid_item_space)
            mRecyclerView.addItemDecoration(GridItemDecoration(GridListAdapter.SPAN_COUNT,
                    space))
        }
    }

    class LocationViewHolder internal constructor(itemView: View) : BaseViewHolder(itemView) {
        internal var container: FrameLayout = itemView.findViewById(R.id.cp_list_item_location_layout)
        internal var current: TextView = itemView.findViewById(R.id.cp_list_item_location)

    }
}