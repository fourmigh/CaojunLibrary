package org.caojun.utils

import android.content.Context
import android.support.v4.content.ContextCompat

object ResourcesUtils {

    fun getColor(context: Context, resId: Int): Int {
        return ContextCompat.getColor(context, resId)
    }

    fun getId(context: Context, resName: String, defValue: Int): Int {
        return getResId(context, "id", resName, defValue)
    }

    fun getStringId(context: Context, resName: String, defValue: Int): Int {
        return getResId(context, "string", resName, defValue)
    }

    fun getDrawableId(context: Context, resName: String, defValue: Int): Int {
        return getResId(context, "drawable", resName, defValue)
    }

    fun getLayoutId(context: Context, resName: String, defValue: Int): Int {
        return getResId(context, "layout", resName, defValue)
    }

    fun getStyleId(context: Context, resName: String, defValue: Int): Int {
        return getResId(context, "style", resName, defValue)
    }

    fun getColorId(context: Context, resName: String, defValue: Int): Int {
        return getResId(context, "color", resName, defValue)
    }

    fun getDimenId(context: Context, resName: String, defValue: Int): Int {
        return getResId(context, "dimen", resName, defValue)
    }

    fun getAnimId(context: Context, resName: String, defValue: Int): Int {
        return getResId(context, "anim", resName, defValue)
    }

    fun getMenuId(context: Context, resName: String, defValue: Int): Int {
        return getResId(context, "menu", resName, defValue)
    }

    private fun getResId(context: Context, type: String, resName: String, defValue: Int): Int {
        val value = context.resources.getIdentifier(resName, type, context.packageName)
        return if (value == 0) defValue else value
    }

    fun getDimensionPixelOffset(context: Context, resName: String, defValue: Int): Int {
        val resId = getDimenId(context, resName, defValue)
        return context.resources.getDimensionPixelOffset(resId)
    }

    fun getDimensionPixelOffset(context: Context, resId: Int): Int {
        return context.resources.getDimensionPixelOffset(resId)
    }

    fun getStringArray(context: Context, resId: Int): Array<String>? {
        return try {
            context.resources.getStringArray(resId)
        } catch (e: Exception) {
            null
        }
    }
}