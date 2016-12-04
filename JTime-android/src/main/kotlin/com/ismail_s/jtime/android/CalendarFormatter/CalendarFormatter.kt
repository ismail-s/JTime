package com.ismail_s.jtime.android.CalendarFormatter

import java.text.SimpleDateFormat
import java.util.*

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
 * Format [calendar] as per [formatString], using [SimpleDateFormat].
 */
fun formatCalendar(calendar: GregorianCalendar, formatString: String): String {
    val formatter = SimpleDateFormat(formatString, Locale.getDefault())
    return formatter.format(calendar.time)
}
