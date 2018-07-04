package org.caojun.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet

class RoundTextView: AppCompatTextView {

    private val DEFAULT_CORNER = 5
    private val DEFAULT_ALL_CORNER = Integer.MIN_VALUE

    private var tvBgColor = Color.TRANSPARENT
    private var tvAllCorner: Float = 0.toFloat()
    private var tvTopLeftCorner = DEFAULT_CORNER.toFloat()
    private var tvTopRightCorner = DEFAULT_CORNER.toFloat()
    private var tvBottomRightCorner = DEFAULT_CORNER.toFloat()
    private var tvBottomLeftCorner = DEFAULT_CORNER.toFloat()

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        extractAttribute(context, attrs)
        setViewBackground()
    }

    private fun extractAttribute(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.TextViewCorner, 0, 0)
        try {
            tvBgColor = ta.getColor(R.styleable.TextViewCorner_bgColor, Color.TRANSPARENT)
            tvAllCorner = ta.getDimension(R.styleable.TextViewCorner_allCorner, java.lang.Float.MIN_VALUE)
            tvTopLeftCorner = ta.getDimension(R.styleable.TextViewCorner_topLeftCorner, DEFAULT_CORNER.toFloat())
            tvTopRightCorner = ta.getDimension(R.styleable.TextViewCorner_topRightCorner, DEFAULT_CORNER.toFloat())
            tvBottomRightCorner = ta.getDimension(R.styleable.TextViewCorner_bottomRightCorner, DEFAULT_CORNER.toFloat())
            tvBottomLeftCorner = ta.getDimension(R.styleable.TextViewCorner_bottomLeftCorner, DEFAULT_CORNER.toFloat())
        } finally {
            ta.recycle()
        }
    }

    fun setCorner(all: Int) {
        tvAllCorner = all.toFloat()
        setViewBackground()
    }

    fun setCorner(topLeft: Int, topRight: Int, bottomRight: Int, bottomLeft: Int) {
        tvTopLeftCorner = topLeft.toFloat()
        tvTopRightCorner = topRight.toFloat()
        tvBottomRightCorner = bottomRight.toFloat()
        tvBottomLeftCorner = bottomLeft.toFloat()
        setViewBackground()
    }

    fun setBgColor(color: Int) {
        tvBgColor = color
        setViewBackground()
    }

    private fun setViewBackground() {

        val drawable: Drawable

        if (tvAllCorner != java.lang.Float.MIN_VALUE) {
            drawable = DrawableHelper.getCornerDrawable(
                    tvAllCorner,
                    tvAllCorner,
                    tvAllCorner,
                    tvAllCorner,
                    tvBgColor)
        } else {
            drawable = DrawableHelper.getCornerDrawable(
                    tvTopLeftCorner,
                    tvTopRightCorner,
                    tvBottomLeftCorner,
                    tvBottomRightCorner,
                    tvBgColor)
        }

        DrawableHelper.setRoundBackground(this, drawable)
    }
}