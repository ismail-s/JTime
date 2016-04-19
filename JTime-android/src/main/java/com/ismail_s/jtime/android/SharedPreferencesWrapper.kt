package com.ismail_s.jtime.android

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesWrapper {
    private var context: Context
    private var SHARED_PREFERENCES_NAME = SharedPreferencesWrapper::class.qualifiedName
    private var PROPERTY_ACCESS_TOKEN = "ACCESS_TOKEN"
    var accessToken: String
        get() = sharedPrefs?.getString(PROPERTY_ACCESS_TOKEN, "") as String
        set(value) {
            sharedPrefs?.edit()?.putString(PROPERTY_ACCESS_TOKEN, value)?.apply()
        }

    val sharedPrefs: SharedPreferences?
        get() = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    constructor(context: Context) {
        this.context = context
    }

    fun clearAccessToken() {
        accessToken = ""
    }
}
