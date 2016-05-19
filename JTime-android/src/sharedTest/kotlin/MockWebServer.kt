package com.ismail_s.jtime.android.MockWebServer

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
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
            if (recordedRequest.path.startsWith("/Masjids/1/times")) {
                val mockJsonResponse = """{"times": [
                {"id": 1,"type": "f","datetime": "2016-03-28T05:30:00.000Z"},
                {"id": 2,"type": "z","datetime": "2016-03-28T12:00:00.000Z"},
                {"id": 3,"type": "a","datetime": "2016-03-28T15:00:00.000Z"},
                {"id": 4,"type": "m","datetime": "2016-03-28T15:12:00.000Z"},
                {"id": 5,"type": "e","datetime": "2016-03-28T19:45:00.000Z"}
                ]}"""
                return MockResponse().setBody(mockJsonResponse)
            }
            if (recordedRequest.path.startsWith("/Masjids")) {
                val mockJsonResponse = """[
                {"id": 1, "name": "one"},
                {"id": 2, "name": "two",
                  "humanReadableAddress": "some made-up address..."}]"""
                return MockResponse().setBody(mockJsonResponse)
            }
            return MockResponse().setResponseCode(404)
        }
    })
    RestClient.url = server.url("/").toString()
    return server
}
