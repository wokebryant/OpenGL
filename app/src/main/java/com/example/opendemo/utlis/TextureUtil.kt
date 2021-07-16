package com.example.opendemo.utlis

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils

/**
 *  纹理工具类
 */
object TextureUtil {

    var bitmapInfo: BitmapInfo? = null

    /**
     * 加载纹理
     */
    fun loadTexture(context: Context, resId: Int): Int {
        val textureArray = IntArray(1)
        //创建一个纹理对象，即textureArray
        GLES20.glGenTextures(1, textureArray, 0)
        if (0 == textureArray[0]) return 0
        val bitmapOptions = BitmapFactory.Options()
        bitmapOptions.inScaled = false
        val bitmap = BitmapFactory.decodeResource(context.resources, resId, bitmapOptions)
        if (bitmap == null) {
            GLES20.glDeleteTextures(1, textureArray, 0)
            return 0
        } else {
            bitmapInfo = BitmapInfo(bitmap.width, bitmap.height)
        }

        //绑定纹理对象到OpenGl
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureArray[0])

        //设置默认的纹理过滤参数
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        //加载bitmap到纹理中
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        //生成MIP贴图
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)

        //数据如果已经被加载进OpenGL，则可以回收bitmap
        bitmap.recycle()

        //取消绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        //返回纹理对象
        return textureArray[0]
    }


}

data class BitmapInfo(val bitmapWidth: Int, val bitmapHeight: Int)