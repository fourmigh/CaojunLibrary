package org.caojun.giraffeplayer

import java.io.Serializable

/**
 * Created by tcking on 2017
 */

class Option private constructor(val category: Int, val name: String?, val value: Any?) : Serializable, Cloneable {

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val option = o as Option?

        if (category != option!!.category) return false
        if (if (name != null) name != option.name else option.name != null) return false
        return if (value != null) value == option.value else option.value == null

    }

    override fun hashCode(): Int {
        var result = category
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (value?.hashCode() ?: 0)
        return result
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Option {
        return super.clone() as Option
    }

    companion object {

        fun create(category: Int, name: String, value: String): Option {
            return Option(category, name, value)
        }

        fun create(category: Int, name: String, value: Long?): Option {
            return Option(category, name, value)
        }
    }
}
