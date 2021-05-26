package com.example.opengldemo.utlis

/**
 *  坐标相关
 */
object CoordinateUtil{

    //数组中每个顶点的坐标数
    const val COORDS_PER_VERTEX = 3
    const val COORDS_PER_TEXTURE = 2

    //顶点坐标绘制顺序(两个三角形)，逆时针
    val drawOrder = shortArrayOf(
        0, 1, 2, 0, 2, 3
    )

    //三角形坐标
    val triangleCoords = floatArrayOf( //按逆时针顺序
        0.0f, 0.622008459f, 0.0f,      // 顶部
        -0.5f, -0.311004243f, 0.0f,    // 左下
        0.5f, -0.311004243f, 0.0f      // 右下
    )

    //正方形坐标
    val squareCoords = floatArrayOf(
        -0.5f,  0.5f, 0.0f,      // top left
        -0.5f, -0.5f, 0.0f,      // bottom left
        0.5f, -0.5f, 0.0f,      // bottom right
        0.5f,  0.5f, 0.0f       // top right
    )

    /**
     *  立方体坐标
     */
    val cubeCoords = floatArrayOf(

    )

    //纹理坐标
    //纹理坐标系和opengl es不同，和android视图坐标系相同
    val textureCoords = floatArrayOf(
        0f, 0f, //纹理坐标V0
        0f, 1.0f,     //纹理坐标V1
        1.0f, 1.0f,     //纹理坐标V2
        1.0f, 0f   //纹理坐标V3
    )




}