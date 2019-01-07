package com.hm.dumingwei.kotlinandroid

import android.app.Application
import kotlin.properties.Delegates

/**
 * Created by dmw on 2019/1/7.
 * Desc:
 */
class App : Application() {

    companion object {

        var instance: App by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

    }
}