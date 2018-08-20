package org.caojun.swipemenulistview

import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/**
 *
 * @author baoyz
 * @date 2014-8-23
 */
class SwipeMenuView(private val mMenu: SwipeMenu, private val mListView: SwipeMenuListView) : LinearLayout(mMenu.context), OnClickListener {
    private var mLayout: SwipeMenuLayout? = null
    var onSwipeItemClickListener: OnSwipeItemClickListener? = null
    var position: Int = 0

    init {
        val items = mMenu.menuItems
        for ((id, item) in items.withIndex()) {
            addItem(item, id)
        }
    }

    private fun addItem(item: SwipeMenuItem, id: Int) {
        val params = LinearLayout.LayoutParams(item.width,
                LinearLayout.LayoutParams.MATCH_PARENT)
        val parent = LinearLayout(context)
        parent.id = id
        parent.gravity = Gravity.CENTER
        parent.orientation = LinearLayout.VERTICAL
        parent.layoutParams = params
        parent.setBackgroundDrawable(item.background)
        parent.setOnClickListener(this)
        addView(parent)

        if (item.icon != null) {
            parent.addView(createIcon(item))
        }
        if (!TextUtils.isEmpty(item.title)) {
            parent.addView(createTitle(item))
        }

    }

    private fun createIcon(item: SwipeMenuItem): ImageView {
        val iv = ImageView(context)
        iv.setImageDrawable(item.icon)
        return iv
    }

    private fun createTitle(item: SwipeMenuItem): TextView {
        val tv = TextView(context)
        tv.text = item.title
        tv.gravity = Gravity.CENTER
        tv.textSize = item.titleSize.toFloat()
        tv.setTextColor(item.titleColor)
        return tv
    }

    override fun onClick(v: View) {
        if (onSwipeItemClickListener != null && mLayout!!.isOpen) {
            onSwipeItemClickListener!!.onItemClick(this, mMenu, v.id)
        }
    }

    fun setLayout(mLayout: SwipeMenuLayout) {
        this.mLayout = mLayout
    }

    interface OnSwipeItemClickListener {
        fun onItemClick(view: SwipeMenuView, menu: SwipeMenu, index: Int)
    }
}
