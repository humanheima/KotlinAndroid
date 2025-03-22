package com.hm.dumingwei.kotlinandroid.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

class SimpleViewModel(val app: Application) : AndroidViewModel(app) {



    fun test(){
        viewModelScope
    }
}