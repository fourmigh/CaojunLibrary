package org.caojun.widget

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView

/**
 * Created by CaoJun on 2017/11/16.
 */
class AutofitTextView: TextView, AutofitHelper.OnTextSizeChangeListener {
    private var mHelper: AutofitHelper? = null

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int): super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        mHelper = AutofitHelper.create(this, attrs, defStyle).addOnTextSizeChangeListener(this)
    }

    override fun setTextSize(unit: Int, size: Float) {
        super.setTextSize(unit, size)
        mHelper?.setTextSize(unit, size)
    }

    override fun setLines(lines: Int) {
        super.setLines(lines)
        mHelper?.setMaxLines(lines)
    }

    override fun setMaxLines(maxLines: Int) {
        super.setMaxLines(maxLines)
        mHelper?.setMaxLines(maxLines)
    }

    fun getAutofitHelper(): AutofitHelper? = mHelper

    fun isSizeToFit(): Boolean = mHelper?.isEnabled()?:false

    fun setSizeToFit() {
        setSizeToFit(true)
    }

    fun setSizeToFit(sizeToFit: Boolean) {
        mHelper?.setEnabled(sizeToFit)
    }

    fun getMaxTextSize(): Float = mHelper?.getMaxTextSize()?:0f

    fun setMaxTextSize(size: Float) {
        mHelper?.setMaxTextSize(size)
    }

    fun setMaxTextSize(unit: Int, size: Float) {
        mHelper?.setMaxTextSize(unit, size)
    }

    fun getMinTextSize(): Float = mHelper?.getMinTextSize()?:0f

    fun setMinTextSize(minSize: Int) {
        mHelper?.setMinTextSize(TypedValue.COMPLEX_UNIT_SP, minSize.toFloat())
    }

    fun setMinTextSize(unit: Int, minSize: Float) {
        mHelper?.setMinTextSize(unit, minSize)
    }

    fun getPrecision(): Float = mHelper?.getPrecision()?:0f

    fun setPrecision(precision: Float) {
        mHelper?.setPrecision(precision)
    }

    override fun onTextSizeChange(textSize: Float, oldTextSize: Float) {
        // do nothing
    }
}