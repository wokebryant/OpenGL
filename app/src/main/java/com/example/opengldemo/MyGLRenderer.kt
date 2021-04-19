package com.example.opengldemo

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.opengldemo.shape.Square
import com.example.opengldemo.shape.Triangle
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * OpenGL渲染类
 */
class MyGLRenderer : GLSurfaceView.Renderer {

    private lateinit var mTriangle: Triangle
    private lateinit var mSquare: Square

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //设置背景参数，参数为RGB色值以及透明度
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mTriangle = Triangle()
        mSquare = Square()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        mTriangle.draw()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //设置Surface显示区域，坐标原点和右上角坐标
        GLES20.glViewport(0, 0, width, height)
    }

}