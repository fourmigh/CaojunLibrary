package org.caojun.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v4.graphics.drawable.DrawableCompat
import android.os.Build
import android.support.v4.content.ContextCompat
import android.graphics.BitmapFactory
import android.util.SparseArray

object BitmapUtils {

    private val hmBitmap = SparseArray<Bitmap>()

    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int, width: Int = 0, height: Int = 0): Bitmap {

        if (hmBitmap[drawableId] != null) {
            return hmBitmap[drawableId]!!
        }

        var drawable = ContextCompat.getDrawable(context, drawableId)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable!!).mutate()
        }

        val drawableWidth = if (width > 0) width else drawable!!.intrinsicWidth
        val drawableHeight = if (height > 0) height else drawable!!.intrinsicHeight

        val bitmap = Bitmap.createBitmap(
            drawableWidth, drawableHeight,
            Bitmap.Config.ARGB_4444
        )
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)

        hmBitmap.put(drawableId, bitmap)
        return bitmap
    }

    fun getBitmapFormResources(context: Context, resId: Int): Bitmap {
        return BitmapFactory.decodeResource(context.resources, resId)
    }
}