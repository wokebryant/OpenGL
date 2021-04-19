package com.example.opengldemo.shape

import android.opengl.GLES20
import com.example.opengldemo.utlis.ShaderUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 绘制三角形
 */

//数组中每个顶点的坐标数
const val COORDS_PER_VERTEX = 3
//三角形坐标
var triangleCoords = floatArrayOf( //按逆时针顺序
    0.0f, 0.622008459f, 0.0f,      // 顶部
    -0.5f, -0.311004243f, 0.0f,    // 左下
    0.5f, -0.311004243f, 0.0f      // 右下
)

class Triangle {
    //使用RGB色值和Alpha设置颜色
    val color = floatArrayOf(
        0.63671875f, 0.76953125f, 0.22265625f, 1.0f
    )

    //将坐标写入 ByteBuffer 中
    private var vertexBuffer: FloatBuffer =
        //申请内存，坐标值的数量*4字节
        ByteBuffer.allocateDirect(triangleCoords.size * 4).run {
            //使用硬件native字节指令
            order(ByteOrder.nativeOrder())
            //从ByteBuffer创建一个浮点缓冲区
            asFloatBuffer().apply {
                //将坐标添加到FloatBuffer
                put(triangleCoords)
                //设置缓冲区以读取第一个坐标
                position(0)
            }
        }

    private var mProgram: Int

    init {
        //添加顶点着色器和片段着色器到程序中
        val vertexShader: Int = ShaderUtil.loadVertexShader()
        val fragmentShader = ShaderUtil.loadFragmentShader()
        mProgram = GLES20.glCreateProgram().also {program ->
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
        }
    }

    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0

    private val verTexCount = triangleCoords.size / COORDS_PER_VERTEX
    private val verTexStride = COORDS_PER_VERTEX * 4  //???

    /**
     * 绘制
     */
    fun draw() {
        //获取程序
        GLES20.glUseProgram(mProgram)

        //返回属性变量的位置
        //vPosition是顶点着色器代码中字段
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            //启用顶点属性
            GLES20.glEnableVertexAttribArray(it)
            //准备三角形坐标数据
            GLES20.glVertexAttribPointer(
                it,
                COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                verTexStride,
                vertexBuffer
            )
        }

        //返回统一变量的位置(此处是获取程序中颜色的位置)
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also {
            GLES20.glUniform4fv(it, 1, color, 0)
        }

        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, verTexCount)

        GLES20.glDisableVertexAttribArray(mPositionHandle)

    }




}