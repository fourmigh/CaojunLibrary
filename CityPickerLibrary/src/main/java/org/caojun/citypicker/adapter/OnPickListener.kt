package org.caojun.citypicker.adapter

import org.caojun.citypicker.model.City

interface OnPickListener {

    fun onPick(position: Int, data: City?)
    fun onLocate()
}