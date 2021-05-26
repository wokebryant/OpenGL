package com.example.opengldemo.shape

import android.opengl.GLES20
import com.example.opengldemo.utlis.*
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

/**
 *  绘制圆柱
 */
class Cylinder {
    companion object{
        private const val SEPARATE = 120
        private const val RADIUS = 0.5f
        private const val HEIGHT = 1.0f
    }

    private lateinit var coordinates: FloatArray
    private lateinit var coordinatesBottom: FloatArray
    private lateinit var coordinatesTop: FloatArray
    private lateinit var gradientColors: FloatArray
    private lateinit var gradientSideColors: FloatArray

    private var vertexBuffer: FloatBuffer           //圆柱侧面
    private var vertexBottomBuffer: FloatBuffer     //圆柱底部
    private var vertexTopBuffer: FloatBuffer        //圆柱顶部
    private var gradientColorBuffer: FloatBuffer
    private var gradientSideColorBuffer: FloatBuffer

    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var mGradientColorHandle: Int = 0
    private var mGradientColorSideHandle: Int = 0
    private var vPMatrixHandle: Int = 0

    private val mProgram: Int

    init {
        //获取顶点坐标和颜色值
        getCoordinates(RADIUS, SEPARATE)
        vertexBuffer = BufferUtil.getFloatBuffer(coordinates)
        vertexBottomBuffer = BufferUtil.getFloatBuffer(coordinatesBottom)
        vertexTopBuffer = BufferUtil.getFloatBuffer(coordinatesTop)
        gradientColorBuffer = BufferUtil.getFloatBuffer(gradientColors)
        gradientSideColorBuffer = BufferUtil.getFloatBuffer(gradientSideColors)
        //加载着色器，获取程序
        val verTexShader = ShaderUtil.loadSampleVertexShader()
        val fragmentShader = ShaderUtil.loadSampleFragmentShader()
        mProgram = ProgramUtil.getProgramWithVF(verTexShader, fragmentShader)
    }

    /**
     *  计算圆柱每个顶点的坐标， sin(pi) = sin(180)
     */
    private fun getCoordinates(radius: Float, verTexNum: Int) {
        //侧面坐标数，数组个数为（顶点个数 + 额外一个点闭合）* 2，2代表顶部和底部
        val coordinatesSideNum = (verTexNum + 1) * 3  * 2
        //圆坐标数, 数组个数为圆心 + 顶点个数 + 额外一个点闭合
        val coordinatesCircleNum = (verTexNum + 2) * 3

        val stepNum = 360f / verTexNum

        coordinates = FloatArray(coordinatesSideNum)
        var sideOffset = 0
        //设置每一个顶点的坐标，将圆柱侧面拆解成棱柱，棱柱的每一个面为一个矩形，然后将矩形拆解成两个三角形
        for (i in 0..360 step stepNum.toInt()) {
            coordinates[sideOffset++] = radius * cos(i * Math.PI / 180f) .toFloat()
            coordinates[sideOffset++] = radius * sin(i * Math.PI / 180f) .toFloat()
            coordinates[sideOffset++] = HEIGHT

            coordinates[sideOffset++] = radius * cos(i * Math.PI / 180f) .toFloat()
            coordinates[sideOffset++] = radius * sin(i * Math.PI / 180f) .toFloat()
            coordinates[sideOffset++] = 0.0f
        }

        //设置圆柱顶部
        coordinatesTop = FloatArray(coordinatesCircleNum)
        coordinatesTop[0] = 0.0f
        coordinatesTop[1] = 0.0f
        coordinatesTop[2] = HEIGHT
        var topOffset = 3
        for (i in 0..360 step stepNum.toInt()) {
            coordinatesTop[topOffset++] = radius * cos(i * Math.PI / 180f) .toFloat()
            coordinatesTop[topOffset++] = radius * sin(i * Math.PI / 180f) .toFloat()
            coordinatesTop[topOffset++] = HEIGHT
        }

        //设置圆柱底部
        coordinatesBottom = FloatArray(coordinatesCircleNum)
        coordinatesBottom[0] = 0.0f
        coordinatesBottom[1] = 0.0f
        coordinatesBottom[2] = 0.0f
        var bottomOffset = 3
        for (i in 0..360 step stepNum.toInt()) {
            coordinatesBottom[bottomOffset++] = radius * cos(i * Math.PI / 180f) .toFloat()
            coordinatesBottom[bottomOffset++] = radius * sin(i * Math.PI / 180f) .toFloat()
            coordinatesBottom[bottomOffset++] = 0.0f
        }

        //获取圆形颜色
        gradientColors = FloatArray(coordinatesCircleNum * 4 /3)
        gradientColors[0] = 0.5f
        gradientColors[1] = 0.5f
        gradientColors[2] = 0.5f
        gradientColors[3] = 0.5f
        for (i in 4 until gradientColors.size) {
            gradientColors[i] = 1.0f
        }

        //获取侧面颜色
        gradientSideColors = FloatArray(coordinatesSideNum * 4 / 3)
        for (i in gradientSideColors.indices) {
            gradientSideColors[i] = 0.7f
        }
    }


    /**
     * https://blog.csdn.net/u013749540/article/details/91826613
     * 绘制三角形序列的三种方式
     */
    fun draw(mvpMatrix: FloatArray) {
        //在OpenGL ES中启用程序
        GLES20.glUseProgram(mProgram)

        prepareMatrixParams(mvpMatrix)

        prepareVerTexParams()
        prepareSideGrandientColorParams()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, coordinates.size / CoordinateUtil.COORDS_PER_VERTEX)

        prepareBottomVerTexParams()
        prepareGrandientColorParams()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, coordinatesBottom.size / CoordinateUtil.COORDS_PER_VERTEX)

        prepareTopVerTexParams()
        prepareGrandientColorParams()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, coordinatesTop.size / CoordinateUtil.COORDS_PER_VERTEX)

        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mGradientColorSideHandle)
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
     *  启用圆形渐变色句柄
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
     *  启用侧边圆形渐变色句柄
     */
    fun prepareSideGrandientColorParams() {
        mGradientColorSideHandle = GLES20.glGetAttribLocation(mProgram, "a_Color").also {
            GLES20.glEnableVertexAttribArray(it)

            GLES20.glVertexAttribPointer(
                it,
                ColorUtil.COLOR_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                ColorUtil.COLOR_PER_VERTEX * 4,
                gradientSideColorBuffer
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
     *  启用侧面顶点句柄
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

    /**
     *  启用顶部顶点句句柄
     */
    fun prepareTopVerTexParams() {
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
                vertexTopBuffer
            )
        }
    }



}