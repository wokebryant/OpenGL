package com.example.opendemo.opengl.widget

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.example.opendemo.opengl.MyGLRenderer

/**
 *  OpenGL渲染视图
 */
class MyGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs){

    private val renderer: MyGLRenderer

    companion object {
        private const val TOUCH_SCALE_FACTOR: Float = 180.0f / 320f
    }
    private var previousX: Float = 0f
    private var previousY: Float = 0f

    init {
        //创建OpenGL ES版本
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer()
        renderer.context = getContext()
        setRenderer(renderer)

        //设置将渲染模式设置为仅在绘制数据发生变化时绘制视图
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun updateScale(scale: Float) {
        renderer.scale = scale
        requestRender()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var x: Float = event!!.x
        var y: Float = event.y

        when(event.action) {
            MotionEvent.ACTION_MOVE -> {
                var dx: Float = x - previousX
                var dy: Float = y - previousY

                if (y > height / 2) {
                    dx *= -1
                }
                if (x < width / 2) {
                    dy *= -1
                }
                Log.i("SurfaceView:", "dx= $dx, dy=$dy, x=$x, y=$y")

                renderer.angle += (dx + dy) * TOUCH_SCALE_FACTOR
                Log.i("RotateAngle=:", renderer.angle.toString())

                requestRender()
            }

        }
        previousX = x
        previousY = y

        return true
    }

}