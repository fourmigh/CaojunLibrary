package org.caojun.giraffeplayer

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView

/**
 * Created by tcking on 2017
 */

class ScalableTextureView : TextureView, ScalableDisplay {
    private var measureHelper: MeasureHelper? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    override fun setAspectRatio(ratio: Int) {
        measureHelper?.setAspectRatio(ratio)
        requestLayout()
    }

    override fun setVideoSize(videoWidth: Int, videoHeight: Int) {
        if (videoWidth > 0 && videoHeight > 0) {
            measureHelper?.setVideoSize(videoWidth, videoHeight)
            requestLayout()
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        measureHelper = MeasureHelper(this)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureHelper?.doMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measureHelper!!.measuredWidth, measureHelper!!.measuredHeight)
    }
}
