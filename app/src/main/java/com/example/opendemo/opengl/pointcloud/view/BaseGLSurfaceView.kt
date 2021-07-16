package com.example.opendemo.opengl.pointcloud.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.sqrt

open class BaseGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs) {

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
    }

    /**
     *  获取指尖之间的间距
     */
    fun getSpacing(event: MotionEvent): Float = if (event.pointerCount == 2) {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            sqrt(x * x + y * y)
        } else {
            0f
        }

    /**
     *  判断双指操作是缩放还是平移
     */
    fun isScale(currentSpace: Float, previousSpace: Float): Boolean = abs(currentSpace - previousSpace) > 50

}