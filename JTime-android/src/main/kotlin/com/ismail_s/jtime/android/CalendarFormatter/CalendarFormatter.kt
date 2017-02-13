package com.ismail_s.jtime.android.CalendarFormatter

import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_YEAR
import java.util.Calendar.YEAR

/**
 * Format [calendar] as _HH:mm_.
 */
fun formatCalendarAsTime(calendar: GregorianCalendar): String {
    return formatCalendar(calendar, "HH:mm")
}

/**
 * Format [calendar] as _EEE dd MMM yyyy_.
 */
fun formatCalendarAsDate(calendar: GregorianCalendar): String {
    return formatCalendar(calendar, "EEE dd MMM yyyy")
}

/**
 * Format [calendar] as _EEE dd MMM yyyy_ or "today" if the [calendar] is for today.
 */
fun formatCalendarAsTodayOrDate(calendar: GregorianCalendar): String {
    val today = GregorianCalendar()
    if (intArrayOf(DAY_OF_YEAR, YEAR).fold(true, { b, c -> b && calendar.get(c) == today.get(c)})) {
        return "today"
    } else return formatCalendarAsDate(calendar)
}

/**
 * Format [calendar] as per [formatString], using [SimpleDateFormat].
 */
fun formatCalendar(calendar: GregorianCalendar, formatString: String): String {
    val formatter = SimpleDateFormat(formatString, Locale.getDefault())
    return formatter.format(calendar.time)
}
