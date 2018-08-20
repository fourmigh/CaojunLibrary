package org.caojun.swipemenulistview

import android.content.Context
import android.support.v4.view.GestureDetectorCompat
import android.support.v4.widget.ScrollerCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector.OnGestureListener
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import android.widget.AbsListView
import android.widget.FrameLayout

/**
 *
 * @author baoyz
 * @date 2014-8-23
 */
class SwipeMenuLayout : FrameLayout {

    private var mSwipeDirection: Int = 0

    var contentView: View? = null
    var menuView: SwipeMenuView? = null
    private var mDownX: Int = 0
    private var state = STATE_CLOSE
    private var mGestureDetector: GestureDetectorCompat? = null
    private var mGestureListener: OnGestureListener? = null
    private var isFling: Boolean = false
    private val MIN_FLING = dp2px(15)
    private val MAX_VELOCITYX = -dp2px(500)
    private var mOpenScroller: ScrollerCompat? = null
    private var mCloseScroller: ScrollerCompat? = null
    private var mBaseX: Int = 0
    var position: Int = 0
        set(position) {
            field = position
            menuView?.position = position
        }
    private var mCloseInterpolator: Interpolator? = null
    private var mOpenInterpolator: Interpolator? = null

    var swipeEnable = true

    val isOpen: Boolean
        get() = state == STATE_OPEN

    @JvmOverloads
    constructor(contentView: View, menuView: SwipeMenuView,
                closeInterpolator: Interpolator? = null, openInterpolator: Interpolator? = null) : super(contentView.context) {
        this.mCloseInterpolator = closeInterpolator
        this.mOpenInterpolator = openInterpolator
        this.contentView = contentView
        this.menuView = menuView
        this.menuView!!.setLayout(this)
        init()
    }

    private constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    private constructor(context: Context) : super(context)

    fun setSwipeDirection(swipeDirection: Int) {
        mSwipeDirection = swipeDirection
    }

