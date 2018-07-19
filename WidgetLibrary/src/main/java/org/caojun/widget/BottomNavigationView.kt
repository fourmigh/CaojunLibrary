package org.caojun.widget

import android.annotation.SuppressLint
import android.content.Context
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.SparseIntArray
import android.view.Menu
import android.view.MenuItem
import android.view.View

/**
 * 取消菜单项大于3时的位移动画；
 * 支持添加菜单项小红点：implementation 'q.rorbin:badgeview:latest.release'
 */
class BottomNavigationView : BottomNavigationView {

    private lateinit var itemViews: Array<BottomNavigationItemView>
    private lateinit var viewOnClickListener: View.OnClickListener

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val menuView: BottomNavigationMenuView =
                getField(BottomNavigationView::class.java, this, "mMenuView")
        itemViews = getField(BottomNavigationMenuView::class.java, menuView, "mButtons")
        viewOnClickListener = getField(BottomNavigationMenuView::class.java, menuView, "mOnClickListener")

        enableShiftingMode(menuView, false)
        enableItemShiftingMode(menuView, false)
    }

    /**
     * 是否开启选中的选项宽度变大
     *
     * @param enable It will has a shift animation if true. Otherwise all items are the same width.
     */
    @SuppressLint("RestrictedApi")
    private fun enableShiftingMode(menuView: BottomNavigationMenuView, enable: Boolean) {
        setField(BottomNavigationMenuView::class.java, menuView, "mShiftingMode", enable)
        menuView.updateMenuView()
    }

    /**
     * 是否开启选中的选项显示图标和文字，其它只显示图标
     *
     * @param enable It will has a shift animation for item if true. Otherwise the item text always be shown.
     */
    @SuppressLint("RestrictedApi")
    private fun enableItemShiftingMode(menuView: BottomNavigationMenuView, enable: Boolean) {
        for (iv in itemViews) {
            setField(BottomNavigationItemView::class.java, iv, "mShiftingMode", enable)
        }
        menuView.updateMenuView()
    }

    /**
     * get private filed in this specific object
     *
     * @param targetClass
     * @param instance    the filed owner
     * @param fieldName
     * @param <T>
     * @return field if success, null otherwise.
    </T> */
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun <T> getField(targetClass: Class<*>, instance: Any, fieldName: String): T {
        val field = targetClass.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(instance) as T
    }

    /**
     * change the field value
     *
     * @param targetClass
     * @param instance    the filed owner
     * @param fieldName
     * @param value
     */
    @Throws(NoSuchFieldException::class)
    private fun setField(targetClass: Class<*>, instance: Any, fieldName: String, value: Any) {
        val field = targetClass.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(instance, value)
    }

    /**
     * 获取 BottomNavigationView 中的单个选项
     *
     * @param position
     * @return
     */
    fun getBottomNavigationItemView(position: Int): BottomNavigationItemView {
        return itemViews[position]
    }

    /**
     * 设置 ViewPager 和 BottomNavigationView 联动：
     * 滑动 ViewPager 自动选中 BottomNavigationView；点击 BottomNavigationView 自动显示 ViewPager
     */
    fun setupWithViewPager(viewPager: ViewPager, listener: OnNavigationItemSelectedListener?) {
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageSelected(position: Int) {
                setCurrentItem(position)
            }
        })

        val menu: Menu = menu
        val size: Int = menu.size()
        val items = SparseIntArray(size)
        for (i in 0 until size) {
            val itemId = menu.getItem(i).itemId
            items.put(itemId, i)
        }
        var lastItemId = 0
        setOnNavigationItemSelectedListener { item ->
            if (lastItemId != item.itemId) {
                lastItemId = item.itemId
                viewPager.setCurrentItem(items.get(item.itemId), false)
                listener?.onNavigationItemSelected(item)
            }
            true
        }
    }

    /**
     * 设置选中 BottomNavigationView 选项
     *
     * @param item start from 0.
     */
    fun setCurrentItem(item: Int) {
        // check bounds
        if (item < 0 || item > maxItemCount) {
            throw ArrayIndexOutOfBoundsException("item is out of bounds, we expected 0 - "
                    + (maxItemCount - 1) + ". Actually " + item)
        }
        viewOnClickListener.onClick(itemViews[item])
    }

    /**
     * 选中 BottomNavigationView 时的回调
     */
    interface OnNavigationItemSelectedListener {
        fun onNavigationItemSelected(item: MenuItem)
    }

    //添加小红点实例代码
//    private fun addBadgeAt(position: Int, number: Int): Badge {
//        // add badge
//        return QBadgeView(this)
//                .setBadgeNumber(number)
//                .setGravityOffset(40f, 4f, true)
//                .bindTarget(navigation.getBottomNavigationItemView(position))
//                .setOnDragStateChangedListener { dragState, badge, targetView ->
//                    if (Badge.OnDragStateChangedListener.STATE_SUCCEED == dragState) {
//
//                    }
//                }
//    }
}

