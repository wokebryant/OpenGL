package com.example.opendemo.opengl.pointcloud.renderer

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.opendemo.opengl.pointcloud.data.*
import com.example.opendemo.opengl.pointcloud.utils.PointsUtil
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.properties.Delegates
open abstract class BaseGLRenderer : GLSurfaceView.Renderer {

    companion object {
        private const val TAG = "BaseGLRenderer"
        const val POINT_PER_VERTEX = 3
        const val POINT_PER_COLOR = 4
        const val POINT_PER_TEXTURE = 2
    }

    //旋转
    private var isIntersect = false
    private var previousPoint: Point3f? = null

    //缩放
    private var previousSpace: Float = 0f
    //平移
    private var previousTransX: Float = 0f
    private var previousTransY: Float = 0f

    private var invertVpMatrix = FloatArray(16)
    private var modelMatrix = FloatArray(16)

    //颜色
    val greenColor = floatArrayOf(0f, 1f, 0f, 0f)
    val redColor = floatArrayOf(1f, 0f, 0f, 0f)
    val blueColor = floatArrayOf(0f, 0f, 1f, 0f)
    val whiteColor = floatArrayOf(1f, 1f, 1f, 0f)
    val yellowColor = floatArrayOf(0.92f, 1f, 0f, 0f)
    val purpleColor = floatArrayOf(1.0f, 0f, 1.0f, 0f)
    val lightColorInner = floatArrayOf(0.5f, 0.5f, 0.5f, 0f)
    val lightColorMiddle = floatArrayOf(0.35f, 0.35f, 0.35f, 0f)
    val lightColorOut = floatArrayOf(0.2f, 0.2f, 0.2f, 0f)

    //着色器句柄
    var mMatrixHandle: Int = 0
    var mVertexHandle: Int = 0
    var mColorHandle: Int = 0
    var mTextureHandle: Int = 0

    //着色器程序
    var mProgram by Delegates.notNull<Int>()

    @Volatile
    lateinit var context: Context

    //旋转角度
    @Volatile
    var angleX: Float = 0f
    var angleY: Float = 0f

    //缩放比例
    @Volatile
    var scale: Float = 4.0f

    //平移距离
    @Volatile
    var transX: Float = 0.0f
    @Volatile
    var transY: Float = 0.0f
    @Volatile
    var transZ: Float = 0.0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        initPoints()
        initProgram()
        initHandle()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
    }

    //初始化顶点坐标
    abstract fun initPoints()

    //初始化着色器
    abstract fun initProgram()

    //初始化句柄
    abstract fun initHandle()

    fun setInvertVPMatrix(invertMatrix: FloatArray) {
        this.invertVpMatrix = invertMatrix
    }

    fun setModelMatrix(modelMatrix: FloatArray) {
        this.modelMatrix = modelMatrix
    }

    /**
     * 处理旋转
     */
    open fun handleRotate(normalizedX: Float, normalizedY: Float, isDownPress: Boolean) {
        if (isDownPress) {
            previousPoint = null
        }

        //设置近平面坐标和远平面坐标
        val nearPointNdc = floatArrayOf(normalizedX, normalizedY, -1.0f, 1.0f)
        val farPointNdc = floatArrayOf(normalizedX, normalizedY, 1.0f, 1.0f)

        val nearPointWorld = FloatArray(4)
        val farPointWorld = FloatArray(4)

        //通过vp逆矩阵求得虚拟空间的两个点
        Matrix.multiplyMV(nearPointWorld, 0, invertVpMatrix, 0, nearPointNdc, 0)
        Matrix.multiplyMV(farPointWorld, 0, invertVpMatrix, 0, farPointNdc, 0)

        PointsUtil.divideByW(nearPointWorld)
        PointsUtil.divideByW(farPointWorld)

        //两个点连成一条射线
        val nearPointRay = Point3f(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2])
        val farPointRay = Point3f(farPointWorld[0], farPointWorld[1], farPointWorld[2])

        val ray = Ray(nearPointRay, PointsUtil.vectorBetween(nearPointRay, farPointRay))

        val centerPoint = floatArrayOf(-0f, -0.0f, 0.0f, 1f)
        Matrix.multiplyMV(centerPoint, 0, modelMatrix, 0, centerPoint, 0)

        //射线和检测物体做相交测试，求得交点（将检测物体看成一个球体）
        val sphere = Sphere(Point3f(centerPoint[0], centerPoint[1], centerPoint[2]), 0.5f)
        isIntersect = PointsUtil.intersects(sphere, ray)
        if (isIntersect) {
            val plane = Plane(Point3f(0f, 0f, 0f), Vector(0f, 0f, 1f))
            val intersectionPoint = PointsUtil.intersectionPoint(ray, plane)
            if (previousPoint != null) {
                angleX += (intersectionPoint.x - previousPoint!!.x) * 500f
                angleY += (intersectionPoint.y - previousPoint!!.y) * 500f
            }
            previousPoint = intersectionPoint
            Log.i(
                TAG,
                " nearPoint: " + nearPointRay.x.toString() + " " + nearPointRay.y.toString() + " " + nearPointRay.z.toString() +
                        " farPoint: " + farPointRay.x.toString() + " " + farPointRay.y.toString() + " " + farPointRay.z.toString() +
                        " insertPoint= " + intersectionPoint.x.toString() + " " + intersectionPoint.y.toString() + " " + intersectionPoint.z
            )
        }
    }

    fun getPreviousSpace() = previousSpace

    /**
     *  处理缩放
     */
    fun handleScale(currentSpace: Float, isDownPress: Boolean) {
        if (isDownPress) {
            previousSpace = currentSpace
        } else {
            scale *= if (currentSpace > previousSpace) {
                1.2f
            } else {
                0.8f
            }
            previousSpace = currentSpace
        }
    }

    /**
     *  处理平移
     */
    fun handleTranslate(currentTransX: Float, currentTransY: Float, isDownPress: Boolean) {
        if (isDownPress) {
            previousTransX = currentTransX
            previousTransY = currentTransY
        } else {
//            if (abs(currentTransX - previousTransX) < 0.01 || abs(currentTransY - previousTransY) < 0.01) {
//                return
//            }
            transX += (currentTransX - previousTransX) / scale
            transY += (currentTransY - previousTransY) / scale
            previousTransX = currentTransX
            previousTransY = currentTransY
            Log.i(
                TAG,
                "transX= $transX, transY= $transY, scale= $scale, anglex= $angleX, angleY= $angleY"
            )

        }
    }

}