package com.ismail_s.jtime.android.pojo

import java.util.*

class SalaahTimePojo(val masjidId: Int, val type: SalaahType, val datetime: GregorianCalendar) {
    var id: Int? = null
    var masjidName: String? = null

    constructor(masjidId: Int, masjidName: String, type: SalaahType,
                datetime: GregorianCalendar) : this(masjidId, type, datetime) {
        this.masjidName = masjidName
    }
}