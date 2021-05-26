package com.example.opengldemo.pointcloud.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.opengldemo.pointcloud.data.CubeArea
import com.example.opengldemo.pointcloud.data.PaletteMode
import com.example.opengldemo.pointcloud.data.Point3f
import com.example.opengldemo.pointcloud.data.PointData
import com.example.opengldemo.pointcloud.utils.PointsUtil
import com.example.opengldemo.pointcloud.utils.UIUtil
import kotlinx.coroutines.*


/**
 *  涂抹画板
 */
class PaletteView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attributeSet, defStyleAttr) {

    companion object {
        private const val TAG = "PaletteView"

        private const val MOCK_Z_POINT_FRONT = 0.1f
        private const val MOCK_Z_POINT_BACK = -0.1f
    }

    private var width: Int? = 0
    private var height: Int? = 0

    private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private var mPath: Path = Path()

    private var mPreviousX: Float = 0f
    private var mPreviousY: Float = 0f

    private var mBufferBitmap: Bitmap? = null
    private var mBufferCanvas: Canvas? = null

    private var mDrawSize = UIUtil.dip2px(20.0f)
    private var mEraserSize = UIUtil.dip2px(20.0f)

    private var mDrawColor  = Color.parseColor("#99EBFF00")
    private var mEraserColor = Color.parseColor("#cc8b2b93")

    private var mCanEraser = false
    private var mMode = PaletteMode.DRAW

    private lateinit var mCallBack: Callback

    private val scope = CoroutineScope(Job())
    private var selectedPointArray: FloatArray = floatArrayOf()
    private var removePointArray: FloatArray = floatArrayOf()
    //move事件每执行一次，就更新起始和结束坐标
    private var mInvertVpMatrix: FloatArray? = null
    private var mInvertModelMatrix: FloatArray? = null
    private lateinit var mStartWorldPoint: Point3f
    private lateinit var mEndWorldPoint: Point3f


    /**
     *  初始化Paint
     */
    init {
        isDrawingCacheEnabled = true

        mPaint.apply {
            style = Paint.Style.STROKE
            isFilterBitmap = true
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = mDrawSize.toFloat()
            color = mDrawColor
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        }
        post {
            width = getWidth()
            height = getHeight()
            visibility = GONE
            Log.i(TAG, "width= $width, height= $height")
        }
    }

    /**
     *  初始化缓存
     */
    private fun initBuffer() {
        mBufferBitmap = Bitmap.createBitmap(width!!, height!!, Bitmap.Config.ARGB_8888)
        mBufferCanvas = Canvas(mBufferBitmap!!)
    }


    /**
     *  设置模式, 涂抹or擦除
     */
    fun setMode(mode: PaletteMode) {
        if (mode != mMode) {
            mMode = mode
            if (mMode == PaletteMode.DRAW) {
                mPaint.apply {
                    strokeWidth = mDrawSize.toFloat()
                    color = mDrawColor
                }
            } else {
                mPaint.apply {
                    strokeWidth = mEraserSize.toFloat()
                    color = mEraserColor
                }
            }
        }
    }

    /**
     *  设置画笔大小
     */
    fun setPenSize(size: Int) {
        mDrawSize = size
    }

    /**
     *  设置橡皮擦大小
     */
    fun setEraserSize(size: Int) {
        mEraserSize = size
    }

    /**
     *  设置画笔颜色
     */
    fun setPenColor(color: Int) {
        mDrawColor = color
    }

