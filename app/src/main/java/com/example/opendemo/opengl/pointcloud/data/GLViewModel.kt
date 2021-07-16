package com.example.opendemo.opengl.pointcloud.data

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

/**
 *  3D点云ViewModel
 */
class GLViewModel : ViewModel() {

    private val liveData = MutableLiveData<Context>()

    val cloudPointData = Transformations.switchMap(liveData) {
        Repository.getCloudPointFromAssets(it)
    }

    fun getCloudPointData(context: Context) {
        liveData.value = context
    }

}