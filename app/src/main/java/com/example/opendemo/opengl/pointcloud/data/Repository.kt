package com.example.opendemo.opengl.pointcloud.data

import android.content.Context
import android.util.Log
import androidx.lifecycle.liveData
import com.example.opendemo.opengl.pointcloud.utils.PointsUtil
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue

/**
 *  请求数据
 */
object Repository {

    //光源放大系数
    private const val LIGHT_INNER_ENLARGE = 0.5f
    private const val LIGHT_MIDDLE_ENLARGE = 0.7f
    private const val LIGHT_OUT_ENLARGE = 0.9f

    /**
     *  获取光源的Mock数据
     */
    fun getMockLightSourcePoint(): ConcentricRingArray {
        val list = ArrayList<Point3f>()
        var x = 0f
        var y = 0f
        var z = 0f
        for (i in -100..100) {
            x = i.toFloat() / 100
            for (j in -100..100) {
                y = j.toFloat() / 100
                list.add(Point3f(x, y, z))
            }
        }
        Log.i("LIST_SIZE", "light= ${list.size}")

        var concentricRingPoint: ConcentricRingArray? = null
        runBlocking {
            concentricRingPoint = getConcentricRingPoint(list)
        }

        return concentricRingPoint!!
    }

    /**
     * 获取光源3个同心环点集
     */
    private suspend fun getConcentricRingPoint(pointList: ArrayList<Point3f>): ConcentricRingArray? {
        var concentricCircles: ConcentricCircles?
        var concentricRingList: ConcentricRingList?
        var concentricRingArray: ConcentricRingArray?
        val centerPoint = floatArrayOf(0f, 0f, 0f, 1f)

        val job = Job()
        val scope = CoroutineScope(job)

        return suspendCoroutine {
            scope.launch(Dispatchers.IO) {
                val start = System.currentTimeMillis()

                val listInner = async {
                    val radius = 0.7f / 4
                    PointsUtil.getPointInSphere(pointList, radius, centerPoint)
                }

                val listMiddle = async {
                    val radius = 1.8f / 4
                    PointsUtil.getPointInSphere(pointList, radius, centerPoint)
                }

                val listOut = async {
                    val radius = 4.0f / 4
                    PointsUtil.getPointInSphere(pointList, radius, centerPoint)
                }

                concentricCircles = ConcentricCircles(listInner.await(), listMiddle.await(), listOut.await())

                concentricCircles.let { concentricCircles ->
                    enlargeConcentricCirclesPoint(concentricCircles!!)

                    concentricRingList = ConcentricRingList(concentricCircles.circleInner,
                        (concentricCircles.circleMiddle - concentricCircles.circleInner) as ArrayList<Point3f>,
                        (concentricCircles.circleOut - concentricCircles.circleMiddle) as ArrayList<Point3f>
                    )
                }

                concentricRingList.let { concentricRingList ->
                    concentricRingArray = ConcentricRingArray(
                        PointsUtil.transformListToArray(concentricRingList!!.ringInner),
                        PointsUtil.transformListToArray(concentricRingList.ringMiddle),
                        PointsUtil.transformListToArray(concentricRingList.ringOut)
                    )
                }

                it.resume(concentricRingArray)

                Log.i("GET_LIGHT_POINT_COST  ", "${System.currentTimeMillis() - start}")
            }
        }
    }

    /**
     *  放大同心圆里的点
     */
    private fun enlargeConcentricCirclesPoint(concentricCircle: ConcentricCircles) {
        concentricCircle.circleInner.forEach { point3f ->
            point3f.x *= LIGHT_INNER_ENLARGE
            point3f.y *= LIGHT_INNER_ENLARGE
        }

        concentricCircle.circleMiddle.forEach { point3f ->
            point3f.x *= LIGHT_MIDDLE_ENLARGE
            point3f.y *= LIGHT_MIDDLE_ENLARGE
        }

        concentricCircle.circleOut.forEach { point3f ->
            point3f.x *= LIGHT_OUT_ENLARGE
            point3f.y *= LIGHT_OUT_ENLARGE
        }
    }

    /**
     *  获取Pcd文件坐标
     */
    fun getCloudPointFromAssets(context: Context) = fire(Dispatchers.IO) {
        var start = System.currentTimeMillis()
        var points = ArrayList<Point3f>()

        val inputReader = InputStreamReader(context.resources.assets.open("pcd/1.pcd"))
        val bufReader = BufferedReader(inputReader)
        var line: String?

        var lineNum = 0
        while (bufReader.readLine().also { line = it } != null) {
            if (++lineNum < 12) continue
            var pointValue = line?.split(" ")
            if (pointValue != null && pointValue.size >= 3) {
                points.add(
                    Point3f(
                        pointValue[0].toFloat(),
                        pointValue[1].toFloat(),
                        pointValue[2].toFloat()
                    )
                )
            }
        }

        val top: Float = points.maxBy { it.y }?.y?.absoluteValue!!
        val bottom: Float = points.minBy { it.y }?.y?.absoluteValue!!
        val left: Float = points.maxBy { it.x }?.y?.absoluteValue!!
        val right: Float = points.minBy { it.x }?.x?.absoluteValue!!

        val maxValue = maxOf(maxOf(top, bottom), maxOf(left, right))

        for (item in points) {
            item.x /= maxValue
            item.y /= maxValue
            item.z /= maxValue
        }

        PointData.allCloudPoint = points
        Log.d("GET_PCD_POINT_COST", ("${System.currentTimeMillis() - start}"))

        Result.success(PointsUtil.transformListToArray(points))
    }



    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) =
        liveData<Result<T>>(context) {
            val result = try {
                block()
            } catch (e: Exception) {
                Result.failure<T>(e)
            }
            emit(result)
        }
}