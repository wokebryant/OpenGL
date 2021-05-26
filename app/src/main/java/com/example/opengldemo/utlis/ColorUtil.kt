package com.example.opengldemo.utlis

/**
 *  颜色相关
 */
object ColorUtil {

    const val COLOR_PER_VERTEX = 4

    /**
     *  单一颜色，使用RGB色值和Alpha设置颜色
     */
    val color = floatArrayOf(
        0.63671875f, 0.76953125f, 0.22265625f, 1.0f
    )

    /**
     *  渐变色，每个顶点设置不同的颜色
     */
    val gradientColor = floatArrayOf(
        1f, 0f, 0f, 1f, // vertex 0 red
        0f, 1f, 0f, 1f, // vertex 1 green
        0f, 0f, 1f, 1f, // vertex 2 blue
        1f, 0f, 1f, 1f // vertex 3 magenta
    )

    /**
     *  定义立方体每一个顶点的颜色
     */
    val cubeColors = floatArrayOf(

    )
}