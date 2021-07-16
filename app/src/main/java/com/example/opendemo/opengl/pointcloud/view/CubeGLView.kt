package com.example.opendemo.opengl.pointcloud.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.opendemo.opengl.pointcloud.data.Control
import com.example.opendemo.opengl.pointcloud.data.Point3f
import com.example.opendemo.opengl.pointcloud.data.contains
import com.example.opendemo.opengl.pointcloud.renderer.CubeRenderer
import com.example.opendemo.opengl.pointcloud.utils.PointsUtil

@SuppressLint("ClickableViewAccessibility")
class CubeGLView(context: Context, attrs: AttributeSet) : BaseGLSurfaceView(context, attrs) {

    companion object {
        private const val TAG = "CubeGLView"
    }

    private var controlType: Control = Control.NONE

    private lateinit var mRenderer: CubeRenderer

    fun init(startPoint: Point3f, endPoint: Point3f) {
        mRenderer = CubeRenderer()
        mRenderer.context = context
        mRenderer.setDrawFacePoint(startPoint, endPoint)
        setRenderer(mRenderer)
        renderMode = RENDERMODE_WHEN_DIRTY

        setOnTouchListener{ view, motionEvent ->
            handleTouch(view, motionEvent)
        }


    }

    /**
     *  处理触摸事件
     */
    private fun handleTouch(view: View, e: MotionEvent): Boolean {
        val pointCount = e.pointerCount
        //将Android坐标转换为OpenGL归一坐标
        val normalizedX: Float = PointsUtil.getNormalizedX(e.x, view.width.toFloat())
        val normalizedY: Float = PointsUtil.getNormalizedY(e.y, view.height.toFloat())

        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                controlType = getControlType(normalizedX, normalizedY)
                queueEvent {
                    mRenderer.handleScale(0f, true)
                    mRenderer.handleRotate(normalizedX, normalizedY, true)
                    mRenderer.handelViewChange(controlType, normalizedX, normalizedX, true)
//                    mRenderer.handleTranslate(normalizedX, normalizedY, true)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (pointCount > 1) {
                    mRenderer.handleScale(getSpacing(e), false)
                } else {
                    if (Control.NONE == controlType) {
                        mRenderer.handleRotate(normalizedX, normalizedY, false)
//                        mRenderer.handleTranslate(normalizedX, normalizedY, false)
                    } else {
                        mRenderer.handelViewChange(controlType, normalizedX, normalizedX, false)
                    }
                }
                requestRender()
            }
        }
        return true
    }

    /**
     *  获取触碰边框类别
     */
    private fun getControlType(normalizedX: Float, normalizedY: Float): Control {
        val worldPoint = PointsUtil.getWorldPoint(
            normalizedX,
            normalizedY,
            mRenderer.getInvertVPMatrix(),
            true
        )
        val x = worldPoint.x
        val y = worldPoint.y

        var controlType = Control.NONE

        when {
            //主视图触碰
            mRenderer.mainLeftTouchArea.contains(x, y) -> {
                controlType = Control.MAIN_LEFT
            }
            mRenderer.mainRightTouchArea.contains(x, y) -> {
                controlType = Control.MAIN_RIGHT
            }
            mRenderer.mainTopTouchArea.contains(x, y) -> {
                controlType = Control.MAIN_TOP
            }
            mRenderer.mainBottomTouchArea.contains(x, y) -> {
                controlType = Control.MAIN_BOTTOM
            }
            //左视图触碰
            mRenderer.leftLeftTouchArea.contains(x, y) -> {
                controlType = Control.LEFT_LEFT
            }
            mRenderer.leftRightTouchArea.contains(x, y) -> {
                controlType = Control.LEFT_RIGHT
            }
            mRenderer.leftTopTouchArea.contains(x, y) -> {
                controlType = Control.LEFT_TOP
            }
            mRenderer.leftBottomTouchArea.contains(x, y) -> {
                controlType = Control.LEFT_BOTTOM
            }
            //俯视图触碰
            mRenderer.topLeftTouchArea.contains(x, y) -> {
                controlType = Control.TOP_LEFT
            }
            mRenderer.topRightTouchArea.contains(x, y) -> {
                controlType = Control.TOP_RIGHT
            }
            mRenderer.topTopTouchArea.contains(x, y) -> {
                controlType = Control.TOP_TOP
            }
            mRenderer.topBottomTouchArea.contains(x, y) -> {
                controlType = Control.TOP_BOTTOM
            }
        }
        Log.i(TAG, " controlType= $controlType")
        return controlType
    }




}