package com.ismail_s.jtime.android

import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.ismail_s.jtime.android.pojo.MasjidPojo
import com.ismail_s.jtime.android.pojo.SalaahTimePojo
import com.ismail_s.jtime.android.pojo.SalaahType
import com.ismail_s.jtime.android.pojo.charToSalaahType
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.reject
import nl.komponents.kovenant.resolve
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.NoRouteToHostException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper class for talking to the rest server asynchronously.
 */
class RestClient {
    private var sharedPrefs: SharedPreferencesWrapper
    private var context: Context
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    private val noNetworkException: NoRouteToHostException
        get() = NoRouteToHostException(context.getString(R.string.no_network_exception))

    constructor(context: Context) {
        this.context = context
        this.sharedPrefs = SharedPreferencesWrapper(context)

        if ((FuelManager.instance.baseHeaders == emptyMap<String, String>()
                || FuelManager.instance.baseHeaders == null)
                && sharedPrefs.accessToken != "") {
            setHttpHeaders(sharedPrefs.accessToken)
        }
    }

    private fun setHttpHeaders(accessToken: String) {
        FuelManager.instance.baseHeaders = mapOf("Authorization" to accessToken,
                "Accept" to "application/json",
                "Content-Type" to "application/json")
    }

    /**
     * Check if we can connect to the internet. Does not guarantee we can connect to the rest
     * server.
     */
    fun internetIsAvailable(): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        return if (networkInfo != null && networkInfo.isConnected) true else false
    }

    /**
     * Get a list of all the masjids on the rest server.
     */
    fun getMasjids(): Promise<List<MasjidPojo>, Throwable> {
        val deferred = deferred<List<MasjidPojo>, Throwable>()
        if (!internetIsAvailable()) {
            deferred reject noNetworkException
            return deferred.promise
        }
        "${Companion.url}/Masjids".httpGet().responseJson { request, response, result ->
            when (result) {
                is Result.Failure -> deferred reject result.getAs<FuelError>()!!
                is Result.Success -> {
                    val data = result.get().array()
                    val res = mutableListOf<MasjidPojo>()
                    for (m in data.iterator<JSONObject>()) {
                        val name = m.getString("name")
                        val address: String
                        try {
                            address = m.getString("humanReadableAddress")
                        } catch (e: JSONException) {
                            address = ""
                        }
                        val id = m.getInt("id")
                        val location = m.getJSONObject("location")
                        val latitude = location["lat"].toString().toDouble()
                        val longitude = location["lng"].toString().toDouble()
                        res.add(MasjidPojo(name, id, address, latitude, longitude))
                    }
                    deferred resolve res
                }
            }
        }
        return deferred.promise
    }

    /**
     * Get the salaah times for a particular masjid, for a particular date.
     */
    fun getMasjidTimes(masjidId: Int, date: GregorianCalendar): Promise<MasjidPojo, Throwable> {
        val deferred = deferred<MasjidPojo, Throwable>()
        if (!internetIsAvailable()) {
            deferred reject noNetworkException
            return deferred.promise
        }
        "${Companion.url}/Masjids/$masjidId/times"
                .httpGet(listOf("date" to dateFormatter.format(date.time)))
                .responseJson { request, response, result ->
                    when (result) {
                        is Result.Failure -> deferred reject result.getAs<FuelError>()!!
                        is Result.Success -> {
                            val times = result.get().obj().getJSONArray("times")
                            val res = MasjidPojo()
                            for (time in times.iterator<JSONObject>()) {
                                val type = time.getString("type")
                                val datetimeStr = time.getString("datetime")
                                val datetime = GregorianCalendar()
                                datetime.time = dateFormatter.parse(datetimeStr)
                                when (type) {
                                    "f" -> res.fajrTime = datetime
                                    "z" -> res.zoharTime = datetime
                                    "a" -> res.asrTime = datetime
                                    "m" -> {
                                        datetime.add(Calendar.MINUTE, 5)
                                        res.magribTime = datetime
                                    }
                                    "e" -> res.eshaTime = datetime
                                }
                            }
                            deferred resolve res
                        }
                    }
                }
        return deferred.promise
    }

    /**
     * Get a list of salaah times for masjids nearby to the given [latitude] and [longitude].
     *
     * @param salaahType the type of salaah times to be returned, or all types if not specified.
     */
    fun getTimesForNearbyMasjids(latitude: Double, longitude: Double, salaahType: SalaahType? = null)
            : Promise<List<SalaahTimePojo>, Throwable> {
        val deferred = deferred<List<SalaahTimePojo>, Throwable>()
        val loc = JSONObject().put("lat", latitude).put("lng", longitude)
        val params: MutableList<Pair<String, Any>> = mutableListOf("location" to loc.toString(),
            "date" to dateFormatter.format(GregorianCalendar().time))
        if (salaahType != null)
            params.add("salaahType" to salaahType.apiRef)
        "${Companion.url}/SalaahTimes/times-for-multiple-masjids"
                .httpGet(params)
                .responseJson { request, response, result ->
                    when (result) {
                        is Result.Failure -> { deferred reject result.error }
                        is Result.Success -> {
                            val times = result.value.obj().getJSONArray("res")
                            val res = mutableListOf<SalaahTimePojo>()
                            for (time in times.iterator<JSONObject>()) {
                                val type = charToSalaahType(time.getString("type")[0])
                                val masjidId = time.getInt("masjidId")
                                val masjidName = time.getString("masjidName")
                                val masjidLocation = time.getJSONObject("masjidLocation")
                                val masjidLoc = Location("")
                                masjidLoc.latitude = masjidLocation.getDouble("lat")
                                masjidLoc.longitude = masjidLocation.getDouble("lng")
                                val datetimeStr = time.getString("datetime")
                                val datetime = GregorianCalendar()
                                datetime.time = dateFormatter.parse(datetimeStr)
                                if (type == SalaahType.MAGRIB)
                                    datetime.add(Calendar.MINUTE, 5)
                                res += SalaahTimePojo(masjidId, masjidName, masjidLoc, type, datetime)
                            }
                            deferred resolve res
                        }
                    }
                }
        return deferred.promise
    }

    /**
     * Create a Masjid with the given [name], at the given [latitude] and [longitude].
     */
    fun createMasjid(name: String, latitude: Double, longitude: Double): Promise<Unit, Throwable> {
        val deferred = deferred<Unit, Throwable>()
        val body = JSONObject()
        val loc = JSONObject()
        loc.put("lat", latitude)
        loc.put("lng", longitude)
        body.put("name", name)
        body.put("location", loc)
        val url = Companion.url + "/Masjids"
        url.httpPost().body(body.toString()).responseJson { request, response, result ->
            when (result) {
                is Result.Failure -> { deferred reject result.getAs<FuelError>()!! }
                is Result.Success -> { deferred.resolve() }
            }
        }
        return deferred.promise
    }

    /**
     * Add/update a salaah time on the rest server.
     *
     * @param masjidId the id of the masjid the salaah time is for
     */
    fun createOrUpdateMasjidTime(masjidId: Int, salaahType: SalaahType, date: GregorianCalendar)
            : Promise<Unit, Throwable>{
        val deferred = deferred<Unit, Throwable>()
        val fuelInstance = FuelManager.instance
        val type = salaahType.apiRef
        val datetime = dateFormatter.format(date.time)
        val url = Companion.url + "/SalaahTimes/create-or-update"
        fuelInstance.baseHeaders = fuelInstance.baseHeaders?.plus(mapOf("Content-Type" to "application/x-www-form-urlencoded"))
        url.httpPost(listOf("masjidId" to "$masjidId", "type" to type, "datetime" to datetime))
                .responseJson { request, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            deferred reject result.getAs<FuelError>()!!
                            fuelInstance.baseHeaders = fuelInstance.baseHeaders?.plus(mapOf("Content-Type" to "application/json"))
                        }
                        is Result.Success -> {
                            deferred.resolve()
                            fuelInstance.baseHeaders = fuelInstance.baseHeaders?.plus(mapOf("Content-Type" to "application/json"))
                        }
                    }
                }
        return deferred.promise
    }

    /**
     * Login on the rest server.
     *
     * @param code the secure id token returned by Google Play Services
     * @param email the email address of the user logging in
     */
    fun login(code: String, email: String): Promise<Pair<Int, String>, Throwable> {
        val deferred = deferred<Pair<Int, String>, Throwable>()
        if (!internetIsAvailable()) {
            deferred reject noNetworkException
            return deferred.promise
        }
        val url = Companion.url + "/user_tables/googleid"

        url.httpGet(listOf("id_token" to code)).responseJson { request, response, result ->
            when (result) {
                is Result.Failure -> { deferred reject result.getAs<FuelError>()!! }
                is Result.Success -> {
                    val data = result.get().obj()
                    val accessToken = data.getString("access_token")
                    val id = data.getInt("userId")
                    setHttpHeaders(accessToken)
                    sharedPrefs.accessToken = accessToken
                    sharedPrefs.userId = id
                    sharedPrefs.email = email

                    deferred resolve Pair(id, accessToken)
                }
            }
        }
        return deferred.promise
    }

    /**
     * Logout on the rest server.
     */
    fun logout(): Promise<Unit, Throwable> {
        val deferred = deferred<Unit, Throwable>()
        if (!internetIsAvailable()) {
            deferred reject noNetworkException
            return deferred.promise
        }
        val url = Companion.url + "/user_tables/logout"
        url.httpPost().responseString { request, response, result ->
            when (result) {
                is Result.Failure -> { deferred reject result.getAs<FuelError>()!! }
                is Result.Success -> {
                    if (response.httpStatusCode == 204) {
                        // Clear persisted login tokens
                        clearSavedUser()
                        deferred.resolve()
                    } else {
                        deferred reject result.getAs<FuelError>()!!
                    }
                }
            }
        }
        return deferred.promise
    }

    /**
     * Check if we have a persisted login. If we do, then query the rest server
     * to see if the login is still valid (ie the session hasn't expired).
     * If the session has expired, clear the persisted login.
     *
     * We resolve the promise if we are logged in atm, else we reject the
     * promise (including if there is no persisted login).
     */
    fun areWeStillSignedInOnServer(): Promise<Unit, Unit> {
        val deferred = deferred<Unit, Unit>()
        if (!sharedPrefs.persistedLoginExists()) {
            deferred.reject()
            return deferred.promise
        }
        val url = Companion.url + "/user_tables/${sharedPrefs.userId}"
        url.httpGet().responseJson { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    clearSavedUser()
                    deferred.reject()
                }
                is Result.Success -> {
                    if (response.httpStatusCode == 200) {
                        deferred.resolve()
                    } else {
                        clearSavedUser()
                        deferred.reject()
                    }
                }
            }
        }
        return deferred.promise
    }

    private fun clearSavedUser() {
        FuelManager.instance.baseHeaders = emptyMap()
        sharedPrefs.clearSavedUser()
    }

    companion object {
        // By having url in the companion object, we can change the url from tests
        var url = "https://jtime.ismail-s.com/api"
    }
}

/**
 * Helper function to make it easier to iterate over a [JSONArray].
 */
inline fun <reified T> JSONArray.iterator(): Iterator<T> = object : Iterator<T> {
    private var index = 0
    override fun hasNext(): Boolean {
        return index < this@iterator.length()
    }

    override fun next(): T {
        val result = this@iterator.get(index) as T
        index++
        return result
    }
}
