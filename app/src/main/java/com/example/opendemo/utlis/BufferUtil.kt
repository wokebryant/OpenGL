package com.example.opendemo.utlis

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 *  数据缓冲
 */
object BufferUtil {

    fun getFloatBuffer(floatArray: FloatArray) =
        ByteBuffer.allocateDirect(floatArray.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(floatArray)
                position(0)
            }
    }

    fun getShortBuffer(shortArray: ShortArray) =
        ByteBuffer.allocateDirect(shortArray.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(shortArray)
                position(0)
            }
    }

}