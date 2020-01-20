package org.caojun.marquee

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.layout_marquee.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class MarqueeView : LinearLayout {

    companion object {

        /** 默认滚动时间  */
        private const val ROLLING_INTERVAL_DEFAULT = 10000
        /** 第一次滚动默认延迟  */
        private const val FIRST_SCROLL_DELAY_DEFAULT = 1000
        /** 滚动模式-一直滚动  */
        private const val SCROLL_FOREVER = 100
    }

    private var text = ""
    private var maxPerLine = 15

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        LayoutInflater.from(context).inflate(R.layout.layout_marquee, this, true)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MarqueeTextView)
        val mRollingInterval = typedArray.getInt(R.styleable.MarqueeTextView_scroll_interval, ROLLING_INTERVAL_DEFAULT)
        val mScrollMode = typedArray.getInt(R.styleable.MarqueeTextView_scroll_mode, SCROLL_FOREVER)
        val mFirstScrollDelay =
            typedArray.getInt(R.styleable.MarqueeTextView_scroll_first_delay, FIRST_SCROLL_DELAY_DEFAULT)

        val mTextColor = typedArray.getInteger(R.styleable.MarqueeTextView_android_textColor, Color.WHITE)
        val mTextSize = typedArray.getDimension(R.styleable.MarqueeTextView_android_textSize, 32F)

        maxPerLine = typedArray.getInt(R.styleable.MarqueeTextView_max_per_line, 15)

        typedArray.recycle()

        tvMarquee.rndDuration = mRollingInterval
        tvMarquee.scrollMode = mScrollMode
        tvMarquee.scrollFirstDelay = mFirstScrollDelay

        tvCenter.setTextColor(mTextColor)
        tvMarquee.setTextColor(mTextColor)
        tvCenter.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
        tvMarquee.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize)
    }

    fun getText(): String {
        return text
    }

    fun setText(resId: Int) {
        val text = context.getString(resId)
        setText(text)
    }

    fun setText(text: String) {
        if (this.text == text) {
            return
        }
        this.text = text
        tvCenter.text = text
        tvCenter.visibility = View.VISIBLE
        tvMarquee.visibility = View.GONE

        var count = 100
        doAsync {
            uiThread {
                while (width == 0) {
                    count --
                    if (count < 0) {
                        break
                    }
                }
                val textWidth = calculateScrollingLen()
                val lines = tvCenter.lineCount
                if (text.length < maxPerLine || lines == 1 || textWidth < width) {
                    tvCenter.visibility = View.VISIBLE
                    tvMarquee.visibility = View.GONE
                    tvMarquee.text = ""
                    tvMarquee.stopScroll()
                } else if (tvMarquee.text.toString() != text) {
                    tvMarquee.text = text
                    tvMarquee.visibility = View.VISIBLE
                    tvCenter.visibility = View.GONE
                    tvMarquee.startScroll()
                }
            }
        }
    }

    private fun calculateScrollingLen(): Int {
        val tp = tvCenter.paint
        val rect = Rect()
        val strTxt = tvCenter.text.toString()
        tp.getTextBounds(strTxt, 0, strTxt.length, rect)
        return rect.width()
    }
}