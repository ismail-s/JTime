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

    var userId: String
        get() = sharedPrefs?.getString(PROPERTY_CURRENT_USER_ID, "") as String
        set(value) {
            sharedPrefs?.edit()?.putString(PROPERTY_CURRENT_USER_ID, value)?.apply()
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
        userId = ""
    }

    fun persistedLoginExists(): Boolean {
        if (this.accessToken == "" || this.userId == "") {
            // We don't have a persisted login
            return false
        }
        return true
    }
}
