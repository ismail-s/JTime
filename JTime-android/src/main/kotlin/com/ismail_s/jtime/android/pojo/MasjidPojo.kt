package com.ismail_s.jtime.android.pojo

import android.content.Context
import com.ismail_s.jtime.android.R
import java.util.GregorianCalendar

/**
 * Represents a masjid with some salaah times for a particular day. This class will probably be
 * changed in future as the abstraction it represents does not make complete sense.
 */
class MasjidPojo {
    var name: String? = null
    var id: Int? = null
    var address: String = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
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

    constructor(name: String, id: Int, address: String, latitude: Double, longitude: Double) {
        this.name = name
        this.id = id
        this.address = address
        this.latitude = latitude
        this.longitude = longitude
    }

    constructor()

    val times: Array<GregorianCalendar>
        get() = arrayOf(fajrTime!!, zoharTime!!, asrTime!!, magribTime!!, eshaTime!!)
}

/**
 * Salaah types that are used both on the rest server and this app.
 *
 * @property resId resource id for the string representation of this salaah type
 * @property apiRef the code used to represent this salaah type when talking to the rest server
 */
enum class SalaahType(val resId: Int, val apiRef: Char) {
    FAJR(R.string.fajr, 'f'), ZOHAR(R.string.zohar, 'z'),
    ASR(R.string.asr, 'a'), MAGRIB(R.string.magrib, 'm'), ESHA(R.string.esha, 'e');

    /**
     * Return a localised string representation of this salaah type.
     */
    fun toString(context: Context): String {
        return context.getString(resId)
    }
}

/**
 * Convert the code representing a salaah type to a [SalaahType]. If the code is invalid,
 * then [SalaahType.ESHA] is returned.
 */
fun charToSalaahType(c: Char): SalaahType = when(c) {
    'f' -> SalaahType.FAJR
    'z' -> SalaahType.ZOHAR
    'a' -> SalaahType.ASR
    'm' -> SalaahType.MAGRIB
    else -> SalaahType.ESHA
}
