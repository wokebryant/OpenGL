package com.example.opendemo.opencv

import android.os.Bundle
import android.util.Log
import com.example.opendemo.base.BaseActivity
import com.example.opendemo.R

/**
 *  OpenCV示例
 */
class OpenCVActivity : BaseActivity() {

    companion object {
        private const val TAG = "OpenCVActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opencv)
        parseIntentData()
    }

//    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
//        override fun onManagerConnected(status: Int) {
//            when (status) {
//                LoaderCallbackInterface.SUCCESS -> {
//                    Log.i(TAG, "OpenCV loaded successfully")
//                }
//                else -> {
//                    super.onManagerConnected(status)
//                }
//            }
//        }
//    }

    /**
     * 初始化opencv库
     */
    override fun onResume() {
        super.onResume()
//        if (!OpenCVLoader.initDebug()) {
//            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback)
//        } else {
//            Log.d(TAG, "OpenCV library found inside package. Using it!")
//            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
//        }
    }

    private fun parseIntentData() {
        intent?.let {
            val extra = it.extras
            title = extra?.getString("title")
        }
    }


}