package com.ismail_s.jtime.android

import java.util.GregorianCalendar

class MasjidPojo {
    var name: String? = null
    var id: Int? = null
    var address: String = ""
    var fajrTime: GregorianCalendar? = null
    var zoharTime: GregorianCalendar? = null
    var asrTime: GregorianCalendar? = null
    var magribTime: GregorianCalendar? = null
    var eshaTime: GregorianCalendar? = null

    constructor(name: String, fajrTime: GregorianCalendar, zoharTime: GregorianCalendar, asrTime: GregorianCalendar, magribTime: GregorianCalendar, eshaTime: GregorianCalendar) {
        this.name = name
        this.fajrTime = fajrTime
        this.zoharTime = zoharTime
        this.asrTime = asrTime
        this.magribTime = magribTime
        this.eshaTime = eshaTime
    }

    constructor(name: String) {
        this.name = name
    }

    constructor(name: String, id: Int, address: String) {
        this.name = name
        this.id = id
        this.address = address
    }

    constructor()

    val times: Array<GregorianCalendar>
        get() = arrayOf(fajrTime!!, zoharTime!!, asrTime!!, magribTime!!, eshaTime!!)
}

enum class SalaahType {
    FAJR, ZOHAR, ASR, MAGRIB, ESHA
}
