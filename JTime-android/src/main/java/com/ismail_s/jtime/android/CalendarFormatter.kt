package com.ismail_s.jtime.android

import java.text.SimpleDateFormat
import java.util.*

fun formatCalendarAsTime(calendar: GregorianCalendar): String {
    return formatCalendar(calendar, "HH:mm")
}

fun formatCalendarAsDate(calendar: GregorianCalendar): String {
    return formatCalendar(calendar, "yyyy MMM dd")
}

fun formatCalendar(calendar: GregorianCalendar, formatString: String): String {
    val formatter = SimpleDateFormat(formatString, Locale.getDefault())
    return formatter.format(calendar.time)
}
