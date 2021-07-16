package com.example.opendemo.opengl.pointcloud.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.opengl.Matrix
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.opendemo.opengl.pointcloud.data.*
import com.example.opendemo.opengl.pointcloud.renderer.PointCloudGLRenderer
import com.example.opendemo.opengl.pointcloud.utils.PointsUtil
import kotlinx.coroutines.*

/**
 *  3D点云视图
 */
@SuppressLint("ClickableViewAccessibility")
class PointCloudGLView(context: Context, attrs: AttributeSet) : BaseGLSurfaceView(context, attrs) {

    companion object {
        private const val TAG = "PointCloudGLView"

        private const val SINGLE_POINT = 1
        private const val DOUBLE_POINT = 2
    }

    private val viewModel by lazy {
        ViewModelProviders.of(context as FragmentActivity, ViewModelFactory()
        ).get(GLViewModel::class.java)
    }

    private val scope = CoroutineScope(Job())
    private val mRenderer: PointCloudGLRenderer = PointCloudGLRenderer()

    private lateinit var longPressListener: OnLongPressListener
    private lateinit var smearListener: OnSmearListener
    private lateinit var stateChangeListener: OnStateChangeListener

    private lateinit var downPressPointF: PointF

    private var isLongPress = false
    private var isDownPress = false
    private var isSmear = false


    init {
        mRenderer.context = context
        setRenderer(mRenderer)
        renderMode = RENDERMODE_WHEN_DIRTY

        setOnTouchListener { view, motionEvent ->
            handleTouch(view, motionEvent)
        }

        viewModel.getCloudPointData(context)
        viewModel.cloudPointData.observe(context as FragmentActivity, Observer {
            if (it != null) {
                val points = it.getOrNull()
                if (points != null) {
                    mRenderer.updateVertexPoint(points)
                    requestRender()
                }
            }
        })
    }

    /**
     *  处理触摸事件
     */
    private fun handleTouch(view: View, e: MotionEvent): Boolean {
        val pointCount = e.pointerCount

        val normalizedX: Float = PointsUtil.getNormalizedX(e.x, view.width.toFloat())
        val normalizedY: Float = PointsUtil.getNormalizedY(e.y, view.height.toFloat())

        val worldPoint = PointsUtil.getWorldPoint(normalizedX, normalizedY, mRenderer.getInvertVPMatrix(), true)

        when (e.action and ACTION_MASK) {
            ACTION_DOWN  -> {
                Log.i(TAG, "ACTION= DOWN")
                isDownPress = true
                downPressPointF = PointF(e.x, e.y)
                stateChangeListener.onTapDown()
            }

            //屏幕上已经有一个点按住 再按下一点时触发该事件
            ACTION_POINTER_DOWN -> {
                Log.i(TAG, "ACTION= POINTER_DOWN")
                isDownPress = false
                if (isSmear) {
                    smearListener.onSmearFinish()
                }

                val centerPoint = getCenterPoint(e, view)
                mRenderer.handleScale(getSpacing(e), true)
                mRenderer.handleTranslate(centerPoint.x, centerPoint.y, true)
            }

            //屏幕上已经有两个点按住 再松开一点时触发该事件
            ACTION_POINTER_UP -> {
                Log.i(TAG, "ACTION= POINTER_UP")
                isDownPress = false
            }

            ACTION_MOVE -> {
                Log.i(TAG, "ACTION= MOVE")

                when (pointCount) {
                    SINGLE_POINT -> {
                        if (isLongPress) {
                            longPressListener.onLongPressMove(e.x, e.y)
                        } else {
                            if (isDownPress) {
                                smearListener.onSmearDoing(e,
                                    downPressPointF,
                                    mRenderer.getInvertVPMatrix(),
                                    mRenderer.getInvertModelMatrix())
                                isSmear = true
                            }
                        }
                    }

                    DOUBLE_POINT -> {
                        val isScale = isScale(getSpacing(e), mRenderer.getPreviousSpace())
                        if (isScale) {
                            mRenderer.handleScale(getSpacing(e), false)
                        } else {
                            val centerPoint = getCenterPoint(e, view)
                            mRenderer.handleTranslate(centerPoint.x, centerPoint.y, false)
                        }
                        requestRender()

                        Log.i(TAG, "isScale= $isScale, cSpace= ${getSpacing(e)}, pSpace= ${mRenderer.getPreviousSpace()}")
                    }
                }
            }

            ACTION_UP -> {
                Log.i(TAG, "ACTION= UP")

                if (isLongPress) {
                    updateCloudPoint()
                    longPressListener.onLongPressUp(worldPoint)
                    isLongPress = false
                }

                if (isSmear) {
                    smearListener.onSmearFinish()
                    isSmear = false
                }
            }
        }
        return true
//        return gestureDetector.onTouchEvent(e)
    }

