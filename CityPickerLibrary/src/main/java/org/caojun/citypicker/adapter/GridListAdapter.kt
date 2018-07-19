package org.caojun.citypicker.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import org.caojun.citypicker.R
import org.caojun.citypicker.model.HotCity

class GridListAdapter(context: Context, data: List<HotCity>) : RecyclerView.Adapter<GridListAdapter.GridViewHolder>() {

    companion object {
        const val SPAN_COUNT = 3
    }

    private val mContext: Context = context
    private val mData: List<HotCity> = data
    private var mInnerListener: InnerListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.cp_grid_item_layout, parent, false)
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val pos = holder.adapterPosition
        val data = mData[pos]
        //设置item宽高
        val dm = mContext.resources.displayMetrics
        val screenWidth = dm.widthPixels
        val typedValue = TypedValue()
        mContext.theme.resolveAttribute(R.attr.cpGridItemSpace, typedValue, true)
        val space = mContext.resources.getDimensionPixelSize(typedValue.resourceId)
        val padding = mContext.resources.getDimensionPixelSize(R.dimen.cp_default_padding)
        val indexBarWidth = mContext.resources.getDimensionPixelSize(R.dimen.cp_index_bar_width)
        val itemWidth = (screenWidth - padding - space * (SPAN_COUNT - 1) - indexBarWidth) / SPAN_COUNT
        val lp = holder.container.layoutParams
        lp.width = itemWidth
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.container.layoutParams = lp

        holder.name.text = data.getName()
        holder.container.setOnClickListener {
            if (mInnerListener != null) {
                mInnerListener!!.dismiss(pos, data)
            }
        }
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var container: FrameLayout = itemView.findViewById(R.id.cp_grid_item_layout)
        internal var name: TextView = itemView.findViewById(R.id.cp_gird_item_name)

    }

    fun setInnerListener(listener: InnerListener) {
        this.mInnerListener = listener
    }
}