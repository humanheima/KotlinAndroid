package com.hm.dumingwei

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


/**
 * Created by dumingwei on 2020/4/27.
 *
 * Desc: json工具类
 */
class JsonUtilKt private constructor(val gson: Gson) {

    private val TAG: String? = "JsonUtilKt"

    companion object {
        @JvmStatic
        val instance: JsonUtilKt by lazy { JsonUtilKt(Gson()) }
    }

    fun toJson(obj: Any?): String {
        return gson.toJson(obj)
    }

    fun <T> toObject(json: String?, classOfT: Class<T>): T? {
        return try {
            gson.fromJson(json, classOfT)
        } catch (e: Exception) {
            Log.d(TAG, "toObject: error ${e.message}")
            null
        }
    }

    fun <T> toList(json: String?, type: Type): List<T>? {
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            Log.d(TAG, "toObject: error ${e.message}")
            null
        }
    }

}
