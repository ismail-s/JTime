package com.ismail_s.jtime.android.pojo

import android.location.Location
import java.util.*

class SalaahTimePojo(val masjidId: Int, val type: SalaahType, val datetime: GregorianCalendar) {
    var id: Int? = null
    var masjidName: String? = null
    var masjidLoc: Location? = null

    constructor(masjidId: Int, masjidName: String, masjidLoc: Location, type: SalaahType,
                datetime: GregorianCalendar) : this(masjidId, type, datetime) {
        this.masjidName = masjidName
        this.masjidLoc = masjidLoc
    }
}