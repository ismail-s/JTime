package com.ismail_s.jtime.android

import java.util.GregorianCalendar

class RestClient {
    fun getMasjidTimes(masjidName: String): MasjidPojo {
        val res = MasjidPojo(masjidName)
        res.fajrTime = GregorianCalendar(2015, 1, 1, 5, 30)
        res.zoharTime = GregorianCalendar(2015, 1, 1, 12, 0)
        res.asrTime = GregorianCalendar(2015, 1, 1, 15, 0)
        res.magribTime = GregorianCalendar(2015, 1, 1, 15, 12)
        res.eshaTime = GregorianCalendar(2015, 1, 1, 19, 45)
        return res
    }
}
