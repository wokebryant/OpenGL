package com.example.opengldemo.shape

import com.example.opengldemo.utlis.BufferUtil
import com.example.opengldemo.utlis.ColorUtil
import com.example.opengldemo.utlis.CoordinateUtil

/**
 *  绘制立方体
 */
class Cube {

    private val verTexBuffer = BufferUtil.getFloatBuffer(CoordinateUtil.cubeCoords)
    private val colorsBuffer = BufferUtil.getFloatBuffer(ColorUtil.cubeColors)

    init {

    }


}