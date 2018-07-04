package org.caojun.widget

import android.content.Context
import android.graphics.*
import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.IntDef
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import org.caojun.utils.DisplayUtils
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

class CircleProgressBar: View {

    companion object {
        private val DEFAULT_MAX = 100

        private const val LINE = 0
        private const val SOLID = 1
        private const val SOLID_LINE = 2

        private const val LINEAR = 0
        private const val RADIAL = 1
        private const val SWEEP = 2

        private val DEFAULT_START_DEGREE = -90.0f

        private val DEFAULT_LINE_COUNT = 45

        private val DEFAULT_LINE_WIDTH = 4.0f
        private val DEFAULT_PROGRESS_TEXT_SIZE = 11.0f
        private val DEFAULT_PROGRESS_STROKE_WIDTH = 1.0f

        private val COLOR_FFF2A670 = "#fff2a670"
        private val COLOR_FFD3D3D5 = "#ffe3e3e5"
    }

    private val mProgressRectF = RectF()
    private val mProgressTextRect = Rect()

    private val mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mProgressBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mProgressTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    private var mRadius: Float = 0.toFloat()
    private var mCenterX: Float = 0.toFloat()
    private var mCenterY: Float = 0.toFloat()

    private var mProgress: Int = 0
    private var mMax = DEFAULT_MAX

    //Background Color of the progress bar
    private var mBackgroundColor: Int = 0

    //Only work well in the Line Style, represents the line count of the rings included
    private var mLineCount: Int = 0
    //Only work well in the Line Style, Height of the line of the progress bar
    private var mLineWidth: Float = 0.toFloat()

    //Stroke width of the progress of the progress bar
    private var mProgressStrokeWidth: Float = 0.toFloat()
    //Text size of the progress of the progress bar
    private var mProgressTextSize: Float = 0.toFloat()

    //Start color of the progress of the progress bar
    private var mProgressStartColor: Int = 0
    //End color of the progress of the progress bar
    private var mProgressEndColor: Int = 0
    //Color of the progress value of the progress bar
    private var mProgressTextColor: Int = 0
    //Background color of the progress of the progress bar
    private var mProgressBackgroundColor: Int = 0

    //Format the current progress value to the specified format
    private var mProgressFormatter: ProgressFormatter? = DefaultProgressFormatter()

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(LINE, SOLID, SOLID_LINE)
    private annotation class Style

    //The style of the progress color
    @Style
    private var mStyle: Int = 0

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(LINEAR, RADIAL, SWEEP)
    private annotation class ShaderMode