    /**
     *  获取双指触摸中心坐标
     */
    private fun getCenterPoint(e: MotionEvent, view: View): CenterPoint {
        val normalizedX0 = PointsUtil.getNormalizedX(e.getX(0), view.width.toFloat())
        val normalizedY0 = PointsUtil.getNormalizedY(e.getY(0), view.height.toFloat())
        val normalizedX1 = PointsUtil.getNormalizedX(e.getX(1), view.width.toFloat())
        val normalizedY1 = PointsUtil.getNormalizedY(e.getY(1), view.height.toFloat())

        val worldPoint0 = PointsUtil.getWorldPoint(normalizedX0, normalizedY0, mRenderer.getInvertVPMatrix(), true)
        val worldPoint1 = PointsUtil.getWorldPoint(normalizedX1, normalizedY1, mRenderer.getInvertVPMatrix(), true)

        val centerPointX = (worldPoint0.x + worldPoint1.x) / 2
        val centerPointY = (worldPoint1.y + worldPoint1.y) / 2

        return CenterPoint(centerPointX, centerPointY)
    }

    /**
     *  根据model矩阵计算变换后的3D点云世界坐标
     */
    private fun updateCloudPoint() {
        scope.launch(Dispatchers.IO) {
            val newPointList = ArrayList<Point3f>()
            for (point in PointData.allCloudPoint) {
                val pointArray = floatArrayOf(point.x, point.y, point.z, 1.0f)
                Matrix.multiplyMV(pointArray, 0, mRenderer.getModelMatrix(), 0, pointArray, 0)
                val newPoint = Point3f(pointArray[0], pointArray[1], pointArray[2])
                newPointList.add(newPoint)
            }
            PointData.allCloudPoint = newPointList
        }
    }

    /**
     *  处理长按事件
     */
    private val gestureDetector = GestureDetector(getContext(), object : GestureDetector.OnGestureListener{

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onShowPress(e: MotionEvent?) {

        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return true
        }


        override fun onLongPress(e: MotionEvent?) {
            isLongPress = true
            e.let {
                val normalizedX: Float = PointsUtil.getNormalizedX(it!!.x, width.toFloat())
                val normalizedY: Float = PointsUtil.getNormalizedY(it.y, height.toFloat())
                val worldPoint = PointsUtil.getWorldPoint(normalizedX, normalizedY, mRenderer.getInvertVPMatrix(), true)

                longPressListener .onLongPressDown(it!!.x, it.y, worldPoint)
            }
            Log.i(TAG, "longPress")
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            return true
        }

    })

    /**
     *  处理选中的点集
     */
    fun updateSmearView() {
        mRenderer.handelSmearPoint()
        requestRender()
        Log.i(TAG, "show end")
    }

    /**
     *  展示涂抹区域中正确的点集
     */
    fun showCorrectPartView() {
        mRenderer.clearSmearPoint()
        mRenderer.handelCorrectPartPoint()
        requestRender()
    }

    /**
     *  展示正确点集
     */
    fun showCorrectAnswerView() {
        mRenderer.clearSmearPoint()
        mRenderer.handelCorrectAnswerPoint()
        requestRender()
    }

    fun setOnLongPressListener(listener: OnLongPressListener) {
        longPressListener = listener
    }

    fun setOnSmearListener(listener: OnSmearListener) {
        smearListener = listener
    }

    fun setOnStateChangeListener(listener: OnStateChangeListener) {
        stateChangeListener = listener
    }

    /**
     *  e: 屏幕坐标， w:世界坐标
     */
    interface OnLongPressListener {
        fun onLongPressDown(ex: Float, ey: Float, worldPoint: Point3f)

        fun onLongPressMove(ex: Float, ey: Float)

        fun onLongPressUp(worldPoint: Point3f)
    }

    /**
     * 涂抹监听
     */
    interface OnSmearListener {
        fun onSmearDoing(event: MotionEvent, pointF: PointF, invertVPMatrix: FloatArray, invertModelMatrix: FloatArray)

        fun onSmearFinish()
    }

    /**
     *  点云触碰状态监听
     */
    interface OnStateChangeListener {
        fun onTapDown()
    }

}