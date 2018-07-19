package org.caojun.citypicker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import org.caojun.citypicker.R
import java.util.*

class SideIndexBar: View {

    private val DEFAULT_INDEX_ITEMS = arrayOf("定位", "热门", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#")

    private var mIndexItems: MutableList<String>? = null
    private var mItemHeight: Float = 0.toFloat() //每个index的高度
    private var mTextSize: Int = 0      //sp
    private var mTextColor: Int = 0
    private var mTextTouchedColor: Int = 0
    private var mCurrentIndex = -1

    private var mPaint: Paint? = null
    private var mTouchedPaint: Paint? = null

    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mTopMargin: Float = 0.toFloat()   //居中绘制，文字绘制起点和控件顶部的间隔

    private var mOverlayTextView: TextView? = null
    private var mOnIndexChangedListener: OnIndexTouchedChangedListener? = null

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        mIndexItems = ArrayList()
        mIndexItems!!.addAll(Arrays.asList(*DEFAULT_INDEX_ITEMS))

        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.cpIndexBarTextSize, typedValue, true)
        mTextSize = context.resources.getDimensionPixelSize(typedValue.resourceId)

        context.theme.resolveAttribute(R.attr.cpIndexBarNormalTextColor, typedValue, true)
        mTextColor = context.resources.getColor(typedValue.resourceId)

        context.theme.resolveAttribute(R.attr.cpIndexBarSelectedTextColor, typedValue, true)
        mTextTouchedColor = context.resources.getColor(typedValue.resourceId)

        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.textSize = mTextSize.toFloat()
        mPaint!!.color = mTextColor

        mTouchedPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mTouchedPaint!!.textSize = mTextSize.toFloat()
        mTouchedPaint!!.color = mTextTouchedColor
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var index: String
        for (i in mIndexItems!!.indices) {
            index = mIndexItems!![i]
            val fm = mPaint!!.fontMetrics
            canvas.drawText(index,
                    (mWidth - mPaint!!.measureText(index)) / 2,
                    mItemHeight / 2 + (fm.bottom - fm.top) / 2 - fm.bottom + mItemHeight * i + mTopMargin,
                    if (i == mCurrentIndex) mTouchedPaint else mPaint)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = width
        mHeight = Math.max(h, oldh)
        mItemHeight = (mHeight / mIndexItems!!.size).toFloat()
        mTopMargin = (mHeight - mItemHeight * mIndexItems!!.size) / 2
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        performClick()
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val y = event.y
                val indexSize = mIndexItems!!.size
                var touchedIndex = (y / mItemHeight).toInt()
                if (touchedIndex < 0) {
                    touchedIndex = 0
                } else if (touchedIndex >= indexSize) {
                    touchedIndex = indexSize - 1
                }
                if (mOnIndexChangedListener != null && touchedIndex >= 0 && touchedIndex < indexSize) {
                    if (touchedIndex != mCurrentIndex) {
                        mCurrentIndex = touchedIndex
                        mOverlayTextView?.visibility = View.VISIBLE
                        mOverlayTextView?.text = mIndexItems!![touchedIndex]
                        mOnIndexChangedListener!!.onIndexChanged(mIndexItems!![touchedIndex], touchedIndex)
                        invalidate()
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mCurrentIndex = -1
                mOverlayTextView?.visibility = View.GONE
                invalidate()
            }
        }
        return true
    }

    fun setOverlayTextView(overlay: TextView): SideIndexBar {
        this.mOverlayTextView = overlay
        return this
    }

    fun setOnIndexChangedListener(listener: OnIndexTouchedChangedListener): SideIndexBar {
        this.mOnIndexChangedListener = listener
        return this
    }

    interface OnIndexTouchedChangedListener {
        fun onIndexChanged(index: String, position: Int)
    }
}