    private fun init() {
        layoutParams = AbsListView.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT)
        mGestureListener = object : SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent): Boolean {
                isFling = false
                return true
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent,
                                 velocityX: Float, velocityY: Float): Boolean {
                if (Math.abs(e1.x - e2.x) > MIN_FLING && velocityX < MAX_VELOCITYX) {
                    isFling = true
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }
        }
        mGestureDetector = GestureDetectorCompat(context,
                mGestureListener)

        // mScroller = ScrollerCompat.create(getContext(), new
        // BounceInterpolator());
        if (mCloseInterpolator != null) {
            mCloseScroller = ScrollerCompat.create(context,
                    mCloseInterpolator)
        } else {
            mCloseScroller = ScrollerCompat.create(context)
        }
        if (mOpenInterpolator != null) {
            mOpenScroller = ScrollerCompat.create(context,
                    mOpenInterpolator)
        } else {
            mOpenScroller = ScrollerCompat.create(context)
        }

        val contentParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        contentView?.layoutParams = contentParams
        if (contentView!!.id < 1) {
            contentView?.id = CONTENT_VIEW_ID
        }

        menuView?.id = MENU_VIEW_ID
        menuView?.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT)

        addView(contentView)
        addView(menuView)

        //自定义背景色
        this.setBackgroundResource(R.drawable.bg_item)
    }

    fun onSwipe(event: MotionEvent): Boolean {
        mGestureDetector!!.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mDownX = event.x.toInt()
                isFling = false
            }
            MotionEvent.ACTION_MOVE -> {
                // Log.i("byz", "downX = " + mDownX + ", moveX = " + event.getX());
                var dis = (mDownX - event.x).toInt()
                if (state == STATE_OPEN) {
                    dis += menuView!!.width * mSwipeDirection
                }
                swipe(dis)
            }
            MotionEvent.ACTION_UP -> if ((isFling || Math.abs(mDownX - event.x) > menuView!!.width / 2) && Math.signum(mDownX - event.x) == mSwipeDirection.toFloat()) {
                // open
                smoothOpenMenu()
            } else {
                // close
                smoothCloseMenu()
                return false
            }
        }
        return true
    }

    private fun swipe(diss: Int) {
        var dis = diss
        if (!swipeEnable) {
            return
        }
        if (Math.signum(dis.toFloat()) != mSwipeDirection.toFloat()) {
            dis = 0
        } else if (Math.abs(dis) > menuView!!.width) {
            dis = menuView!!.width * mSwipeDirection
        }

        contentView!!.layout(-dis, contentView!!.top,
                contentView!!.width - dis, measuredHeight)

        if (mSwipeDirection == SwipeMenuListView.DIRECTION_LEFT) {

            menuView!!.layout(contentView!!.width - dis, menuView!!.top,
                    contentView!!.width + menuView!!.width - dis,
                    menuView!!.bottom)
        } else {
            menuView!!.layout(-menuView!!.width - dis, menuView!!.top,
                    -dis, menuView!!.bottom)
        }
    }

    override fun computeScroll() {
        if (state == STATE_OPEN) {
            if (mOpenScroller!!.computeScrollOffset()) {
                swipe(mOpenScroller!!.currX * mSwipeDirection)
                postInvalidate()
            }
        } else {
            if (mCloseScroller!!.computeScrollOffset()) {
                swipe((mBaseX - mCloseScroller!!.currX) * mSwipeDirection)
                postInvalidate()
            }
        }
    }

    fun smoothCloseMenu() {
        state = STATE_CLOSE
        if (mSwipeDirection == SwipeMenuListView.DIRECTION_LEFT) {
            mBaseX = -contentView!!.left
            mCloseScroller!!.startScroll(0, 0, menuView!!.width, 0, 350)
        } else {
            mBaseX = menuView!!.right
            mCloseScroller!!.startScroll(0, 0, menuView!!.width, 0, 350)
        }
        postInvalidate()
    }

    fun smoothOpenMenu() {
        if (!swipeEnable) {
            return
        }
        state = STATE_OPEN
        if (mSwipeDirection == SwipeMenuListView.DIRECTION_LEFT) {
            mOpenScroller!!.startScroll(-contentView!!.left, 0, menuView!!.width, 0, 350)
        } else {
            mOpenScroller!!.startScroll(contentView!!.left, 0, menuView!!.width, 0, 350)
        }
        postInvalidate()
    }

    fun closeMenu() {
        if (mCloseScroller!!.computeScrollOffset()) {
            mCloseScroller!!.abortAnimation()
        }
        if (state == STATE_OPEN) {
            state = STATE_CLOSE
            swipe(0)
        }
    }

    fun openMenu() {
        if (!swipeEnable) {
            return
        }
        if (state == STATE_CLOSE) {
            state = STATE_OPEN
            swipe(menuView!!.width * mSwipeDirection)
        }
    }

    private fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                context.resources.displayMetrics).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        menuView?.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(
                measuredHeight, View.MeasureSpec.EXACTLY))
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        contentView!!.layout(0, 0, measuredWidth,
                contentView!!.measuredHeight)
        if (mSwipeDirection == SwipeMenuListView.DIRECTION_LEFT) {
            menuView!!.layout(measuredWidth, 0,
                    measuredWidth + menuView!!.measuredWidth,
                    contentView!!.measuredHeight)
        } else {
            menuView!!.layout(-menuView!!.measuredWidth, 0,
                    0, contentView!!.measuredHeight)
        }
    }

    fun setMenuHeight(measuredHeight: Int) {
        val params = menuView!!.layoutParams as FrameLayout.LayoutParams
        if (params.height != measuredHeight) {
            params.height = measuredHeight
            menuView!!.layoutParams = params
        }
    }

    companion object {

        private val CONTENT_VIEW_ID = 1
        private val MENU_VIEW_ID = 2

        private val STATE_CLOSE = 0
        private val STATE_OPEN = 1
    }
}
