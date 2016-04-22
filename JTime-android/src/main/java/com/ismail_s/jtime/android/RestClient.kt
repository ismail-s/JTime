package com.ismail_s.jtime.android

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Manager
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.loopj.android.http.RequestParams
import com.strongloop.android.loopback.Model
import com.strongloop.android.loopback.ModelRepository
import com.strongloop.android.loopback.RestAdapter
import com.strongloop.android.loopback.callbacks.ListCallback
import com.strongloop.android.remoting.adapters.Adapter
import com.strongloop.android.remoting.adapters.RestContractItem
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.net.NoRouteToHostException
import java.util.*

class RestClient {
    private var sharedPrefs: SharedPreferencesWrapper
    private var context: Context
    private var restAdapter: RestAdapter
    private var masjidRepo: ModelRepository<Model>
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private val noNetworkException = NoRouteToHostException("Network is not available")

    constructor(context: Context) {
        this.context = context
        this.restAdapter = RestAdapter(context, Companion.url)
        this.sharedPrefs = SharedPreferencesWrapper(context)
        this.masjidRepo = this.restAdapter.createRepository("Masjid")
        this.restAdapter.contract.addItem(RestContractItem("/Masjids/:id/times-for-today", "GET"), "Masjid.getTodayTimes")
        this.restAdapter.contract.addItem(RestContractItem("/Masjids/:id/times", "GET"), "Masjid.getTimes")

        if ((Manager.instance.baseHeaders == emptyMap<String, String>() || Manager.instance.baseHeaders == null) && sharedPrefs.accessToken != "") {
            Manager.instance.baseHeaders = mapOf("Authorization" to sharedPrefs.accessToken)
        }
    }

    fun internetIsAvailable(): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected()) {
            return true
        } else {
            return false
        }
    }

    fun getMasjids(cb: MasjidsCallback) {
        if (!internetIsAvailable()) {
            cb.onError(noNetworkException)
        }
        this.masjidRepo.findAll(object: ListCallback<Model> {
            override fun onSuccess(masjids: List<Model>) {
                var res = mutableListOf<MasjidPojo>()
                for (m in masjids) {
                    val name = m.get("name") as String
                    val id = m.id as Int
                    res.add(MasjidPojo(name, id))
                }
                cb.onSuccess(res)
            }

            override fun onError(t: Throwable) = cb.onError(t)
        })
    }

    fun getMasjidTimes(masjidId: Int, cb: MasjidTimesCallback, date: GregorianCalendar) {
        if (!internetIsAvailable()) {
            cb.onError(noNetworkException)
        }
        val map = hashMapOf(Pair("id", masjidId), Pair("date", dateFormatter.format(date.time)))
        this.masjidRepo.invokeStaticMethod("getTimes", map, object : Adapter.JsonObjectCallback() {
            override fun onSuccess(response: JSONObject) {
                val times = response.getJSONArray("times")
                var res = MasjidPojo()
                for (i in 0..times.length()-1) {
                    val type = (times[i] as JSONObject).getString("type")
                    val datetimeStr = (times[i] as JSONObject).getString("datetime")
                    var datetime = GregorianCalendar()
                    datetime.time = dateFormatter.parse(datetimeStr)
                    when (type) {
                        "f" -> res.fajrTime = datetime
                        "z" -> res.zoharTime = datetime
                        "a" -> res.asrTime = datetime
                        "m" -> res.magribTime = datetime
                        "e" -> res.eshaTime = datetime
                    }
                }
                cb.onSuccess(res)
            }

            override fun onError(t: Throwable) = cb.onError(t)
        })
    }

    fun login(code: String, cb: LoginCallback) {
        if (!internetIsAvailable()) {
            cb.onError(noNetworkException)
        }
        val requestParams = RequestParams()
        requestParams.put("code", code)
        val url = Companion.url.substringBeforeLast('/') + "/auth/google/callback"

        Fuel.get(url, listOf("code" to code)).responseJson { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    cb.onError(result.getAs<FuelError>()!!)
                }
                is Result.Success -> {
                    val data = result.get()
                    val accessToken = data.getString("access_token")
                    val id = data.getInt("userId")
                    restAdapter.setAccessToken(accessToken)
                    Manager.instance.baseHeaders = mapOf("Authorization" to accessToken)
                    sharedPrefs.accessToken = accessToken
                    sharedPrefs.userId = id

                    cb.onSuccess(id, accessToken)
                }
            }
        }
    }

    fun logout(cb: LogoutCallback) {
        if (!internetIsAvailable()) {
            cb.onError(noNetworkException)
        }
        val url = Companion.url + "/users/logout"
        url.httpPost().responseString { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    cb.onError(result.getAs<FuelError>()!!)
                }
                is Result.Success -> {
                    if (response.httpStatusCode == 204) {
                        // Clear persisted login tokens
                        clearSavedUser()
                        cb.onSuccess()
                    }
                    else {
                        cb.onError(result.getAs<FuelError>()!!)
                    }
                }
            }
        }
    }

    /**
     * Check if we have a persisted login. If we do, then query the rest server
     * to see if the login is still valid (ie the session hasn't expired).
     * If the session has expired, clear the persisted login.
     *
     * cb.onLoggedIn is called if we are logged in atm, else cb.OnLoggedOut
     * is called (including if there is no persisted login).
     */
    fun checkIfStillSignedInOnServer(cb: SignedinCallback) {
        if (sharedPrefs.accessToken == "" || sharedPrefs.userId == -1) {
            // We don't have a persisted login
            cb.onLoggedOut()
            return
        }
        var url = Companion.url + "/users/${sharedPrefs.userId}"
        url.httpGet().responseJson {request, response, result ->
            when (result) {
                is Result.Failure -> {
                    clearSavedUser()
                    cb.onLoggedOut()
                }
                is Result.Success -> {
                    if (response.httpStatusCode == 200) {
                        cb.onLoggedIn()
                    }
                    else {
                        clearSavedUser()
                        cb.onLoggedOut()
                    }
                }
            }
        }
    }

    private fun clearSavedUser() {
        restAdapter.clearAccessToken()
        Manager.instance.baseHeaders = emptyMap()
        sharedPrefs.clearSavedUser()
    }

    interface Callback {
        fun onError(t: Throwable)
    }

    interface MasjidTimesCallback: Callback {
        fun onSuccess(times: MasjidPojo)
    }

    interface MasjidsCallback: Callback {
        fun onSuccess(masjids: List<MasjidPojo>)
    }

    interface LoginCallback: Callback {
        fun onSuccess(id: Int, accessToken: String)
    }

    interface LogoutCallback: Callback {
        fun onSuccess()
    }

    interface SignedinCallback {
        fun onLoggedIn()
        fun onLoggedOut()
    }

    companion object {
        // By having url in the companion object, we can change the url from tests
        var url = "http://ismail-laptop:3000/api"
    }
}
