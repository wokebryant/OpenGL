package com.example.opengldemo.utlis

import android.opengl.GLES20

/**
 *  OpenGL程序工具类
 */
object ProgramUtil {

    /**
     * 通过顶点着色器和片段着色器获取程序
     */
    fun getProgramWithVF(verTexShader: Int, fragmentShader: Int) =
        GLES20.glCreateProgram().also { program ->
            GLES20.glAttachShader(program, verTexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
        }


}