package com.example.opendemo

import android.os.Bundle
import android.view.View
import com.example.opendemo.base.BaseActivity
import com.example.opendemo.opengl.OpenGLES20Activity
import com.example.opendemo.opencv.OpenCVActivity
import com.example.opendemo.utlis.AppUtil
import kotlinx.android.synthetic.main.activity_main.*

/**
 *  主页
 */
class MainActivity : BaseActivity(),View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        test_openGL_btn.setOnClickListener(this)
        test_openCV_btn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            test_openGL_btn.id -> {
                AppUtil.startActivity<OpenGLES20Activity>(this){
                    putExtra("title", "OpenGL")
                }
            }

            test_openCV_btn.id -> {
                AppUtil.startActivity<OpenCVActivity>(this){
                    putExtra("title", "OpenCV")
                }
            }
        }
    }


}