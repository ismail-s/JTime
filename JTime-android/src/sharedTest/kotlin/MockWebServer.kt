package com.ismail_s.jtime.android.MockWebServer

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import android.util.Log
import com.ismail_s.jtime.android.RestClient

/**
* Create a [MockWebServer], set it up with some default responses to
* make it look like the REST api, and set the [RestClient] to use
* our new [MockWebServer] instead of the actual REST api.
*/
fun createMockWebServerAndConnectToRestClient(): MockWebServer {
    val server = MockWebServer()
    server.setDispatcher(object : Dispatcher() {
        @Throws(InterruptedException::class)
        override fun dispatch(recordedRequest: RecordedRequest): MockResponse {
            if (recordedRequest.path.startsWith("/Masjids/1b/times")) {
                val mockJsonResponse = """{"times": [
                {"id": "1a","type": "f","datetime": "2016-03-28T05:30:00.000Z"},
                {"id": "2a","type": "z","datetime": "2016-03-28T12:00:00.000Z"},
                {"id": "3a","type": "a","datetime": "2016-03-28T15:00:00.000Z"},
                {"id": "4a","type": "m","datetime": "2016-03-28T15:12:00.000Z"},
                {"id": "5a","type": "e","datetime": "2016-03-28T19:45:00.000Z"}
                ]}"""
                return MockResponse().setBody(mockJsonResponse)
            }
            if (recordedRequest.path.startsWith("/Masjids")) {
                val mockJsonResponse = """[
                {"id": "1b", "name": "one", "location": {"lat": 0.0, "lng": 0.0}},
                {"id": "2b", "name": "two", "location": {"lng": 0.1, "lat": 0.1},
                  "humanReadableAddress": "some made-up address..."}]"""
                return MockResponse().setBody(mockJsonResponse)
            }
            Log.d("debugging", "HTTP404 for $recordedRequest")
            return MockResponse().setResponseCode(404)
        }
    })
    RestClient.url = server.url("/").toString().dropLast(1)
    Log.d("debugging", "RestClient.url is now ${RestClient.url}")
    return server
}
