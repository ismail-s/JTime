import android.content.Context

class SharedPreferences {
    private var context: Context
    private var SHARED_PREFERENCES_NAME = SharedPreferences::class.java.canonicalName
    private var PROPERTY_ACCESS_TOKEN = "ACCESS_TOKEN"
    constructor(context: Context) {
        this.context = context
    }

    fun storeAccessToken(token: String) {
        getSharedPrefs().edit().putString(PROPERTY_ACCESS_TOKEN, token).apply()
    }

    fun clearAccessToken() = storeAccessToken("")

    fun getAccessToken() = getSharedPrefs().getString(PROPERTY_ACCESS_TOKEN, "")

    fun getSharedPrefs() = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
}
