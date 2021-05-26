package com.example.opengldemo.pointcloud.utils

import android.app.Application

object BaseUtil {

    var applicationContext: Application? = null
        private set

    fun init(application: Application?) {
        applicationContext = application
    }

}