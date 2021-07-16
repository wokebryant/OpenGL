package com.example.opendemo.opengl.shape

import android.content.Context
import android.opengl.GLES20
import com.example.opendemo.R
import com.example.opendemo.utlis.*
import java.nio.FloatBuffer
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 *  球体绘制/地球仪
 */
class Ball(var context: Context) {

    private lateinit var ballCoords: FloatArray
    private lateinit var textureCoords: FloatArray

    private val vertexBuffer: FloatBuffer
    private var textureBuffer: FloatBuffer

    private var vPMatrixHandle: Int = 0
    private var mPositionHandle: Int = 0
    private var mTextureHandle: Int = 0

    private val mProgram: Int
    private val mTextureId: Int

    private var alVertix = ArrayList<Float>()
    private var textureVertix = ArrayList<Float>()

    init {
//        getBallCoordinates()
        calculateAttribute()
        ballCoords = convertToFloat(alVertix)
        textureCoords = convertToFloat(textureVertix)
        vertexBuffer = BufferUtil.getFloatBuffer(ballCoords)
        textureBuffer = BufferUtil.getFloatBuffer(textureCoords)

        //加载着色器，获取程序
        val verTexShader = ShaderUtil.loadVertexShader()
        val fragmentShader = ShaderUtil.loadFragmentShader()
        mProgram = ProgramUtil.getProgramWithVF(verTexShader, fragmentShader)
        mTextureId = TextureUtil.loadTexture(context, R.drawable.earth_2d)
    }

    //计算顶点坐标和纹理坐标
    private fun calculateAttribute() {
        val radius = 1.0f // 球的半径
        val angleSpan = Math.PI / 90f // 将球进行单位切分的角度
        var vAngle = 0.0
        while (vAngle < Math.PI) {
            var hAngle = 0.0
            while (hAngle < 2 * Math.PI) {
                val x0 = (radius * Math.sin(vAngle) * Math.cos(hAngle)).toFloat()
                val y0 = (radius * Math.sin(vAngle) * Math.sin(hAngle)).toFloat()
                val z0 = (radius * Math.cos(vAngle)).toFloat()
                val x1 = (radius * Math.sin(vAngle) * Math.cos(hAngle + angleSpan)).toFloat()
                val y1 = (radius * Math.sin(vAngle) * Math.sin(hAngle + angleSpan)).toFloat()
                val z1 = (radius * Math.cos(vAngle)).toFloat()
                val x2 =
                    (radius * Math.sin(vAngle + angleSpan) * Math.cos(hAngle + angleSpan)).toFloat()
                val y2 =
                    (radius * Math.sin(vAngle + angleSpan) * Math.sin(hAngle + angleSpan)).toFloat()
                val z2 = (radius * Math.cos(vAngle + angleSpan)).toFloat()
                val x3 = (radius * Math.sin(vAngle + angleSpan) * Math.cos(hAngle)).toFloat()
                val y3 = (radius * Math.sin(vAngle + angleSpan) * Math.sin(hAngle)).toFloat()
                val z3 = (radius * Math.cos(vAngle + angleSpan)).toFloat()
                val s0 = (hAngle / Math.PI / 2).toFloat()
                val s1 = ((hAngle + angleSpan) / Math.PI / 2).toFloat()
                val t0 = (vAngle / Math.PI).toFloat()
                val t1 = ((vAngle + angleSpan) / Math.PI).toFloat()
                alVertix.add(x1)
                alVertix.add(y1)
                alVertix.add(z1)
                alVertix.add(x0)
                alVertix.add(y0)
                alVertix.add(z0)
                alVertix.add(x3)
                alVertix.add(y3)
                alVertix.add(z3)
                textureVertix.add(s1) // x1 y1对应纹理坐标
                textureVertix.add(t0)
                textureVertix.add(s0) // x0 y0对应纹理坐标
                textureVertix.add(t0)
                textureVertix.add(s0) // x3 y3对应纹理坐标
                textureVertix.add(t1)
                alVertix.add(x1)
                alVertix.add(y1)
                alVertix.add(z1)
                alVertix.add(x3)
                alVertix.add(y3)
                alVertix.add(z3)
                alVertix.add(x2)
                alVertix.add(y2)
                alVertix.add(z2)
                textureVertix.add(s1) // x1 y1对应纹理坐标
                textureVertix.add(t0)
                textureVertix.add(s0) // x3 y3对应纹理坐标
                textureVertix.add(t1)
                textureVertix.add(s1) // x2 y3对应纹理坐标
                textureVertix.add(t1)
                hAngle = hAngle + angleSpan
            }
            vAngle = vAngle + angleSpan
        }
    }

