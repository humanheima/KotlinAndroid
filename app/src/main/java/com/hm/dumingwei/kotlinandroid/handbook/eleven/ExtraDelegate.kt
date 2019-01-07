package com.hm.dumingwei.kotlinandroid.handbook.eleven

import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import kotlin.reflect.KProperty

/**
 * Created by dmw on 2019/1/7.
 * Desc:
 */
class ExtraDelegate<out T>(private val extraName: String, private val defaultValue: T) {

    private var extra: T? = null

    operator fun getValue(thisRef: AppCompatActivity, property: KProperty<*>): T {
        extra = getExtra(extra, extraName, thisRef)
        return extra ?: defaultValue
    }

    operator fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        extra = getExtra(extra, extraName, thisRef)
        return extra ?: defaultValue
    }
}

fun <T> extraDelegate(extra: String, defaultValue: T): ExtraDelegate<T> = ExtraDelegate(extra, defaultValue)

fun extraDelegate(extra: String) = extraDelegate(extra, null)

@Suppress("UNCHECKED_CAST")
private fun <T> getExtra(oldExtra: T?, extraName: String, thisRef: AppCompatActivity): T? = oldExtra
        ?: thisRef.intent?.extras?.get(extraName) as T?

@Suppress("UNCHECKED_CAST")
private fun <T> getExtra(oldExtra: T?, extraName: String, thisRef: Fragment): T? = oldExtra
        ?: thisRef.arguments?.get(extraName) as T?