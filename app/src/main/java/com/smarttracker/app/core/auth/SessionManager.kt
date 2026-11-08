package com.smarttracker.app.core.auth

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("detex_auth_session", Context.MODE_PRIVATE)

    fun setLoggedIn(phoneNumber: String, countryCode: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_PHONE_NUMBER, phoneNumber)
            .putString(KEY_COUNTRY_CODE, countryCode)
            .apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getPhoneNumber(): String = prefs.getString(KEY_PHONE_NUMBER, "") ?: ""

    fun getCountryCode(): String = prefs.getString(KEY_COUNTRY_CODE, "+91") ?: "+91"

    fun logout() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_COUNTRY_CODE = "country_code"
    }
}
