package org.caojun.calendar.listener

import org.caojun.calendar.model.CalendarDay

/**
 * Created by CaoJun on 2017/8/23.
 */
interface OnDayClickListener {
    fun onDayClick(calendarDay: CalendarDay)
}