    //动态数组转FloatBuffer
    fun convertToFloat(data: ArrayList<Float>): FloatArray {
        val d = FloatArray(data.size)
        for (i in d.indices) {
            d[i] = data[i]
        }
        return d
    }

    /**
     *  获取球体坐标
     *  vAngle: 纬度旋转角度  a
     *  hAngle: 经度旋转角度  b
     */
    private fun getBallCoordinates() {
        val radius = 1.0f
        val angleToArc = Math.PI / 180f
        val angleSpan = 2  //10度的旋转

        var vertexList = mutableListOf<Float>()
        var textureList = mutableListOf<Float>()

        for (vAngle in -90..90 step angleSpan) {
            for (hAngle in 0..360 step angleSpan) {
                //定义球体单元矩形但四个顶点
                val x0: Float = (radius * cos(vAngle * angleToArc) * sin(hAngle * angleToArc)).toFloat()
                val y0: Float = radius * sin(vAngle * angleToArc).toFloat()
                val z0: Float = (radius * cos(vAngle * angleToArc) * cos(hAngle * angleToArc)).toFloat()

                val x1: Float = (radius * cos(vAngle * angleToArc) * sin((hAngle + angleSpan) * angleToArc)).toFloat()
                val y1: Float = radius * sin(vAngle * angleToArc).toFloat()
                val z1: Float = (radius * cos(vAngle * angleToArc) * cos((hAngle + angleSpan) * angleToArc)).toFloat()

                val x2: Float = (radius * cos((vAngle + angleSpan) * angleToArc) * sin((hAngle + angleSpan) * angleToArc)).toFloat()
                val y2: Float = radius * sin((vAngle + angleSpan) * angleToArc).toFloat()
                val z2: Float = (radius * cos((vAngle + angleSpan) * angleToArc) * cos((hAngle + angleSpan) * angleToArc)).toFloat()

                val x3: Float = (radius * cos((vAngle + angleSpan) * angleToArc) * sin(hAngle * angleToArc)).toFloat()
                val y3: Float = radius * sin((vAngle + angleSpan) * angleToArc).toFloat()
                val z3: Float = (radius * cos((vAngle + angleSpan) * angleToArc) * cos(hAngle * angleToArc)).toFloat()

                //s: 纬度旋转方向， t:经度旋转方向
                val s0: Float = (hAngle / 90).toFloat()
                val s1: Float = ((hAngle + angleSpan) / 90).toFloat()
                val t0: Float = (vAngle / 90).toFloat()
                val t1: Float = ((vAngle + angleSpan) / 90).toFloat()



                //将矩形拆分为2个三角形，单独绘制
                vertexList.apply {
                    add(x3)
                    add(y3)
                    add(z3)

                    add(x0)
                    add(y0)
                    add(z0)

                    add(x1)
                    add(y1)
                    add(z1)

                    add(x3)
                    add(y3)
                    add(z3)

                    add(x1)
                    add(y1)
                    add(z1)

                    add(x2)
                    add(y2)
                    add(z2)
                }

                //顶点对应的纹理坐标
                textureList.apply {
                    add(s0)
                    add(t1)

                    add(s0)
                    add(t0)

                    add(s1)
                    add(t0)

                    add(s0)
                    add(t1)

                    add(s1)
                    add(t0)

                    add(s1)
                    add(t1)
                }


            }
        }

        ballCoords = vertexList.toFloatArray()
        textureCoords = textureList.toFloatArray()
    }

    fun draw(mvpMatrix: FloatArray) {
        //在OpenGL ES中启用程序
        GLES20.glUseProgram(mProgram)

        prepareMatrixParams(mvpMatrix)
        prepareVerTexParams()
        prepareTextureParams()

        GLES20.glDrawArrays(
            GLES20.GL_TRIANGLES,
            0,
            ballCoords.size / CoordinateUtil.COORDS_PER_VERTEX
        )

        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mTextureHandle)
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
     *  启用纹理句柄
     */
    fun prepareTextureParams() {
        mTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord").also {
            GLES20.glEnableVertexAttribArray(it)

            GLES20.glVertexAttribPointer(
                it,
                CoordinateUtil.COORDS_PER_TEXTURE,
                GLES20.GL_FLOAT,
                false,
                CoordinateUtil.COORDS_PER_TEXTURE * 4,
                textureBuffer
            )
            //激活纹理
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            //绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)
        }
    }

}