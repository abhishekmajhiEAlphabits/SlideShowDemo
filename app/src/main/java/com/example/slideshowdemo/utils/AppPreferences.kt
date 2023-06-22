package com.example.slideshowdemo.utils

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {

    var sharedPreference: SharedPreferences? = null

    companion object {
        const val PREF_NAME = "tvApp"
        const val DEFAULT_LAUNCHER = "launcher"
    }

    init {
        if (sharedPreference == null)
            sharedPreference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveKeyValue(value: String, key: String) {
        if (sharedPreference == null) return
        sharedPreference!!.edit().putString(key, value).apply()
    }

    fun retrieveValueByKey(key: String, default: String): String {
        return sharedPreference?.getString(key, default)!!
    }

}