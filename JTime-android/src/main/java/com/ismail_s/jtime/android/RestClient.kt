package com.ismail_s.jtime.android

import android.content.Context
import cz.msebera.android.httpclient.Header
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import com.loopj.android.http.RequestParams
import com.strongloop.android.loopback.Model
import com.strongloop.android.loopback.ModelRepository
import com.strongloop.android.loopback.RestAdapter
import com.strongloop.android.loopback.callbacks.ListCallback
import com.strongloop.android.remoting.adapters.Adapter
import com.strongloop.android.remoting.adapters.RestContractItem
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class RestClient {
    private var restAdapter: RestAdapter
    private var masjidRepo: ModelRepository<Model>
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

    constructor(context: Context) {
        this.restAdapter = RestAdapter(context, Companion.url)
        this.masjidRepo = this.restAdapter.createRepository("Masjid")
        this.restAdapter.contract.addItem(RestContractItem("/Masjids/:id/times-for-today", "GET"), "Masjid.getTodayTimes")
        this.restAdapter.contract.addItem(RestContractItem("/Masjids/:id/times", "GET"), "Masjid.getTimes")
    }

    fun getMasjids(cb: MasjidsCallback) {
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
        val requestParams = RequestParams()
        requestParams.put("code", code)
        val url = Companion.url.substringBeforeLast('/') + "/auth/google/callback"
        AsyncHttpClient().get(url, requestParams, object:JsonHttpResponseHandler(){
            override fun onSuccess(statusCode: Int, headers: Array<Header>, response: JSONObject) {
                val accessToken = response.getString("access_token")
                val id = response.getInt("id")
                restAdapter.setAccessToken(accessToken)
                cb.onSuccess(id, accessToken)
            }

            override fun onFailure(statusCode: Int, headers: Array<Header>, throwable: Throwable, errorResponse: JSONObject) {
                cb.onError(throwable)
            }
        })
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

    companion object {
        // By having url in the companion object, we can change the url from tests
        var url = "http://ismail-laptop:3000/api"
    }
}
