package org.caojun.swipemenulistview

import android.content.Context

import java.util.ArrayList

/**
 *
 * @author baoyz
 * @date 2014-8-23
 */
class SwipeMenu(val context: Context) {
    private val mItems: MutableList<SwipeMenuItem>
    var viewType: Int = 0

    val menuItems: List<SwipeMenuItem>
        get() = mItems

    init {
        mItems = ArrayList()
    }

    fun addMenuItem(item: SwipeMenuItem) {
        mItems.add(item)
    }

    fun removeMenuItem(item: SwipeMenuItem) {
        mItems.remove(item)
    }

    fun getMenuItem(index: Int): SwipeMenuItem {
        return mItems[index]
    }

}
