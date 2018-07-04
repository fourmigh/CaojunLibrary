package org.caojun.widget

import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.support.annotation.ColorInt
import android.view.View

object DrawableHelper {

    fun setRoundBackground(view: View, drawable: Drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.background = drawable
        } else {
            view.setBackgroundDrawable(drawable)
        }
    }

    fun getCornerDrawable(topLeft: Float,
                          topRight: Float,
                          bottomLeft: Float,
                          bottomRight: Float,
                          @ColorInt color: Int): Drawable {

        val outerR = FloatArray(8)
        outerR[0] = topLeft
        outerR[1] = topLeft
        outerR[2] = topRight
        outerR[3] = topRight
        outerR[4] = bottomRight
        outerR[5] = bottomRight
        outerR[6] = bottomLeft
        outerR[7] = bottomLeft

        val drawable = ShapeDrawable()
        drawable.shape = RoundRectShape(outerR, null, null)
        drawable.paint.color = color

        return drawable
    }
}