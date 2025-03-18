package com.hm.dumingwei.kotlinandroid.testbase

import kotlin.properties.Delegates
import kotlin.reflect.KProperty

class Delegate {
    private var value: String = "Default"

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        println("Getting ${property.name} from $thisRef")
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        println("Setting ${property.name} to $value in $thisRef")
        this.value = value
    }
}

class Example {
    var name: String by Delegate()
}
