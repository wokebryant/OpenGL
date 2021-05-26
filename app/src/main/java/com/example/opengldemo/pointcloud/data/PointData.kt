package com.example.opengldemo.pointcloud.data

/**
 *  全局点云数据
 */
object PointData {

    /**
     *  点云
     */
    @Volatile
    var allCloudPoint: ArrayList<Point3f> = ArrayList()

    /**
     *  光源点集
     */
    @Volatile
    var lightSourcePoint: ArrayList<Point3f> = ArrayList()

    /**
     *  涂抹点集
     */
    @Volatile
    var selectedSmearPoint: FloatArray = floatArrayOf()

    /**
     *  涂抹点集中的正确区域
     */
    @Volatile
    var correctPartPoint: FloatArray = floatArrayOf()

    /**
     *  正确区域
     */
    @Volatile
    var correctAnswerPoint: FloatArray = floatArrayOf()

}