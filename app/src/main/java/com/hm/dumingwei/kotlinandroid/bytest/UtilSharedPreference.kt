package com.hm.dumingwei.kotlinandroid.bytest

import android.content.Context
import android.content.SharedPreferences
import kotlin.reflect.KProperty

/**
 * Created by p_dmweidu on 2023/6/28
 * Desc: 实战演练，SharedPreference 委托
 */
class UtilSharedPreference<T>(private val key: String, private val default: T) {

    companion object {

        lateinit var sp: SharedPreferences

        fun initSharedPreference(context: Context, fileName: String) {
            sp = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Any) {
        sp.edit().apply {
            when (value) {
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                is Int -> putInt(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Set<*> -> putStringSet(key, value as Set<String>) // only support Set<String>
                else -> throw IllegalArgumentException("SharedPreferences can't be save this type")
            }.commit()
        }
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        sp.apply {
            val res: Any = when (default) {
                is Long -> getLong(key, default)
                is String -> getString(key, default) ?: ""
                is Int -> getInt(key, default)
                is Boolean -> getBoolean(key, default)
                is Float -> getFloat(key, default)
                is Set<*> -> getStringSet(key, default as Set<String>) ?: default as Set<String>
                else -> throw IllegalArgumentException("SharedPreferences can't be get this type")
            }
            return res as T
        }
    }

}
