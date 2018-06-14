package org.caojun.velocimeter.painter.digital

import org.caojun.velocimeter.painter.Painter

/**
 * 数字
 * Created by CaoJun on 2017/9/11.
 */
interface Digital: Painter {
    fun setValue(value: Float)
}