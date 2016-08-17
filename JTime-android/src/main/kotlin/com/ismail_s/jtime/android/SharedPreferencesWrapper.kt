package com.ismail_s.jtime.android

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesWrapper {
    private var context: Context
    private var SHARED_PREFERENCES_NAME = "com.ismail_s.jtime.android.SharedPreferencesWrapper"
    private var PROPERTY_ACCESS_TOKEN = "ACCESS_TOKEN"
    private var PROPERTY_CURRENT_USER_ID = "CURRENT_USER_ID"
    private var PROPERTY_USER_EMAIL = "CURRENT_USER_EMAIL"
    var accessToken: String
        get() = sharedPrefs?.getString(PROPERTY_ACCESS_TOKEN, "") as String
        set(value) {
            sharedPrefs?.edit()?.putString(PROPERTY_ACCESS_TOKEN, value)?.apply()
        }

    var userId: Int
        get() = sharedPrefs?.getInt(PROPERTY_CURRENT_USER_ID, -1) as Int
        set(value) {
            sharedPrefs?.edit()?.putInt(PROPERTY_CURRENT_USER_ID, value)?.apply()
        }

    var email: String
        get() = sharedPrefs?.getString(PROPERTY_USER_EMAIL, "") as String
        set(value) {
            sharedPrefs?.edit()?.putString(PROPERTY_USER_EMAIL, value)?.apply()
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

    fun persistedLoginExists(): Boolean {
        if (this.accessToken == "" || this.userId == -1) {
            // We don't have a persisted login
            return false
        }
        return true
    }
}
