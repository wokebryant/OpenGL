package com.example.opengldemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.opengldemo.widget.MyGLSurfaceView

class OpenGLES20Activity : BaseActivity() {

//    private lateinit var gLView: MyGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        gLView = MyGLSurfaceView(this)
//        setContentView(gLView)
        setContentView(R.layout.activity_main)

    }


}