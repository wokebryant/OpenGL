package com.example.opengldemo.widget

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.example.opengldemo.MyGLRenderer
import java.util.jar.Attributes

/**
 *  OpenGL渲染视图
 */
class MyGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs){

    private val renderer: MyGLRenderer

    init {
        //创建OpenGL ES版本
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer()
        setRenderer(renderer)

        //设置将渲染模式设置为仅在绘制数据发生变化时绘制视图
        renderMode = RENDERMODE_WHEN_DIRTY
    }




}