package com.example.opendemo.opengl

import android.os.Bundle
import com.example.opendemo.base.BaseActivity
import com.example.opendemo.R
import kotlinx.android.synthetic.main.activity_opengl.*

class OpenGLES20Activity : BaseActivity() {

//    private lateinit var gLView: MyGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        gLView = MyGLSurfaceView(this)
//        setContentView(gLView)
        parseIntentData()
        setContentView(R.layout.activity_opengl)

        var offset = 1.0f
        scaleBtn.setOnClickListener {
            offset -= 0.1f
            myGLSurfaceView.updateScale(offset)
        }

    }

    private fun parseIntentData() {
        intent?.let {
            val extra = it.extras
            title = extra?.getString("title")
        }
    }


}