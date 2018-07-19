package org.caojun.citypicker.adapter

import org.caojun.citypicker.model.City

interface InnerListener {

    fun dismiss(position: Int, data: City?)
    fun locate()
}