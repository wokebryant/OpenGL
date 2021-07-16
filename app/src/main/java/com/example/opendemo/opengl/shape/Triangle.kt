package com.example.opendemo.opengl.shape

import android.opengl.GLES20
import com.example.opendemo.utlis.*
import java.nio.FloatBuffer

/**
 * 绘制三角形
 */

class Triangle {
    //将坐标写入 ByteBuffer 中
    private var vertexBuffer: FloatBuffer = BufferUtil.getFloatBuffer(CoordinateUtil.triangleCoords)

    private var mProgram: Int

    init {
        //添加顶点着色器和片段着色器到程序中
        val vertexShader = ShaderUtil.loadVertexShader()
        val fragmentShader = ShaderUtil.loadFragmentShader()
        mProgram = ProgramUtil.getProgramWithVF(vertexShader, fragmentShader)
    }

    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var vPMatrixHandle: Int = 0


    private val verTexCount = CoordinateUtil.triangleCoords.size / CoordinateUtil.COORDS_PER_VERTEX
    private val verTexStride = CoordinateUtil.COORDS_PER_VERTEX * 4  //???

    /**
     * 绘制
     */
    fun draw(mvpMatrix: FloatArray) {
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
                CoordinateUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                verTexStride,
                vertexBuffer
            )
        }

        //返回统一变量的位置(此处是获取程序中颜色的位置)
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor").also {
            GLES20.glUniform4fv(it, 1, ColorUtil.color, 0)
        }

        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix").also {
            GLES20.glUniformMatrix4fv(it, 1, false, mvpMatrix, 0)
        }

        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, verTexCount)

        GLES20.glDisableVertexAttribArray(mPositionHandle)

    }




}