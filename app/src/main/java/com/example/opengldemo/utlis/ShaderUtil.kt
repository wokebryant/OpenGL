package com.example.opengldemo.utlis

import android.opengl.GLES20

/**
 * 着色器工具类
 */
object ShaderUtil {

    const val vertexShaderType = GLES20.GL_VERTEX_SHADER
    const val fragmentShaderType = GLES20.GL_FRAGMENT_SHADER

    /**
     * 顶点着色器代码
     */
    const val vertexShaderCode =
        "attribute vec4 vPosition;" +
                "void main() {" +
                "  gl_Position = vPosition;" +
                "}"

    /**
     * 片段着色器代码
     */
    const val fragmentShaderCode =
        "precision mediump float;" +
                "uniform vec4 vColor;" +
                "void main() {" +
                "  gl_FragColor = vColor;" +
                "}"


    /**
     * 获取顶点着色器
     */
    fun loadVertexShader(): Int {
        return loadShader(vertexShaderType, vertexShaderCode)
    }

    /**
     * 获取片段着色器
     */
    fun loadFragmentShader(): Int {
        return loadShader(fragmentShaderType, fragmentShaderCode)
    }

    /**
     * 获取着色器
     */
    fun loadShader(type: Int, shaderCode: String) =
        GLES20.glCreateShader(type).also {shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }



}