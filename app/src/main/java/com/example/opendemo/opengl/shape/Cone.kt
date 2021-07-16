package com.example.opendemo.opengl.shape

import android.opengl.GLES20
import com.example.opendemo.utlis.*
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 *  绘制圆锥
 */
class Cone {

    private lateinit var coordinates: FloatArray
    private lateinit var coordinatesBottom: FloatArray
    private lateinit var gradientColors: FloatArray

    private var vertexBuffer: FloatBuffer           //圆锥侧面
    private var vertexBottomBuffer: FloatBuffer     //圆锥底部
    private var gradientColorBuffer: FloatBuffer

    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var mGradientColorHandle: Int = 0
    private var vPMatrixHandle: Int = 0

    private val mProgram: Int

    init {
        //获取顶点坐标和颜色值
        getCoordinates(0.5f, 60)
        vertexBuffer = BufferUtil.getFloatBuffer(coordinates)
        vertexBottomBuffer = BufferUtil.getFloatBuffer(coordinatesBottom)
        gradientColorBuffer = BufferUtil.getFloatBuffer(gradientColors)
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
        coordinates[2] = -0.5f

        val stepNum = 360f / verTexNum

        var offer = 3
        //设置每一个顶点的坐标
        for (i in 0..360 step stepNum.toInt()) {
            coordinates[offer++] = radius * cos(i * Math.PI / 180f) .toFloat()
            coordinates[offer++] = radius * sin(i * Math.PI / 180f) .toFloat()
            coordinates[offer++] = 0.0f
        }

        //设置圆锥底面
        coordinatesBottom = FloatArray(coordinates.size)
        for (i in coordinates.indices) {
            if (i == 2) coordinatesBottom[i] = 0.0f else coordinatesBottom[i] = coordinates[i]
        }

        //获取颜色
        gradientColors = FloatArray(coordinates.size * 4 /3)
        gradientColors[0] = 0.5f
        gradientColors[1] = 0.5f
        gradientColors[2] = 0.5f
        gradientColors[3] = 0.5f

        for (i in 4 until gradientColors.size) {
            gradientColors[i] = 1.0f
        }
    }




    fun draw(mvpMatrix: FloatArray) {
        //在OpenGL ES中启用程序
        GLES20.glUseProgram(mProgram)

        prepareVerTexParams()
//        prepareColorParamas()
        prepareGrandientColorParams()
        prepareMatrixParams(mvpMatrix)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, coordinates.size / CoordinateUtil.COORDS_PER_VERTEX)

        prepareBottomVerTexParams()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, coordinatesBottom.size / CoordinateUtil.COORDS_PER_VERTEX)

        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mGradientColorHandle)
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
     *  启用渐变色句柄
     */
    fun prepareGrandientColorParams() {
        mGradientColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color").also {
            GLES20.glEnableVertexAttribArray(it)

            GLES20.glVertexAttribPointer(
                it,
                ColorUtil.COLOR_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                ColorUtil.COLOR_PER_VERTEX * 4,
                gradientColorBuffer
            )
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

    /**
     *  启用底部顶点句句柄
     */
    fun prepareBottomVerTexParams() {
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
                vertexBottomBuffer
            )
        }
    }
}