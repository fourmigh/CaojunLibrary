package org.caojun.citypicker.adapter.decoration

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import org.caojun.citypicker.R
import org.caojun.citypicker.model.City

class SectionItemDecoration(context: Context, data: MutableList<City>) : RecyclerView.ItemDecoration() {

    private var mData: MutableList<City> = data
    private var mBgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var mBounds = Rect()

    private var mSectionHeight: Int
    private var mBgColor: Int
    private var mTextColor: Int
    private var mTextSize: Int

    init {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.cpSectionBackground, typedValue, true)
        mBgColor = context.resources.getColor(typedValue.resourceId)
        context.theme.resolveAttribute(R.attr.cpSectionHeight, typedValue, true)
        mSectionHeight = context.resources.getDimensionPixelSize(typedValue.resourceId)
        context.theme.resolveAttribute(R.attr.cpSectionTextSize, typedValue, true)
        mTextSize = context.resources.getDimensionPixelSize(typedValue.resourceId)
        context.theme.resolveAttribute(R.attr.cpSectionTextColor, typedValue, true)
        mTextColor = context.resources.getColor(typedValue.resourceId)

        mBgPaint.color = mBgColor

        mTextPaint.textSize = mTextSize.toFloat()
        mTextPaint.color = mTextColor
    }

    fun setData(data: MutableList<City>) {
        this.mData = data
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        super.onDraw(c, parent, state)
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child
                    .layoutParams as RecyclerView.LayoutParams
            val position = params.viewLayoutPosition
            if (!mData.isEmpty() && position <= mData.size - 1 && position > -1) {
                if (position == 0) {
                    drawSection(c, left, right, child, params, position)
                } else if (mData[position].getSection() != mData[position - 1].getSection()) {
                    drawSection(c, left, right, child, params, position)
                }
            }
        }
    }

    private fun drawSection(c: Canvas, left: Int, right: Int, child: View,
                            params: RecyclerView.LayoutParams, position: Int) {
        c.drawRect(left.toFloat(),
                (child.top - params.topMargin - mSectionHeight).toFloat(),
                right.toFloat(),
                (child.top - params.topMargin).toFloat(), mBgPaint)
        mTextPaint.getTextBounds(mData[position].getSection(),
                0,
                mData[position].getSection().length,
                mBounds)
        c.drawText(mData[position].getSection(),
                child.paddingLeft.toFloat(),
                (child.top - params.topMargin - (mSectionHeight / 2 - mBounds.height() / 2)).toFloat(),
                mTextPaint)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        val pos = (parent.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        if (pos < 0) return
        if (mData.isEmpty()) return
        val section = mData[pos].getSection()
        val child = parent.findViewHolderForLayoutPosition(pos).itemView

        var flag = false
        if (pos + 1 < mData.size) {
            if (section != mData[pos + 1].getSection()) {
                if (child.height + child.top < mSectionHeight) {
                    c.save()
                    flag = true
                    c.translate(0f, (child.height + child.top - mSectionHeight).toFloat())
                }
            }
        }
        c.drawRect(parent.paddingLeft.toFloat(),
                parent.paddingTop.toFloat(),
                (parent.right - parent.paddingRight).toFloat(),
                (parent.paddingTop + mSectionHeight).toFloat(), mBgPaint)
        mTextPaint.getTextBounds(section, 0, section.length, mBounds)
        c.drawText(section,
                child.paddingLeft.toFloat(),
                (parent.paddingTop + mSectionHeight - (mSectionHeight / 2 - mBounds.height() / 2)).toFloat(),
                mTextPaint)
        if (flag)
            c.restore()
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        val position = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        if (!mData.isEmpty() && position <= mData.size - 1 && position > -1) {
            if (position == 0) {
                outRect.set(0, mSectionHeight, 0, 0)
            } else if (mData[position].getSection() != mData[position - 1].getSection()) {
                outRect.set(0, mSectionHeight, 0, 0)
            }
        }
    }
}