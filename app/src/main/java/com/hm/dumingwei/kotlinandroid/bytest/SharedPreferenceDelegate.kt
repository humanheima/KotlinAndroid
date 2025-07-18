package com.hm.dumingwei.kotlinandroid.bytest

import android.content.Context
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by p_dmweidu on 2025/7/18
 * Desc: SharedPreferenceDelegate 代理方式
 */
class SharedPreferenceDelegate<T : Any>(
    private val context: Context,
    private val key: String,
    private val defaultValue: T
) : ReadWriteProperty<Any, T> {

    private val prefName: String = "MyPrefs"

    private val prefs by lazy {
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return when (defaultValue) {
            is String -> prefs.getString(key, defaultValue) as T
            is Int -> prefs.getInt(key, defaultValue) as T
            is Boolean -> prefs.getBoolean(key, defaultValue) as T
            is Float -> prefs.getFloat(key, defaultValue) as T
            is Long -> prefs.getLong(key, defaultValue) as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        with(prefs.edit()) {
            when (value) {
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Long -> putLong(key, value)
                else -> throw IllegalArgumentException("Unsupported type")
            }
            apply()
        }
    }
}

// 示例使用
class UserSettings(context: Context) {
    var userName: String by SharedPreferenceDelegate(context, "user_name", "")
    var userAge: Int by SharedPreferenceDelegate(context, "user_age", 18)
    var isDarkMode: Boolean by SharedPreferenceDelegate(context, "is_dark_mode", false)
}
