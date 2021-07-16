package com.example.opendemo.opengl.pointcloud

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.opendemo.R
import com.example.opendemo.opengl.pointcloud.data.Point3f
import kotlinx.android.synthetic.main.layout_point_related.*

/**
 *  3D点云Activity
 */
class PointCloudActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PointCloudActivity"
    }

    var mPreviousX: Float = 0f
    var mPreviousY: Float = 0f

    lateinit var mStartWorldPoint: Point3f
    lateinit var mEndWorldPoint: Point3f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud_point)
        initView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        //点云图长按
//        pointGLView.setOnLongPressListener(object : PointCloudGLView.OnLongPressListener{
//            override fun onLongPressDown(ex: Float, ey: Float, worldPoint: Geometry.Point) {
//                mPreviousX = ex
//                mPreviousY = ey
//                mStartWorldPoint = worldPoint
//                selectView.visibility = View.VISIBLE
//            }
//
//            override fun onLongPressMove(ex: Float, ey: Float) {
//                val rectF = RectF(mPreviousX, mPreviousY, ex, ey)
//                selectView.setActualRect(rectF)
//            }
//
//            override fun onLongPressUp(worldPoint: Geometry.Point) {
//                mEndWorldPoint = worldPoint
//
//                selectView.reset()
//                selectView.visibility = View.GONE
//                showCubeView()
//            }
//        })
    }

    /**
     *  长按拖动，手指抬起初始化立方体
     */
//    private fun showCubeView() {
//        cubeView.visibility = View.VISIBLE
//        cubeView.init(mStartWorldPoint, mEndWorldPoint)
//        Log.i(TAG, "Long_Press_up: startX= ${mStartWorldPoint.x}, startY= ${mStartWorldPoint.y}, endX= ${mEndWorldPoint.x}, endY= ${mEndWorldPoint.y}")
//    }

    override fun onResume() {
        super.onResume()
        pointGLView.onResume()
    }

    override fun onPause() {
        super.onPause()
        pointGLView.onPause()
    }




}