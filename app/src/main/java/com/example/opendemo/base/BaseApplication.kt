package com.example.opendemo.base

import android.app.Application
import com.example.opendemo.opengl.pointcloud.utils.BaseUtil

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        BaseUtil.init(this)
    }
}