    //The Shader of mProgressPaint
    @ShaderMode
    private var mShader: Int = 0
    //The Stroke Cap of mProgressPaint and mProgressBackgroundPaint
    private var mCap: Paint.Cap? = null

    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
        initFromAttributes(context, attrs)
        initPaint()
    }

    /**
     * Basic data initialization
     */
    private fun initFromAttributes(context: Context, attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar)

        mBackgroundColor = a.getColor(R.styleable.CircleProgressBar_background_color, Color.TRANSPARENT)

        mLineCount = a.getInt(R.styleable.CircleProgressBar_line_count, DEFAULT_LINE_COUNT)

        mStyle = a.getInt(R.styleable.CircleProgressBar_style, LINE)
        mShader = a.getInt(R.styleable.CircleProgressBar_progress_shader, LINEAR)
        mCap = if (a.hasValue(R.styleable.CircleProgressBar_progress_stroke_cap))
            Paint.Cap.values()[a.getInt(R.styleable.CircleProgressBar_progress_stroke_cap, 0)]
        else
            Paint.Cap.BUTT

        mLineWidth = a.getDimensionPixelSize(R.styleable.CircleProgressBar_line_width, DisplayUtils.dip2px(getContext(), DEFAULT_LINE_WIDTH)).toFloat()
        mProgressTextSize = a.getDimensionPixelSize(R.styleable.CircleProgressBar_progress_text_size, DisplayUtils.dip2px(getContext(), DEFAULT_PROGRESS_TEXT_SIZE)).toFloat()
        mProgressStrokeWidth = a.getDimensionPixelSize(R.styleable.CircleProgressBar_progress_stroke_width, DisplayUtils.dip2px(getContext(), DEFAULT_PROGRESS_STROKE_WIDTH)).toFloat()

        mProgressStartColor = a.getColor(R.styleable.CircleProgressBar_progress_start_color, Color.parseColor(COLOR_FFF2A670))
        mProgressEndColor = a.getColor(R.styleable.CircleProgressBar_progress_end_color, Color.parseColor(COLOR_FFF2A670))
        mProgressTextColor = a.getColor(R.styleable.CircleProgressBar_progress_text_color, Color.parseColor(COLOR_FFF2A670))
        mProgressBackgroundColor = a.getColor(R.styleable.CircleProgressBar_progress_background_color, Color.parseColor(COLOR_FFD3D3D5))

        a.recycle()
    }

    /**
     * Paint initialization
     */
    private fun initPaint() {
        mProgressTextPaint.textAlign = Paint.Align.CENTER
        mProgressTextPaint.textSize = mProgressTextSize

        mProgressPaint.style = if (mStyle == SOLID) Paint.Style.FILL else Paint.Style.STROKE
        mProgressPaint.strokeWidth = mProgressStrokeWidth
        mProgressPaint.color = mProgressStartColor
        mProgressPaint.strokeCap = mCap

        mProgressBackgroundPaint.style = if (mStyle == SOLID) Paint.Style.FILL else Paint.Style.STROKE
        mProgressBackgroundPaint.strokeWidth = mProgressStrokeWidth
        mProgressBackgroundPaint.color = mProgressBackgroundColor
        mProgressBackgroundPaint.strokeCap = mCap

        mBackgroundPaint.style = Paint.Style.FILL
        mBackgroundPaint.color = mBackgroundColor
    }

    /**
     * The progress bar color gradient,
     * need to be invoked in the [.onSizeChanged]
     */
    private fun updateProgressShader() {
        if (mProgressStartColor != mProgressEndColor) {
            var shader: Shader? = null
            when (mShader) {
                LINEAR -> shader = LinearGradient(mProgressRectF.left, mProgressRectF.top,
                        mProgressRectF.left, mProgressRectF.bottom,
                        mProgressStartColor, mProgressEndColor, Shader.TileMode.CLAMP)
                RADIAL -> shader = RadialGradient(mCenterX, mCenterY, mRadius,
                        mProgressStartColor, mProgressEndColor, Shader.TileMode.CLAMP)
                SWEEP -> {
                    //arc = radian * radius
                    val radian = mProgressStrokeWidth / Math.PI * 2.0f / mRadius
                    val rotateDegrees = (DEFAULT_START_DEGREE - if (mCap == Paint.Cap.BUTT && mStyle == SOLID_LINE) 0f else Math.toDegrees(radian).toFloat())

                    shader = SweepGradient(mCenterX, mCenterY, intArrayOf(mProgressStartColor, mProgressEndColor),
                            floatArrayOf(0.0f, 1.0f))
                    val matrix = Matrix()
                    matrix.postRotate(rotateDegrees, mCenterX, mCenterY)
                    shader.setLocalMatrix(matrix)
                }
            }

            mProgressPaint.shader = shader
        } else {
            mProgressPaint.shader = null
            mProgressPaint.color = mProgressStartColor
        }
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        drawBackground(canvas)
        drawProgress(canvas)
        drawProgressText(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        if (mBackgroundColor != Color.TRANSPARENT) {
            canvas.drawCircle(mCenterX, mCenterX, mRadius, mBackgroundPaint)
        }
    }

    private fun drawProgressText(canvas: Canvas) {
        if (mProgressFormatter == null) {
            return
        }

        val progressText = mProgressFormatter!!.format(mProgress, mMax)

        if (TextUtils.isEmpty(progressText)) {
            return
        }

        mProgressTextPaint.textSize = mProgressTextSize
        mProgressTextPaint.color = mProgressTextColor

        mProgressTextPaint.getTextBounds(progressText.toString(), 0, progressText.length, mProgressTextRect)
        canvas.drawText(progressText, 0, progressText.length, mCenterX, mCenterY + mProgressTextRect.height() / 2, mProgressTextPaint)
    }

    private fun drawProgress(canvas: Canvas) {
        when (mStyle) {
            SOLID -> drawSolidProgress(canvas)
            SOLID_LINE -> drawSolidLineProgress(canvas)
            LINE -> drawLineProgress(canvas)
            else -> drawLineProgress(canvas)
        }
    }

    /**
     * In the center of the drawing area as a reference point , rotate the canvas
     */
    private fun drawLineProgress(canvas: Canvas) {
        val unitDegrees = (2.0f * Math.PI / mLineCount).toFloat()
        val outerCircleRadius = mRadius
        val interCircleRadius = mRadius - mLineWidth

        val progressLineCount = (mProgress.toFloat() / mMax.toFloat() * mLineCount).toInt()

        for (i in 0 until mLineCount) {
            val rotateDegrees = i * unitDegrees

            val startX = mCenterX + Math.sin(rotateDegrees.toDouble()).toFloat() * interCircleRadius
            val startY = mCenterX - Math.cos(rotateDegrees.toDouble()).toFloat() * interCircleRadius

            val stopX = mCenterX + Math.sin(rotateDegrees.toDouble()).toFloat() * outerCircleRadius
            val stopY = mCenterX - Math.cos(rotateDegrees.toDouble()).toFloat() * outerCircleRadius

            if (i < progressLineCount) {
                canvas.drawLine(startX, startY, stopX, stopY, mProgressPaint)
            } else {
                canvas.drawLine(startX, startY, stopX, stopY, mProgressBackgroundPaint)
            }
        }
    }

    /**
     * Just draw arc
     */
    private fun drawSolidProgress(canvas: Canvas) {
        canvas.drawArc(mProgressRectF, DEFAULT_START_DEGREE, 360.0f, false, mProgressBackgroundPaint)
        canvas.drawArc(mProgressRectF, DEFAULT_START_DEGREE, 360.0f * mProgress / mMax, true, mProgressPaint)
    }

    /**
     * Just draw arc
     */
    private fun drawSolidLineProgress(canvas: Canvas) {
        canvas.drawArc(mProgressRectF, DEFAULT_START_DEGREE, 360.0f, false, mProgressBackgroundPaint)
        canvas.drawArc(mProgressRectF, DEFAULT_START_DEGREE, 360.0f * mProgress / mMax, false, mProgressPaint)
    }

    /**
     * When the size of CircleProgressBar changed, need to re-adjust the drawing area
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCenterX = (w / 2).toFloat()
        mCenterY = (h / 2).toFloat()

        mRadius = Math.min(mCenterX, mCenterY)
        mProgressRectF.top = mCenterY - mRadius
        mProgressRectF.bottom = mCenterY + mRadius
        mProgressRectF.left = mCenterX - mRadius
        mProgressRectF.right = mCenterX + mRadius

        updateProgressShader()

        //Prevent the progress from clipping
        mProgressRectF.inset(mProgressStrokeWidth / 2, mProgressStrokeWidth / 2)
    }

    override fun setBackgroundColor(backgroundColor: Int) {
        this.mBackgroundColor = backgroundColor
        mBackgroundPaint.color = backgroundColor
        invalidate()
    }

    fun setProgressFormatter(progressFormatter: ProgressFormatter) {
        this.mProgressFormatter = progressFormatter
        invalidate()
    }

    fun setProgressStrokeWidth(progressStrokeWidth: Float) {
        this.mProgressStrokeWidth = progressStrokeWidth
        mProgressRectF.inset(mProgressStrokeWidth / 2, mProgressStrokeWidth / 2)
        invalidate()
    }

    fun setProgressTextSize(progressTextSize: Float) {
        this.mProgressTextSize = progressTextSize
        invalidate()
    }

    fun setProgressStartColor(progressStartColor: Int) {
        this.mProgressStartColor = progressStartColor
        updateProgressShader()
        invalidate()
    }

    fun setProgressEndColor(progressEndColor: Int) {
        this.mProgressEndColor = progressEndColor
        updateProgressShader()
        invalidate()
    }

    fun setProgressTextColor(progressTextColor: Int) {
        this.mProgressTextColor = progressTextColor
        invalidate()
    }

    fun setProgressBackgroundColor(progressBackgroundColor: Int) {
        this.mProgressBackgroundColor = progressBackgroundColor
        mProgressBackgroundPaint.color = mProgressBackgroundColor
        invalidate()
    }

    fun setLineCount(lineCount: Int) {
        this.mLineCount = lineCount
        invalidate()
    }

    fun setLineWidth(lineWidth: Float) {
        this.mLineWidth = lineWidth
        invalidate()
    }

    fun setStyle(@Style style: Int) {
        this.mStyle = style
        mProgressPaint.style = if (mStyle == SOLID) Paint.Style.FILL else Paint.Style.STROKE
        mProgressBackgroundPaint.style = if (mStyle == SOLID) Paint.Style.FILL else Paint.Style.STROKE
        invalidate()
    }

    fun setShader(@ShaderMode shader: Int) {
        mShader = shader
        updateProgressShader()
        invalidate()
    }

    fun setCap(cap: Paint.Cap) {
        mCap = cap
        mProgressPaint.strokeCap = cap
        mProgressBackgroundPaint.strokeCap = cap
        invalidate()
    }

    fun setProgress(progress: Int) {
        this.mProgress = progress
        invalidate()
    }

    fun setMax(max: Int) {
        this.mMax = max
        invalidate()
    }

    fun getProgress(): Int {
        return mProgress
    }

    fun getMax(): Int {
        return mMax
    }

    interface ProgressFormatter {
        fun format(progress: Int, max: Int): CharSequence
    }

    private class DefaultProgressFormatter : ProgressFormatter {

        override fun format(progress: Int, max: Int): CharSequence {
            return String.format(DEFAULT_PATTERN, (progress.toFloat() / max.toFloat() * 100).toInt())
        }

        companion object {
            private val DEFAULT_PATTERN = "%d%%"
        }
    }

    private class SavedState : View.BaseSavedState {
        internal var progress: Int = 0

        internal constructor(superState: Parcelable) : super(superState) {}

        private constructor(`in`: Parcel) : super(`in`) {
            progress = `in`.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(progress)
        }

        companion object {

            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    public override fun onSaveInstanceState(): Parcelable? {
        // Force our ancestor class to save its state
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)

        ss.progress = mProgress

        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)

        setProgress(ss.progress)
    }
}