package com.example.opengldemo

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.opengldemo.widget.MyGLSurfaceView
import kotlinx.android.synthetic.main.activity_main.*

class OpenGLES20Activity : BaseActivity() {

//    private lateinit var gLView: MyGLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        gLView = MyGLSurfaceView(this)
//        setContentView(gLView)
        setContentView(R.layout.activity_main)

        var offset = 1.0f
        scaleBtn.setOnClickListener {
            offset -= 0.1f
            myGLSurfaceView.updateScale(offset)
        }

    }


}