    /**
     *  设置橡皮擦颜色
     */
    fun setEraserColor(color: Int) {
        mEraserColor = color
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mBufferBitmap != null) {
            canvas!!.drawBitmap(mBufferBitmap!!, 0f, 0f, null)
        }
    }

    /**
     *  不能直接调用view的TouchEvent,会导致手势识别不准
     */
    fun handelTouchEvent(event: MotionEvent?): Boolean {
        if (!isEnabled) {
            return false
        }
        val action = event!!.action and MotionEvent.ACTION_MASK
        val x = event.x
        val y = event.y

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mPreviousX = x
                mPreviousY = y
                mPath.moveTo(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                Log.i(TAG, "move")
                mPath.quadTo(mPreviousX, mPreviousY, (x + mPreviousX) / 2, (y + mPreviousY) / 2)
                if (mBufferBitmap == null) {
                    initBuffer()
                }
                mBufferCanvas?.drawPath(mPath, mPaint)
                calculationPath2Point(x, y)
                invalidate()
                mPreviousX = x
                mPreviousY = y
            }

            MotionEvent.ACTION_UP -> {
                Log.i(TAG, "up")
            }
        }
        return true
    }

    /**
     *  设置画笔起始点
     */
    fun setPaintStartPoint(pointF: PointF) {
        if (mPreviousX == 0f && mPreviousY == 0f) {
            mPreviousX = pointF.x
            mPreviousY = pointF.y
            mPath.moveTo(mPreviousX, mPreviousY)
        }
    }

    /**
     *  设置点云相机，投影逆矩阵
     */
    fun setInvertMatrix(invertVPMatrix: FloatArray, invertModelMatrix: FloatArray) {
        if (mInvertVpMatrix == null) {
            mInvertVpMatrix = invertVPMatrix
        }
        mInvertModelMatrix = invertModelMatrix
    }

    fun finishSmear() {
        calculationPointInterSection(selectedPointArray, removePointArray)
        resetPalette()
    }

    /**
     *  计算绘制轨迹对应的点集
     */
    private fun calculationPath2Point(x: Float, y: Float) {
        //获取归一坐标
        val normalizedStartX: Float = PointsUtil.getNormalizedX(mPreviousX - 10f, width!!.toFloat())
        val normalizedStartY: Float = PointsUtil.getNormalizedY(mPreviousY - 10f, height!!.toFloat())

        val normalizedEndX: Float = PointsUtil.getNormalizedX(x + 10f, width!!.toFloat())
        val normalizedEndY: Float = PointsUtil.getNormalizedY(y + 10f, height!!.toFloat())

        //获取世界坐标
        mStartWorldPoint = PointsUtil.getWorldPointWithIMM(normalizedStartX, normalizedStartY, mInvertVpMatrix!!, mInvertModelMatrix!!, true)
        mEndWorldPoint = PointsUtil.getWorldPointWithIMM(normalizedEndX, normalizedEndY, mInvertVpMatrix!!, mInvertModelMatrix!!, true)

        Log.i("DRAW===", "sWorldPoint = ${mStartWorldPoint.x} ${mStartWorldPoint.y} ${mStartWorldPoint.z}, " +
                "eWorldPoint= ${mEndWorldPoint.x} ${mEndWorldPoint.y} ${mEndWorldPoint.z}" )


        val cubeArea = getCubeArea(mStartWorldPoint, mEndWorldPoint)

        when (mMode) {
            PaletteMode.DRAW -> {
                scope.launch(Dispatchers.Main) {
                    withContext(Dispatchers.IO) {
                        selectedPointArray += PointsUtil.getCubeCloudPoint(PointData.allCloudPoint, cubeArea)

                        PointData.selectedSmearPoint = selectedPointArray
                        Log.i(TAG, "calculation end")
                    }
                    mCallBack.updatePointGLView()
                    mCanEraser = selectedPointArray.isNotEmpty()
                }
            }

            PaletteMode.ERASER -> {
                if (!mCanEraser) {
                    return
                }
                scope.launch(Dispatchers.IO) {
                    removePointArray += PointsUtil.getCubeCloudPoint(PointData.allCloudPoint, cubeArea)
                }
            }
        }
    }

    /**
     *  计算涂抹点集和擦除点集的差集
     *  方法耗时, 仅手指抬起时计算
     */
    private fun calculationPointInterSection(selectedArray: FloatArray, removeArray: FloatArray) {
        scope.launch(Dispatchers.IO) {
            val selectedList = async {
                PointsUtil.transformArrayToList(selectedArray)
            }
            val removeList = async {
                PointsUtil.transformArrayToList(removeArray)
            }
            val subtractList = selectedList.await() subtract removeList.await()

            if (subtractList.isNotEmpty()) {
                val subtractArray = PointsUtil.transformSetToArray(subtractList)
                selectedPointArray = subtractArray
                PointData.selectedSmearPoint = selectedPointArray

                withContext(Dispatchers.Main) {
                    mCallBack.updatePointGLView()
                }
            }
        }
    }

    /**
     *  获取框选立方体
     */
    private fun getCubeArea(startPoint: Point3f, endPoint: Point3f): CubeArea {
        val minX = minOf(startPoint.x, endPoint.x)
        val maxX = maxOf(startPoint.x, endPoint.x)
        val minY = minOf(startPoint.y, endPoint.y)
        val maxY = maxOf(startPoint.y, endPoint.y)
        val minZ = minOf(MOCK_Z_POINT_FRONT, MOCK_Z_POINT_BACK)
        val maxZ = maxOf(MOCK_Z_POINT_FRONT, MOCK_Z_POINT_BACK)
        return CubeArea(minX, maxX, minY, maxY, minZ, maxZ)
    }

    /**
     *  手指抬起重置画板
     */
    private fun resetPalette() {
        mPath.reset()
        mPreviousX = 0f
        mPreviousY = 0f
        removePointArray = floatArrayOf()
        mBufferBitmap.let {
            mBufferBitmap!!.eraseColor(Color.TRANSPARENT)
            invalidate()
        }
    }

    /**
     *  设置回调
     */
    fun setCallBack(callBack: Callback) {
        mCallBack = callBack
    }

    interface Callback {
        fun updatePointGLView()
    }

}