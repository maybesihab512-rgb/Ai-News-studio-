package com.ainewsstudio.data

import android.content.Context
import android.content.SharedPreferences
import java.io.File

/** Tiny wrapper over SharedPreferences. Values stay on the device and survive restarts and updates. */
object Prefs {
    private var sp: SharedPreferences? = null
    private var ctx: Context? = null

    fun init(context: Context) {
        ctx = context.applicationContext
        sp = ctx?.getSharedPreferences("ai_news_studio", Context.MODE_PRIVATE)
    }

    fun filesDir(): File? = ctx?.filesDir

    fun getString(key: String, default: String): String = sp?.getString(key, default) ?: default
    fun putString(key: String, value: String) {
        sp?.edit()?.putString(key, value)?.apply()
    }

    fun getBool(key: String, default: Boolean): Boolean = sp?.getBoolean(key, default) ?: default
    fun putBool(key: String, value: Boolean) {
        sp?.edit()?.putBoolean(key, value)?.apply()
    }
}
