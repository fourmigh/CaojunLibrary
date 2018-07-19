package org.caojun.citypicker.adapter.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import org.caojun.citypicker.R

class DividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private var dividerHeight: Float
    private var mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.cpSectionBackground, typedValue, true)
        mPaint.color = context.resources.getColor(typedValue.resourceId)
        dividerHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, context.resources.displayMetrics)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = dividerHeight.toInt()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        val childCount = parent.childCount
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (i in 0 until childCount - 1) {
            val view = parent.getChildAt(i)
            val top = view.bottom.toFloat()
            val bottom = view.bottom + dividerHeight
            c.drawRect(left.toFloat(), top, right.toFloat(), bottom, mPaint)
        }
    }
}