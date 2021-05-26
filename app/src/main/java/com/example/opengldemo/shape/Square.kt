package com.example.opengldemo.shape

import android.content.Context
import android.opengl.GLES20
import com.example.opengldemo.R
import com.example.opengldemo.utlis.*
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 *  绘制正方形
 */

class Square(var context: Context) {

    //将顶点坐标写入ByteBuffer中
    private var vertexBuffer: FloatBuffer = BufferUtil.getFloatBuffer(CoordinateUtil.squareCoords)

    //初始化绘制列表的字节缓冲区
    private var drawListBuffer: ShortBuffer = BufferUtil.getShortBuffer(CoordinateUtil.drawOrder)

    //将渐变色值写入buffer
    private var gradientColorBuffer: FloatBuffer = BufferUtil.getFloatBuffer(ColorUtil.gradientColor)

    //将纹理坐标写入buffer
    private var textureBuffer: FloatBuffer = BufferUtil.getFloatBuffer(CoordinateUtil.textureCoords)

    private val mProgram: Int
    private val mTextureId: Int

    init {
        //加载着色器，获取程序
        val verTexShader = ShaderUtil.loadVertexShader()
        val fragmentShader = ShaderUtil.loadFragmentShader()
        mProgram = ProgramUtil.getProgramWithVF(verTexShader, fragmentShader)
        mTextureId = TextureUtil.loadTexture(context, R.drawable.lf_combsend_laugh)
    }

    private var mPositionHandle: Int = 0
    private var mColorHandle: Int = 0
    private var mGradientColorHandle: Int = 0
    private var vPMatrixHandle: Int = 0
    private var mTextureHandle: Int  = 0

    private val verTexStride = CoordinateUtil.COORDS_PER_VERTEX * 4  //???

    fun draw(mvpMatrix: FloatArray) {
        //在OpenGL ES中启用程序
        GLES20.glUseProgram(mProgram)

        //启用需要的句柄
        prepareVerTexParams()
        prepareTextureParams()
        prepareMatrixParams(mvpMatrix)

        // 绘制，该方法的绘制顺序由drawListBuffer指定
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, CoordinateUtil.drawOrder.size, GLES20.GL_UNSIGNED_SHORT, drawListBuffer)

        //关闭打开的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle)
//        GLES20.glDisableVertexAttribArray(mGradientColorHandle)
        GLES20.glDisableVertexAttribArray(mTextureHandle)
    }

    /**
     *  启用顶点句柄
     */
    fun prepareVerTexParams() {
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition").also {
            //启用顶点属性
            GLES20.glEnableVertexAttribArray(it)

            //准备正方形坐标数据
            GLES20.glVertexAttribPointer(
                it,
                CoordinateUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                verTexStride,
                vertexBuffer
            )
        }
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