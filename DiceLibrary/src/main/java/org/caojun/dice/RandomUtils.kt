package org.caojun.dice

/**
 * Created by CaoJun on 2017/8/15.
 */
object RandomUtils {
    fun getRandom(min: Int, max: Int): Int {
        return (Math.random() * (max + 1 - min) + min).toInt()
    }
}