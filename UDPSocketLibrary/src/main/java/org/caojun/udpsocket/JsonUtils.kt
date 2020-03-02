package org.caojun.udpsocket

import com.alibaba.fastjson.JSON

object JsonUtils {

    @JvmStatic
    fun <T> fromJson(json: String, classOfT: Class<T>): T? {
        return try {
            JSON.parseObject(json, classOfT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun toJSONString(obj: Any): String {
        return JSON.toJSONString(obj)
    }
}