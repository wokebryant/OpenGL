package com.example.opengldemo

import android.app.Application
import com.example.opengldemo.pointcloud.utils.BaseUtil

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        BaseUtil.init(this)
    }
}