package com.example.opengldemo.pointcloud.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.example.opengldemo.R
import com.example.opengldemo.pointcloud.data.PaletteMode
import kotlinx.android.synthetic.main.activity_cloud_point.*
import kotlinx.android.synthetic.main.layout_check_3d_mark_answer.*
import kotlinx.android.synthetic.main.layout_check_3d_mark_answer.view.*
import kotlinx.android.synthetic.main.layout_point_related.view.*

/**
 *  2D关联视图
 */
class PointRelatedView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attributeSet, defStyleAttr){

    companion object {
        private const val TAG = "PointRelatedView"
    }

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_point_related, this)
        initView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        //点云涂抹
        pointGLView.setOnSmearListener(object : PointCloudGLView.OnSmearListener{

            override fun onSmearDoing(event: MotionEvent,
                                      pointF: PointF,
                                      invertVPMatrix: FloatArray,
                                      invertModelMatrix: FloatArray) {
                paletteView.apply {
                    visibility = View.VISIBLE
                    setPaintStartPoint(pointF)
                    setInvertMatrix(invertVPMatrix, invertModelMatrix)
                    handelTouchEvent(event)
                }
            }

            override fun onSmearFinish() {
                paletteView.apply {
                    finishSmear()
                    visibility = View.GONE
                }
            }
        })

        //点云状态监听
        pointGLView.setOnStateChangeListener(object : PointCloudGLView.OnStateChangeListener{
            override fun onTapDown() {
                panoramaView.setIsShow()
            }

        })

        //涂抹板切换按钮
        paletteSwitchView.setOnClickListener(object : PaletteSwitchView.OnClickListener{
            override fun onPenClick() {
                paletteView.setMode(PaletteMode.DRAW)
            }

            override fun onEraserClick() {
                paletteView.setMode(PaletteMode.ERASER)
            }
        })

        //涂抹板获取轨迹对应的点集
        paletteView.setCallBack(object : PaletteView.Callback{
            override fun updatePointGLView() {
                pointGLView.updateSmearView()
            }
        })

        //答案检查横条
        answerCheckTv.setOnLongClickListener {
            pointGLView.showCorrectAnswerView()
            Log.i(TAG, "action_longPress")
            true
        }
        answerCheckTv.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_CANCEL -> {
                    pointGLView.showCorrectPartView()
                    Log.i(TAG, "action_cancel")
                }
            }

            return@setOnTouchListener false
        }
    }

    fun onResume() {
        pointGLView.onResume()
    }

    fun onPause() {
        pointGLView.onPause()
    }

}