package org.caojun.utils

import android.content.Context

object BuildConfigUtils {

    private fun getBuildConfigValue(context: Context, fieldName: String): Any? {
        try {
            val clazz = Class.forName(context.packageName + ".BuildConfig")
            val field = clazz.getField(fieldName)
            return field.get(null)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }
    
    fun getBoolean(context: Context, fieldName: String): Boolean? {
        return getBuildConfigValue(context, fieldName) as Boolean?
    }
}