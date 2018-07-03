package org.caojun.roundtextview

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout

open class RoundLayout: FrameLayout {

    private val DEFAULT_CORNER = 5
    private val DEFAULT_ALL_CORNER = Integer.MIN_VALUE

    private var bgColor = Color.TRANSPARENT
    private var allCorner = DEFAULT_ALL_CORNER.toFloat()
    private var topLeftCorner = DEFAULT_CORNER.toFloat()
    private var topRightCorner = DEFAULT_CORNER.toFloat()
    private var bottomRightCorner = DEFAULT_CORNER.toFloat()
    private var bottomLeftCorner = DEFAULT_CORNER.toFloat()

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        extractAttribute(context, attrs)
        setViewBackground()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount

        if (count > 1) {
            throw IllegalStateException("View can have only single child")
        }

        super.onLayout(changed, l, t, r, b)
    }

    private fun extractAttribute(context: Context, attrs: AttributeSet?) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.TextViewCorner, 0, 0)
        try {
            bgColor = ta.getColor(R.styleable.TextViewCorner_bgColor, Color.TRANSPARENT)
            allCorner = ta.getDimension(R.styleable.TextViewCorner_allCorner, DEFAULT_ALL_CORNER.toFloat())
            topLeftCorner = ta.getDimension(R.styleable.TextViewCorner_topLeftCorner, DEFAULT_CORNER.toFloat())
            topRightCorner = ta.getDimension(R.styleable.TextViewCorner_topRightCorner, DEFAULT_CORNER.toFloat())
            bottomRightCorner = ta.getDimension(R.styleable.TextViewCorner_bottomRightCorner, DEFAULT_CORNER.toFloat())
            bottomLeftCorner = ta.getDimension(R.styleable.TextViewCorner_bottomLeftCorner, DEFAULT_CORNER.toFloat())
        } finally {
            ta.recycle()
        }
    }

    fun setCorner(all: Int) {
        allCorner = all.toFloat()
        setViewBackground()
    }

    fun setCorner(topLeft: Int, topRight: Int, bottomRight: Int, bottomLeft: Int) {
        this.topLeftCorner = topLeft.toFloat()
        this.topRightCorner = topRight.toFloat()
        this.bottomRightCorner = bottomRight.toFloat()
        this.bottomLeftCorner = bottomLeft.toFloat()
        setViewBackground()
    }

    fun setBgColor(color: Int) {
        bgColor = color
        setViewBackground()
    }

    private fun setViewBackground() {
        val drawable: Drawable
        if (allCorner > 0) {
            drawable = DrawableHelper.getCornerDrawable(
                    allCorner,
                    allCorner,
                    allCorner,
                    allCorner,
                    bgColor)
        } else {
            drawable = DrawableHelper.getCornerDrawable(
                    topLeftCorner,
                    topRightCorner,
                    bottomLeftCorner,
                    bottomRightCorner,
                    bgColor)
        }

        DrawableHelper.setRoundBackground(this, drawable)
    }
}