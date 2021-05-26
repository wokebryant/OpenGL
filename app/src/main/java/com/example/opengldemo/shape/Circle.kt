package com.example.opengldemo.shape

import android.opengl.GLES20
import android.util.Log
import com.example.opengldemo.R
import com.example.opengldemo.utlis.*
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.log
import kotlin.math.sin

/**
 *  绘制圆形
 */
class Circle {

    private lateinit var coordinates: FloatArray

    private var vertexBuffer: FloatBuffer

    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var vPMatrixHandle: Int = 0

    private val mProgram: Int

    init {
        //获取顶点坐标和颜色值
        getCoordinates(0.5f, 60)
        vertexBuffer = BufferUtil.getFloatBuffer(coordinates)
        //加载着色器，获取程序
        val verTexShader = ShaderUtil.loadSampleVertexShader()
        val fragmentShader = ShaderUtil.loadSampleFragmentShader()
        mProgram = ProgramUtil.getProgramWithVF(verTexShader, fragmentShader)
    }

    /**
     *  计算圆每个顶点的坐标， sin(pi) = sin(180)
     */
    private fun getCoordinates(radius: Float, verTexNum: Int) {
        //设置圆心坐标, 数组个数为圆心 + 顶点个数 + 额外一个点闭合
        coordinates = FloatArray((verTexNum + 2) * 3)
        coordinates[0] = 0.0f
        coordinates[1] = 0.0f
        coordinates[2] = 0.0f

        val stepNum = 360f / verTexNum

        var offer = 3
        //设置每一个顶点的坐标
        for (i in 0..360 step stepNum.toInt()) {
            coordinates[offer++] = radius * cos(i * Math.PI / 180f) .toFloat()
            coordinates[offer++] = radius * sin(i * Math.PI / 180f) .toFloat()
            coordinates[offer++] = 0.0f
        }
    }




    fun draw(mvpMatrix: FloatArray) {
        //在OpenGL ES中启用程序
        GLES20.glUseProgram(mProgram)

        prepareVerTexParams()
        prepareColorParamas()
        prepareMatrixParams(mvpMatrix)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, coordinates.size / CoordinateUtil.COORDS_PER_VERTEX)

        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    /**
     *  启用颜色句柄
     */
    fun prepareColorParamas() {
        //返回统一变量的位置(此处是获取程序中颜色的位置)
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also {
            GLES20.glUniform4fv(it, 1, ColorUtil.color, 0)
        }
    }

    /**
     *  启用变换矩阵句柄
     */
    fun prepareMatrixParams(mvpMatrix: FloatArray) {
        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix").also {
            GLES20.glUniformMatrix4fv(it, 1, false, mvpMatrix, 0)
        }
    }

    /**
     *  启用顶点句柄
     */
    fun prepareVerTexParams() {
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            //启用顶点属性
            GLES20.glEnableVertexAttribArray(it)

            //准备圆形坐标数据
            GLES20.glVertexAttribPointer(
                it,
                CoordinateUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                CoordinateUtil.COORDS_PER_VERTEX * 4,
                vertexBuffer
            )
        }
    }


}