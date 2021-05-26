package com.example.opengldemo.pointcloud.utils

import android.content.Context
import android.opengl.Matrix
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.example.opengldemo.pointcloud.data.*
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue

object PointsUtil {

    /**
     *  list转数组
     */
    fun transformListToArray(list: ArrayList<Point3f>?): FloatArray {
        val pointList = ArrayList<Float>()
        list.let {
            for (point in list!!) {
                pointList.apply {
                    add(point.x)
                    add(point.y)
                    add(point.z)
                }
            }
        }
        return pointList.toFloatArray()
    }

    /**
     *  set转数组
     */
    fun transformSetToArray(list: Set<Point3f>): FloatArray {
        val pointList = ArrayList<Float>()
        list.let {
            for (point in list) {
                pointList.apply {
                    add(point.x)
                    add(point.y)
                    add(point.z)
                }
            }
        }
        return pointList.toFloatArray()
    }

    /**
     *  数组转list
     */
    fun transformArrayToList(floatArray: FloatArray): ArrayList<Point3f> {
        val pointList = ArrayList<Point3f>()
        floatArray.let {
            if (it.size % 3 == 0) {
                for (index in it.indices step 3) {
                    val vector3f = Point3f(it[index], it[index + 1], it[index + 2])
                    pointList.add(vector3f)
                }
            }
        }
        return pointList
    }

    /**
     *  获取框选立方体边框坐标
     */
    fun getCubeFacePoint(arrayList: ArrayList<FloatArray>): FloatArray {
        val list = ArrayList<Float>()
        for (index in 0 until arrayList.size) {
            arrayList[index].forEach {
                list.add(it)
            }
        }
        var step = 0
        val array = FloatArray(12)
        for (index in 0 until list.size) {
            if (index % 4 != 3) {
                array[step++] = list[index]
            }
        }
        return array
    }

    /**
     *  获取框选立方体内的3D点云坐标
     */
    fun getCubeCloudPoint(allCloudPoint: ArrayList<Point3f>, cubeArea: CubeArea): ArrayList<Float> {
        var cubeCloudPoint = ArrayList<Float>()
        allCloudPoint.forEach { point ->
            if (cubeArea.contains(point.x, point.y, point.z)) {
                cubeCloudPoint.apply {
                    add(point.x)
                    add(point.y)
                    add(point.z)
                }
            }
        }

        return cubeCloudPoint
    }

    /**
     *  获取球体中的内的3D点云坐标
     */
    fun getPointInSphere(pointList: ArrayList<Point3f>, radius: Float, centerPoint: FloatArray): ArrayList<Point3f> {
        val list = ArrayList<Point3f>()
        pointList.forEach { point ->
            val nearPointRay = Point3f(point.x, point.y, 1f)
            val farPointRay = Point3f(point.x, point.y, -1f)
            val ray = Ray(nearPointRay, vectorBetween(nearPointRay, farPointRay))

            val sphere = Sphere(Point3f(centerPoint[0], centerPoint[1], centerPoint[2]), radius)

            val isIntersect = intersects(sphere, ray)
            if (isIntersect) {
                val plane = Plane(Point3f(0f, 0f, 0f), Vector(0f, 0f, 1f))
                val intersectionPoint = intersectionPoint(ray, plane)
                list.add(intersectionPoint)
            }
        }
        return list
    }

    /**
     *  获取两点间点向量
     */
    fun vectorBetween(from: Point3f, to: Point3f): Vector {
        return Vector(
            to.x - from.x,
            to.y - from.y,
            to.z - from.z
        )
    }

    /**
     *  检测球体和射线是否相交
     */
    fun intersects(sphere: Sphere, ray: Ray): Boolean {
        val distance = distanceBetween(sphere.center, ray)
//        Log.i("", "----the distance is: $distance")
        val radius = sphere.radius
        return distance < radius
    }

    /**
     *  获取两点之间点距离
     */
    fun distanceBetween(point: Point3f, ray: Ray): Float {
        val p1ToPoint = vectorBetween(ray.point, point)
        val p2ToPoint = vectorBetween(ray.point.translate(ray.vector), point)

        val areaOfTriangleTimesTwo = p1ToPoint.crossProduct(p2ToPoint).length()
        val lengthOfBase = ray.vector.length()

        return areaOfTriangleTimesTwo / lengthOfBase
    }

    /**
     *  获取射线和平面相交坐标
     */
    fun intersectionPoint(ray: Ray, plane: Plane): Point3f {
        val rayToPlaneVector = vectorBetween(ray.point, plane.point)
        val scaleFactor = (rayToPlaneVector.dotProduct(plane.normal)
                / ray.vector.dotProduct(plane.normal))
        return ray.point.translate(ray.vector.scale(scaleFactor))
    }

    /**
     *  获取归一坐标
     */
    fun getNormalizedX(x: Float, width: Float) = (x / width * 2) - 1

    fun getNormalizedY(y: Float, height: Float) = 1 - (y / height * 2)

    /**
     *  获取归一坐标点对应点世界坐标点
     */
    fun getWorldPoint(
        normalizedX: Float,
        normalizedY: Float,
        invertedVPMatrix: FloatArray,
        isNear: Boolean
    ): Point3f =
        if (isNear) {
            val nearPointNdc = floatArrayOf(normalizedX, normalizedY, -1f, 1f)
            val nearPointWorld = FloatArray(4)
            Matrix.multiplyMV(nearPointWorld, 0, invertedVPMatrix, 0, nearPointNdc, 0)
            divideByW(nearPointWorld)

            Point3f(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2])
        } else {
            val farPointNdc = floatArrayOf(normalizedX, normalizedY, 1f, 1f)
            val farPointWorld = FloatArray(4)
            Matrix.multiplyMV(farPointWorld, 0, invertedVPMatrix, 0, farPointNdc, 0)
            divideByW(farPointWorld)

            Point3f(farPointWorld[0], farPointWorld[1], farPointWorld[2])
        }

    /**
     *  获取归一坐标点对应点世界坐标点, 需要model的逆矩阵
     */
    fun getWorldPointWithIMM(
        normalizedX: Float,
        normalizedY: Float,
        invertedVPMatrix: FloatArray,
        invertModelMatrix: FloatArray,
        isNear: Boolean
    ): Point3f =
        if (isNear) {
            val nearPointNdc = floatArrayOf(normalizedX, normalizedY, -1f, 1f)
            val nearPointWorld = FloatArray(4)
            Matrix.multiplyMV(nearPointWorld, 0, invertedVPMatrix, 0, nearPointNdc, 0)
            Matrix.multiplyMV(nearPointWorld, 0, invertModelMatrix, 0, nearPointWorld, 0)
            divideByW(nearPointWorld)

            Point3f(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2])
        } else {
            val farPointNdc = floatArrayOf(normalizedX, normalizedY, 1f, 1f)
            val farPointWorld = FloatArray(4)
            Matrix.multiplyMV(farPointWorld, 0, invertedVPMatrix, 0, farPointNdc, 0)
            Matrix.multiplyMV(farPointWorld, 0, invertModelMatrix, 0, farPointWorld, 0)
            divideByW(farPointWorld)

            Point3f(farPointWorld[0], farPointWorld[1], farPointWorld[2])
        }


    /**
     *  除以齐次坐标
     */
    fun divideByW(vector: FloatArray) {
        vector[0] /= vector[3]
        vector[1] /= vector[3]
        vector[2] /= vector[3]
    }



}