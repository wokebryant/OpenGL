package com.example.opendemo.opengl.shape

import com.example.opendemo.utlis.BufferUtil
import com.example.opendemo.utlis.ColorUtil
import com.example.opendemo.utlis.CoordinateUtil

/**
 *  绘制立方体
 */
class Cube {

    private val verTexBuffer = BufferUtil.getFloatBuffer(CoordinateUtil.cubeCoords)
    private val colorsBuffer = BufferUtil.getFloatBuffer(ColorUtil.cubeColors)

    init {

    }


}