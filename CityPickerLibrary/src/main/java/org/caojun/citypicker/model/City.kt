package org.caojun.citypicker.model

import android.text.TextUtils
import java.util.regex.Pattern

open class City(name: String, province: String, pinyin: String, code: String) {

    private var name: String = name
    private var province: String = province
    private var pinyin: String = pinyin
    private var code: String = code

    /***
     * 获取悬浮栏文本，（#、定位、热门 需要特殊处理）
     * @return
     */
    fun getSection(): String {
        return if (TextUtils.isEmpty(pinyin)) {
            "#"
        } else {
            val c = pinyin.substring(0, 1)
            val p = Pattern.compile("[a-zA-Z]")
            val m = p.matcher(c)
            if (m.matches()) {
                c.toUpperCase()
            } else if (TextUtils.equals(c, "定") || TextUtils.equals(c, "热"))
                pinyin
            else
                "#"//在添加定位和热门数据时设置的section就是‘定’、’热‘开头
        }
    }

    fun getPinyin(): String {
        return pinyin
    }

    fun getName(): String {
        return name
    }

    fun getCode(): String {
        return code
    }
}