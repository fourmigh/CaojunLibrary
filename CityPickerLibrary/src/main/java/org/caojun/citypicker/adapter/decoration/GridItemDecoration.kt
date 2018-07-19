package org.caojun.citypicker.adapter.decoration

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

class GridItemDecoration(spanCount: Int, space: Int) : RecyclerView.ItemDecoration() {

    private val mSpanCount: Int = spanCount
    private val mSpace: Int = space

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % mSpanCount

        outRect.left = column * mSpace / mSpanCount
        outRect.right = mSpace - (column + 1) * mSpace / mSpanCount
        if (position >= mSpanCount) {
            outRect.top = mSpace
        }
    }
}