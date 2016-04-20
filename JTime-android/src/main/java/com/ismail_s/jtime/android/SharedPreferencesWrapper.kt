package com.ismail_s.jtime.android

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesWrapper {
    private var context: Context
    private var SHARED_PREFERENCES_NAME = SharedPreferencesWrapper::class.qualifiedName
    private var PROPERTY_ACCESS_TOKEN = "ACCESS_TOKEN"
    private var PROPERTY_CURRENT_USER_ID = "CURRENT_USER_ID"
    var accessToken: String
        get() = sharedPrefs?.getString(PROPERTY_ACCESS_TOKEN, "") as String
        set(value) {
            sharedPrefs?.edit()?.putString(PROPERTY_ACCESS_TOKEN, value)?.apply()
        }

    var userId: Int
        get() = sharedPrefs?.getInt(PROPERTY_CURRENT_USER_ID, -1) as String
        set(value) {
            sharedPrefs?.edit()?.putInt(PROPERTY_CURRENT_USER_ID, value)?.apply()
        }

    val sharedPrefs: SharedPreferences?
        get() = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    constructor(context: Context) {
        this.context = context
    }

    fun clearSavedUser() {
        accessToken = ""
        userId = -1
    }
}
