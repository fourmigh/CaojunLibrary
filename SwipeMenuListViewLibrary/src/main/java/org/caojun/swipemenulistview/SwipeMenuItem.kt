package org.caojun.swipemenulistview

import android.content.Context
import android.graphics.drawable.Drawable

/**
 *
 * @author baoyz
 * @date 2014-8-23
 */
class SwipeMenuItem(private val mContext: Context) {

    var id: Int = 0
    var title: String? = null
    var icon: Drawable? = null
    var background: Drawable? = null
    var titleColor: Int = 0
    var titleSize: Int = 0
    var width: Int = 0

    fun setTitle(resId: Int) {
        title = mContext.getString(resId)
    }

    fun setIcon(resId: Int) {
        this.icon = mContext.resources.getDrawable(resId)
    }

    fun setBackground(resId: Int) {
        this.background = mContext.resources.getDrawable(resId)
    }
}
