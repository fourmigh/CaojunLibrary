package org.caojun.roundtextview

import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.annotation.IntDef
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class BadgeView: RoundLayout {

    private var tvBadgeMessage: TextView? = null
    private var rtvBadgeAmount: RoundTextView? = null

    private var mainText: String? = null
    private var mainTextColor: Int = 0
    private var mainTextSize: Float = 0.toFloat()
    private var mainBgColor: Int = 0
    private var subText: String? = null
    private var subTextColor: Int = 0
    private var subTextSize: Float = 0.toFloat()
    private var subBgColor: Int = 0
    private var subTopLeftCorner: Int = 0
    private var subTopRightCorner: Int = 0
    private var subBottomRightCorner: Int = 0
    private var subBottomLeftCorner: Int = 0
    private var subAllCorner: Int = 0
    private var subMarginLeft: Int = 0
    private var subMarginTop: Int = 0
    private var subMarginRight: Int = 0
    private var subMarginBottom: Int = 0
    private var subAllMargin: Int = 0
    private var subPaddingLeft: Int = 0
    private var subPaddingTop: Int = 0
    private var subPaddingRight: Int = 0
    private var subPaddingBottom: Int = 0
    private var subAllPadding: Int = 0
    private var mainTopLeftCorner: Int = 0
    private var mainTopRightCorner: Int = 0
    private var mainBottomRightCorner: Int = 0
    private var mainBottomLeftCorner: Int = 0
    private var mainAllCorner: Int = 0

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        extractAttribute(context, attrs)
        initView()
    }

    private fun extractAttribute(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.BadgeView, 0, 0)
        try {
            mainText = ta.getString(R.styleable.BadgeView_mainText)
            mainTextColor = ta.getColor(R.styleable.BadgeView_mainTextColor, Color.WHITE)
            mainTextSize = ta.getDimension(R.styleable.BadgeView_mainTextSize, 24f)
            mainBgColor = ta.getColor(R.styleable.BadgeView_mainBgColor, Color.parseColor("#FFAE00"))
            mainTopLeftCorner = ta.getDimension(R.styleable.BadgeView_mainTopLeftCorner, 0f).toInt()
            mainTopRightCorner = ta.getDimension(R.styleable.BadgeView_mainTopRightCorner, 0f).toInt()
            mainBottomRightCorner = ta.getDimension(R.styleable.BadgeView_mainBottomRightCorner, 0f).toInt()
            mainBottomLeftCorner = ta.getDimension(R.styleable.BadgeView_mainBottomLeftCorner, 0f).toInt()
            mainAllCorner = ta.getDimension(R.styleable.BadgeView_mainAllCorner, Integer.MIN_VALUE.toFloat()).toInt()
            subText = ta.getString(R.styleable.BadgeView_subText)
            subTextColor = ta.getColor(R.styleable.BadgeView_subTextColor, Color.WHITE)
            subTextSize = ta.getDimension(R.styleable.BadgeView_subTextSize, 24f)
            subBgColor = ta.getColor(R.styleable.BadgeView_subBgColor, Color.parseColor("#96000000"))
            subTopLeftCorner = ta.getDimension(R.styleable.BadgeView_subTopLeftCorner, 0f).toInt()
            subTopRightCorner = ta.getDimension(R.styleable.BadgeView_subTopRightCorner, 0f).toInt()
            subBottomRightCorner = ta.getDimension(R.styleable.BadgeView_subBottomRightCorner, 0f).toInt()
            subBottomLeftCorner = ta.getDimension(R.styleable.BadgeView_subBottomLeftCorner, 0f).toInt()
            subAllCorner = ta.getDimension(R.styleable.BadgeView_subAllCorner, Integer.MIN_VALUE.toFloat()).toInt()
            subMarginLeft = ta.getDimension(R.styleable.BadgeView_subMarginLeft, 16f).toInt()
            subMarginTop = ta.getDimension(R.styleable.BadgeView_subMarginTop, 0f).toInt()
            subMarginRight = ta.getDimension(R.styleable.BadgeView_subMarginRight, 0f).toInt()
            subMarginBottom = ta.getDimension(R.styleable.BadgeView_subMarginBottom, 0f).toInt()
            subAllMargin = ta.getDimension(R.styleable.BadgeView_subMargin, Integer.MIN_VALUE.toFloat()).toInt()
            subPaddingLeft = ta.getDimension(R.styleable.BadgeView_subPaddingLeft, 16f).toInt()
            subPaddingTop = ta.getDimension(R.styleable.BadgeView_subPaddingTop, 5f).toInt()
            subPaddingRight = ta.getDimension(R.styleable.BadgeView_subPaddingRight, 16f).toInt()
            subPaddingBottom = ta.getDimension(R.styleable.BadgeView_subPaddingBottom, 5f).toInt()
            subAllPadding = ta.getDimension(R.styleable.BadgeView_subPadding, Integer.MIN_VALUE.toFloat()).toInt()
        } finally {
            ta.recycle()
        }
    }

    private fun initView() {
        inflate(context, R.layout.view_badge, this)

        tvBadgeMessage = findViewById(R.id.tvBadgeMessage)
        rtvBadgeAmount = findViewById(R.id.rtvBadgeSub)

        // default padding
        if (subText == null || subText!!.isEmpty()) {
            val dp6 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, resources.displayMetrics).toInt()
            val dp16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
            setPadding(dp16, dp6, dp16, dp6)
        } else {
            val dp4 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, resources.displayMetrics).toInt()
            val dp16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
            setPadding(dp16, dp4, dp4, dp4)
        }

        // Main
        setBadgeMainText(mainText)
        setBadgeMainTextColor(mainTextColor)
        setBadgeMainTextSize(mainTextSize)
        setBadgeMainBackgroundColor(mainBgColor)

        if (mainAllCorner != Integer.MIN_VALUE) {
            setBadgeMainCorner(mainAllCorner, mainAllCorner, mainAllCorner, mainAllCorner)
        } else {
            setBadgeMainCorner(mainTopLeftCorner, mainTopRightCorner, mainBottomRightCorner, mainBottomLeftCorner)
        }

        // Sub
        setBadgeSubText(subText)
        setBadgeSubTextColor(subTextColor)
        setBadgeSubTextSize(subTextSize)
        setBadgeSubBackgroundColor(subBgColor)

        if (subAllCorner != Integer.MIN_VALUE) {
            setBadgeSubCorner(subAllCorner, subAllCorner, subAllCorner, subAllCorner)
        } else {
            setBadgeSubCorner(subTopLeftCorner, subTopRightCorner, subBottomRightCorner, subBottomLeftCorner)
        }

        if (subAllMargin != Integer.MIN_VALUE) {
            setBadgeSubMargin(subAllMargin, subAllMargin, subAllMargin, subAllMargin)
        } else {
            setBadgeSubMargin(subMarginLeft, subMarginTop, subMarginRight, subMarginBottom)
        }

        if (subAllPadding != Integer.MIN_VALUE) {
            setBadgeSubPadding(subAllPadding, subAllPadding, subAllPadding, subAllPadding)
        } else {
            setBadgeSubPadding(subPaddingLeft, subPaddingTop, subPaddingRight, subPaddingBottom)
        }
    }

    private fun setBadgeMainCorner(topLeft: Int, topRight: Int, bottomRight: Int, bottomLeft: Int) {
        setCorner(topLeft, topRight, bottomRight, bottomLeft)
    }

    private fun setBadgeMainBackgroundColor(mainBgColor: Int) {
        setBgColor(mainBgColor)
    }

    fun getBadgeMainText(): String {
        return tvBadgeMessage!!.text.toString()
    }

    fun setBadgeMainText(text: String?) {
        tvBadgeMessage!!.text = text
    }

    private fun setBadgeMainTextSize(mainTextSize: Float) {
        tvBadgeMessage!!.textSize = mainTextSize
    }

    fun setBadgeMainTextColor(@ColorInt color: Int) {
        tvBadgeMessage!!.setTextColor(color)
    }

    fun getBadgeSubText(): String {
        return rtvBadgeAmount!!.text.toString()
    }

    fun setBadgeSubText(text: String?) {

        if (text == null || text.isEmpty()) {
            rtvBadgeAmount!!.visibility = GONE
        } else {
            rtvBadgeAmount!!.visibility = VISIBLE
            rtvBadgeAmount!!.text = text
        }
    }

    fun setBadgeSubTextColor(@ColorInt color: Int) {
        rtvBadgeAmount!!.setTextColor(color)
    }

    private fun setBadgeSubTextSize(subTextSize: Float) {
        rtvBadgeAmount!!.textSize = subTextSize
    }

    fun setBadgeSubBackgroundColor(@ColorInt color: Int) {
        rtvBadgeAmount!!.setBgColor(color)
    }

    /**
     * @hide
     */
    @IntDef(VISIBLE, INVISIBLE, GONE)
    @Retention(RetentionPolicy.SOURCE)
    annotation class Visibility

    fun setBadgeSubVisibility(@Visibility visibility: Int) {
        rtvBadgeAmount!!.visibility = visibility
    }

    fun setBadgeSubCorner(topLeft: Int, topRight: Int, bottomRight: Int, bottomLeft: Int) {
        rtvBadgeAmount!!.setCorner(topLeft, topRight, bottomRight, bottomLeft)
    }

    fun setBadgeSubMargin(left: Int, top: Int, right: Int, bottom: Int) {
        val layoutParams = rtvBadgeAmount!!.layoutParams as LinearLayout.LayoutParams
        layoutParams.setMargins(left, top, right, bottom)
        requestLayout()
    }

    fun setBadgeSubPadding(left: Int, top: Int, right: Int, bottom: Int) {
        rtvBadgeAmount!!.setPadding(left, top, right, bottom)
    }
}