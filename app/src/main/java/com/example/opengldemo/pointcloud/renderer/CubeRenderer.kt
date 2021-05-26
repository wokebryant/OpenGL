package com.example.opengldemo.pointcloud.renderer

import android.opengl.GLES20
import com.example.opengldemo.pointcloud.*
import com.example.opengldemo.pointcloud.data.*
import com.example.opengldemo.pointcloud.utils.MatrixTools
import com.example.opengldemo.pointcloud.utils.PointsUtil
import com.example.opengldemo.pointcloud.utils.ShaderUtil
import com.example.opengldemo.utlis.BufferUtil
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs

/**
 *  立体矩形框选
 */
class CubeRenderer : BaseGLRenderer(){

    companion object {
        private const val TAG = "CubeRenderer"
        const val VERTEX_COUNT_PER_FACE = 4
        const val MOCK_Z_POINT_FRONT = 0.1f
        const val MOCK_Z_POINT_BACK = -0.1f
    }

    //立方体长，宽，纵深
    private var width: Float = 0f
    private var height: Float = 0f
    private var depth: Float = 0f

    //Touch_Down事件坐标
    private var mPreviousX: Float = 0f
    private var mPreviousY: Float = 0f

    //三视图触碰区域安全区
    var outPadding = 0.05f
    var verticalPadding = 0.05f
    var horizontalPadding = 0.05f

    //主视图触碰区域
    lateinit var mainLeftTouchArea: TouchArea
    lateinit var mainRightTouchArea: TouchArea
    lateinit var mainTopTouchArea: TouchArea
    lateinit var mainBottomTouchArea: TouchArea

    //左视图触碰区域
    lateinit var leftLeftTouchArea: TouchArea
    lateinit var leftRightTouchArea: TouchArea
    lateinit var leftTopTouchArea: TouchArea
    lateinit var leftBottomTouchArea: TouchArea

    //俯视图触碰区域
    lateinit var topLeftTouchArea: TouchArea
    lateinit var topRightTouchArea: TouchArea
    lateinit var topTopTouchArea: TouchArea
    lateinit var topBottomTouchArea: TouchArea

    //生成的立方体坐标区域
    private lateinit var cubeArea: CubeArea

    //主视图4个点坐标
    var mainLeftTop = FloatArray(4)
    var mainLeftBottom = FloatArray(4)
    var mainRightTop = FloatArray(4)
    var mainRightBottom = FloatArray(4)

    //左视图的4个点坐标
    var leftLeftTop = FloatArray(4)
    var leftLeftBottom = FloatArray(4)
    var leftRightTop = FloatArray(4)
    var leftRightBottom = FloatArray(4)

    //俯视图的4个点坐标
    var topLeftTop = FloatArray(4)
    var topLeftBottom = FloatArray(4)
    var topRightTop = FloatArray(4)
    var topRightBottom = FloatArray(4)

    //立方体8个点坐标
    private lateinit var frontLeftTop: FloatArray
    private lateinit var frontLeftBottom: FloatArray
    private lateinit var frontRightBottom: FloatArray
    private lateinit var frontRightTop: FloatArray

    private lateinit var backLeftTop: FloatArray
    private lateinit var backLeftBottom: FloatArray
    private lateinit var backRightBottom: FloatArray
    private lateinit var backRightTop: FloatArray

    //立方体6个面顶点以及点云坐标
    private lateinit var mVertexPoints: FloatArray
    private lateinit var mFrontFacePoints: FloatArray
    private lateinit var mBackFacePoints: FloatArray
    private lateinit var mLeftFacePoints: FloatArray
    private lateinit var mRightFacePoints: FloatArray
    private lateinit var mTopFacePoints: FloatArray
    private lateinit var mBottomFacePoints: FloatArray

    //顶点Buffer缓存
    private lateinit var mVertexBuffer: FloatBuffer
    private lateinit var mFrontFaceBuffer: FloatBuffer
    private lateinit var mBackFaceBuffer: FloatBuffer
    private lateinit var mLeftFaceBuffer: FloatBuffer
    private lateinit var mRightFaceBuffer: FloatBuffer
    private lateinit var mTopFaceBuffer: FloatBuffer
    private lateinit var mBottomFaceBuffer: FloatBuffer

    private val matrixTools = MatrixTools()
    private var mInvertMainModelMatrix = FloatArray(16)
    private var mInvertLeftModelMatrix =  FloatArray(16)
    private var mInvertTopModelMatrix =  FloatArray(16)

    /**
     *  设置立方体绘制面起点和结束点坐标
     */
    fun setDrawFacePoint(startPoint: Point3f, endPoint: Point3f) {
        val centerX = (endPoint.x + startPoint.x) / 2
        val centerY = (startPoint.y + endPoint.y) / 2

        transX = -centerX
        transY = -centerY

        width = abs(endPoint.x - startPoint.x)
        height = abs(endPoint.y - startPoint.y)
        depth = abs(MOCK_Z_POINT_BACK - MOCK_Z_POINT_FRONT)

        frontLeftTop = floatArrayOf(startPoint.x, startPoint.y, MOCK_Z_POINT_FRONT, 1f)
        frontLeftBottom = floatArrayOf(startPoint.x, startPoint.y - height, MOCK_Z_POINT_FRONT, 1f)
        frontRightBottom = floatArrayOf(endPoint.x, endPoint.y, MOCK_Z_POINT_FRONT, 1f)
        frontRightTop = floatArrayOf(endPoint.x, endPoint.y + height, MOCK_Z_POINT_FRONT, 1f)

        backLeftTop = floatArrayOf(startPoint.x, startPoint.y, MOCK_Z_POINT_BACK, 1f)
        backLeftBottom = floatArrayOf(startPoint.x, startPoint.y - height, MOCK_Z_POINT_BACK, 1f)
        backRightBottom = floatArrayOf(endPoint.x, endPoint.y, MOCK_Z_POINT_BACK, 1f)
        backRightTop = floatArrayOf(endPoint.x, endPoint.y + height, MOCK_Z_POINT_BACK, 1f)

        val minX = minOf(startPoint.x, endPoint.x)
        val maxX = maxOf(startPoint.x, endPoint.x)
        val minY = minOf(startPoint.y, endPoint.y)
        val maxY = maxOf(startPoint.y, endPoint.y)
        val minZ = minOf(MOCK_Z_POINT_FRONT, MOCK_Z_POINT_BACK)
        val maxZ = maxOf(MOCK_Z_POINT_FRONT, MOCK_Z_POINT_BACK)
        cubeArea = CubeArea(minX, maxX, minY, maxY, minZ, maxZ)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        initPoints()
        initProgram()
        initHandle()
    }


    /**
     *  获取顶点坐标并存入Buffer
     */
    override fun initPoints() {
        mVertexPoints = PointsUtil.getCubeCloudPoint(PointData.allCloudPoint, cubeArea).toFloatArray()

        mFrontFacePoints = PointsUtil.getCubeFacePoint(
            arrayListOf(frontLeftTop, frontLeftBottom, frontRightBottom, frontRightTop))
        mBackFacePoints = PointsUtil.getCubeFacePoint(
            arrayListOf(backLeftTop, backLeftBottom, backRightBottom, backRightTop))
        mLeftFacePoints = PointsUtil.getCubeFacePoint(
            arrayListOf(backLeftTop, backLeftBottom, frontLeftBottom, frontLeftTop))
        mRightFacePoints = PointsUtil.getCubeFacePoint(
            arrayListOf(backRightTop, frontRightTop, frontRightBottom, backRightBottom))
        mTopFacePoints = PointsUtil.getCubeFacePoint(
            arrayListOf(backLeftTop, frontLeftTop, frontRightTop, backRightTop))
        mBottomFacePoints = PointsUtil.getCubeFacePoint(
            arrayListOf(backLeftBottom, frontLeftBottom, frontRightBottom, backRightBottom))

        mVertexBuffer = BufferUtil.getFloatBuffer(mVertexPoints)
        mFrontFaceBuffer = BufferUtil.getFloatBuffer(mFrontFacePoints)
        mBackFaceBuffer = BufferUtil.getFloatBuffer(mBackFacePoints)
        mLeftFaceBuffer  = BufferUtil.getFloatBuffer(mLeftFacePoints)
        mRightFaceBuffer  = BufferUtil.getFloatBuffer(mRightFacePoints)
        mTopFaceBuffer = BufferUtil.getFloatBuffer(mTopFacePoints)
        mBottomFaceBuffer = BufferUtil.getFloatBuffer(mBottomFacePoints)
    }


    /**
     *  获取着色器
     */
    override fun initProgram() {
        mProgram = ShaderUtil.createProgram(context.resources,
            "shader/vertex_shader.glsl", "shader/fragment_shader.glsl")
        GLES20.glUseProgram(mProgram)
    }

    /**
     *  获取要处理参数的索引
     */
    override fun initHandle() {
        mVertexHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "vMatrix")
    }

    /**
     *  视图变换时调用，重新设置投影矩阵和相机矩阵
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        val ratio: Float = width.toFloat() / height.toFloat()

        matrixTools.apply {
//            frustum(-ratio, ratio, -1f, 1f, 5f,7f)
            //左视图相当于主视图绕Y轴逆时针旋转90度
            //俯视图相当于主视图绕X轴旋转顺时针旋转90度
            //左视图：ex=-10, uy=1
            //主视图：ez=10, uy=1
            //俯视图：ey=10, uz=-1
            setOrtho(-ratio,ratio,-1f,1f,3f,20f);
            setCamera(
                0f, 0f, 10f,
                0f, 0f, 0f,
                0f, 1f, 0f
            )
        }
    }

    /**
     *  绘制
     */
    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        drawCube()
        changeCubeToLeftView()
        changeCubeToTopView()
    }

    /**
     *  生成立方体，主视图
     */
    private fun drawCube() {
        matrixTools.apply {
            pushModelMatrix()

            rotate(angleY, -1f, 0f, 0f)
            rotate(angleX, 0f, 1f, 0f)
            scale(scale, scale, scale)
            translate(transX, transY, transZ)
            drawCube(finalMatrix)
            updateMainPoint()
            calculationMainArea()

            popModelMatrix()
        }
    }

    /**
     *  生成左视图
     */
    private fun changeCubeToLeftView() {
        matrixTools.apply {
            pushModelMatrix()

            translate(0f, 0.7f, 0f)
            rotate(90f, 0f, 1f, 0f)
            translate(transX, transY, transZ)
            drawCube(finalMatrix)
            updateLeftPoint()
            calculationLeftArea()

            popModelMatrix()
        }
    }

    /**
     *  生成俯视图
     */
    private fun changeCubeToTopView() {
        matrixTools.apply {
            pushModelMatrix()

            translate(0f, -0.7f, 0f)
            rotate(90f, 1f, 0f, 0f)
            translate(transX, transY, transZ)
            drawCube(finalMatrix)
            updateTopPoint()
            calculationTopArea()

            popModelMatrix()
        }
    }

    /**
     *  生成主视图更新坐标
     */
    private fun updateMainPoint() {
        matrixTools.apply {
            multiplyMV(mainLeftTop, modelMatrix, frontLeftTop)
            multiplyMV(mainLeftBottom, modelMatrix, frontLeftBottom)
            multiplyMV(mainRightTop, modelMatrix, frontRightTop)
            multiplyMV(mainRightBottom, modelMatrix, frontRightBottom)
            invert(mInvertMainModelMatrix, modelMatrix)
        }
    }

    /**
     *  生成左视图更新坐标
     */
    private fun updateLeftPoint() {
        matrixTools.apply {
            multiplyMV(leftLeftTop, modelMatrix, backLeftTop)
            multiplyMV(leftLeftBottom, modelMatrix, backLeftBottom)
            multiplyMV(leftRightTop, modelMatrix, frontLeftTop)
            multiplyMV(leftRightBottom, modelMatrix, frontLeftBottom)
            invert(mInvertLeftModelMatrix, modelMatrix)
        }
    }

    /**
     *  生成视俯图更新坐标
     */
    private fun updateTopPoint() {
        matrixTools.apply {
            multiplyMV(topLeftTop, modelMatrix, backLeftTop)
            multiplyMV(topLeftBottom, modelMatrix, frontLeftTop)
            multiplyMV(topRightTop, modelMatrix, backRightTop)
            multiplyMV(topRightBottom, modelMatrix, frontRightTop)
            invert(mInvertTopModelMatrix, modelMatrix)
        }
    }

    /**
     *  主视图4条变边可触碰区域
     */
    private fun calculationMainArea() {
        val xLT = mainLeftTop[0]
        val yLT = mainLeftTop[1]
        val xLB = mainLeftBottom[0]
        val yLB = mainLeftBottom[1]
        val xRT = mainRightTop[0]
        val yRT = mainRightTop[1]
        val xRB = mainRightBottom[0]
        val yRB = mainRightBottom[1]

        mainLeftTouchArea = TouchArea(xLT - outPadding, xLT + horizontalPadding,
            yLB + verticalPadding, yLT - verticalPadding)

        mainRightTouchArea = TouchArea(xRT - horizontalPadding, xRT + outPadding,
            yRB + verticalPadding, yRT - verticalPadding)

        mainTopTouchArea = TouchArea(xLT + horizontalPadding, xRT - horizontalPadding,
            yLT - verticalPadding, yLT + outPadding)

        mainBottomTouchArea = TouchArea(xLB + horizontalPadding, xRB - horizontalPadding,
            yLB - outPadding, yLB + verticalPadding)
    }

    /**
     *  左视图4条边可触碰区域
     */
    private fun calculationLeftArea() {
        val xLT = leftLeftTop[0]
        val yLT = leftLeftTop[1]
        val xLB = leftLeftBottom[0]
        val yLB = leftLeftBottom[1]
        val xRT = leftRightTop[0]
        val yRT = leftRightTop[1]
        val xRB = leftRightBottom[0]
        val yRB = leftRightBottom[1]

        leftLeftTouchArea = TouchArea(xLT - outPadding, xLT + horizontalPadding,
            yLB + verticalPadding, yLT - verticalPadding)

        leftRightTouchArea = TouchArea(xRT - horizontalPadding, xRT + outPadding,
            yRB + verticalPadding, yRT - verticalPadding)
        leftTopTouchArea = TouchArea(xLT + horizontalPadding, xRT - horizontalPadding,
            yLT - verticalPadding, yLT + outPadding)

        leftBottomTouchArea = TouchArea(xLB + horizontalPadding, xRB - horizontalPadding,
            yLB - outPadding, yLB + verticalPadding)
    }

    /**
     *  俯视图4条边可触碰区域
     */
    private fun calculationTopArea() {
        val xLT = topLeftTop[0]
        val yLT = topLeftTop[1]
        val xLB = topLeftBottom[0]
        val yLB = topLeftBottom[1]
        val xRT = topRightTop[0]
        val yRT = topRightTop[1]
        val xRB = topRightBottom[0]
        val yRB = topRightBottom[1]

        topLeftTouchArea = TouchArea(xLT - outPadding, xLT + horizontalPadding,
            yLB + verticalPadding, yLT - verticalPadding)

        topRightTouchArea = TouchArea(xRT - horizontalPadding, xRT + outPadding,
            yRB + verticalPadding, yRT - verticalPadding)
        topTopTouchArea = TouchArea(xLT + horizontalPadding, xRT - horizontalPadding,
            yLT - verticalPadding, yLT + outPadding)

        topBottomTouchArea = TouchArea(xLB + horizontalPadding, xRB - horizontalPadding,
            yLB - outPadding, yLB + verticalPadding)
    }

    private fun drawCube(matrix: FloatArray) {
//        Log.i(TAG, "onDraw")

        GLES20.glEnableVertexAttribArray(mVertexHandle)

        GLES20.glUniform4fv(mColorHandle, 1, whiteColor, 0)
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, matrix, 0)

        //绘制立方体内的3D点云顶点
        GLES20.glVertexAttribPointer(
            mVertexHandle,
            POINT_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            mVertexBuffer
        )
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, mVertexPoints.size / POINT_PER_VERTEX)

        GLES20.glLineWidth(3.0f)
        //绘制矩形前面
        GLES20.glVertexAttribPointer(
            mVertexHandle,
            POINT_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            mFrontFaceBuffer
        )
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, VERTEX_COUNT_PER_FACE)

        //绘制矩形背面
        GLES20.glVertexAttribPointer(
            mVertexHandle,
            POINT_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            mBackFaceBuffer
        )
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, VERTEX_COUNT_PER_FACE)

        //绘制矩形左面
        GLES20.glVertexAttribPointer(
            mVertexHandle,
            POINT_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            mLeftFaceBuffer
        )
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, VERTEX_COUNT_PER_FACE)

        //绘制矩形右边
        GLES20.glVertexAttribPointer(
            mVertexHandle,
            POINT_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            mRightFaceBuffer
        )
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, VERTEX_COUNT_PER_FACE)

        //绘制矩形顶部
        GLES20.glVertexAttribPointer(
            mVertexHandle,
            POINT_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            mTopFaceBuffer
        )
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, VERTEX_COUNT_PER_FACE)

        //绘制矩形底部
        GLES20.glVertexAttribPointer(
            mVertexHandle,
            POINT_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            mBottomFaceBuffer
        )
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, VERTEX_COUNT_PER_FACE)

        GLES20.glDisableVertexAttribArray(mVertexHandle)
    }

    override fun handleRotate(normalizedX: Float, normalizedY: Float, isDownPress: Boolean) {
        setInvertVPMatrix(getInvertVPMatrix())
        setModelMatrix(getModelMatrix())
        super.handleRotate(normalizedX, normalizedY, isDownPress)
    }

    fun getInvertVPMatrix(): FloatArray = matrixTools.invertVpMatrix

    fun getModelMatrix(): FloatArray = matrixTools.modelMatrix

    fun handelViewChange(controlType: Control, normalizedX: Float, normalizedY: Float, isDownPress: Boolean) {
        val worldPoint = PointsUtil.getWorldPoint(
            normalizedX,
            normalizedY,
            getInvertVPMatrix(),
            true
        )
        if (isDownPress) {
            mPreviousX = worldPoint.x
            mPreviousY = worldPoint.y
            return
        }

        val currentX = worldPoint.x
        val currentY = worldPoint.y

        var instanceX: Float = currentX - mPreviousX
        var instanceY: Float = currentY - mPreviousY

        mPreviousX = currentX
        mPreviousY = currentY

        when (controlType) {
            //主视图
            Control.MAIN_LEFT -> {
                mainLeftTop[0] += instanceX
                mainLeftBottom[0] += instanceX
                width += instanceX

                matrixTools.apply {
                    multiplyMV(frontLeftTop, mInvertMainModelMatrix, mainLeftTop)
                    multiplyMV(frontLeftBottom, mInvertMainModelMatrix, mainLeftBottom)

                    backLeftTop = frontLeftTop.clone()
                    backLeftTop[2] = frontLeftTop[2] - depth

                    backLeftBottom = frontLeftBottom.clone()
                    backLeftBottom[2] = frontLeftBottom[2] - depth
                }

            }
            Control.MAIN_RIGHT -> {
                mainRightTop[0] += instanceX
                mainRightBottom[0] += instanceX
                width += instanceX

                matrixTools.apply {
                    multiplyMV(frontRightTop, mInvertMainModelMatrix, mainRightTop)
                    multiplyMV(frontRightBottom, mInvertMainModelMatrix, mainRightBottom)

                    backRightTop = frontRightTop.clone()
                    backRightTop[2] = frontRightTop[2] - depth

                    backRightBottom = frontRightBottom.clone()
                    backRightBottom[2] = frontRightTop[2] - depth
                }
            }
            Control.MAIN_TOP -> {
                mainLeftTop[1] += instanceY
                mainRightTop[1] += instanceY
                height += instanceY

                matrixTools.apply {
                    multiplyMV(frontLeftTop, mInvertMainModelMatrix, mainLeftTop)
                    multiplyMV(frontRightTop, mInvertMainModelMatrix, mainRightTop)

                    backLeftTop = frontLeftTop.clone()
                    backLeftTop[2] = frontLeftTop[2] - depth

                    backRightTop = frontRightTop.clone()
                    backRightTop[2] = frontRightTop[2] - depth
                }
            }
            Control.MAIN_BOTTOM -> {
                mainLeftBottom[1] += instanceY
                mainRightBottom[1] += instanceY
                height += instanceY

                matrixTools.apply {
                    multiplyMV(frontLeftBottom, mInvertMainModelMatrix, mainLeftBottom)
                    multiplyMV(frontRightBottom, mInvertMainModelMatrix, mainRightBottom)

                    backLeftBottom = frontLeftBottom.clone()
                    backLeftBottom[2] = frontLeftBottom[2] - depth

                    backRightBottom = frontRightBottom.clone()
                    backRightBottom[2] = frontRightBottom[2] - depth
                }
            }

            //左视图
            Control.LEFT_LEFT -> {
                leftLeftTop[0] += instanceX
                leftLeftBottom[0] += instanceX
                depth += instanceX

                matrixTools.apply {
                    multiplyMV(backLeftTop, mInvertLeftModelMatrix, leftLeftTop)
                    multiplyMV(backLeftBottom, mInvertLeftModelMatrix, leftLeftBottom)

                    backRightTop = backLeftTop.clone()
                    backRightTop[0] = backLeftTop[0] + width

                    backRightBottom = backLeftBottom.clone()
                    backRightBottom[0] = backLeftBottom[0] + width
                }
            }
            Control.LEFT_RIGHT -> {
                leftRightTop[0] += instanceX
                leftRightBottom[0] += instanceX
                depth += instanceX

                matrixTools.apply {
                    multiplyMV(frontLeftTop, mInvertLeftModelMatrix, leftRightTop)
                    multiplyMV(frontLeftBottom, mInvertLeftModelMatrix, leftRightBottom)

                    frontRightTop = frontLeftTop.clone()
                    frontRightTop[0] = frontLeftTop[0] + width

                    frontRightBottom = frontLeftBottom.clone()
                    frontRightBottom[0] = frontLeftBottom[0] + width
                }
            }
            Control.LEFT_TOP -> {
                leftLeftTop[1] += instanceY
                leftRightTop[1] += instanceY
                height += instanceY

                matrixTools.apply {
                    multiplyMV(backLeftTop, mInvertLeftModelMatrix, leftLeftTop)
                    multiplyMV(frontLeftTop, mInvertLeftModelMatrix, leftRightTop)

                    backRightTop = backLeftTop.clone()
                    backRightTop[0] = backLeftTop[0] + width

                    frontRightTop = frontLeftTop.clone()
                    frontRightTop[0] = frontLeftTop[0] + width
                }
            }
            Control.LEFT_BOTTOM -> {
                leftLeftBottom[1] += instanceY
                leftRightBottom[1] += instanceY
                height += instanceY

                matrixTools.apply {
                    multiplyMV(backLeftBottom, mInvertLeftModelMatrix, leftLeftBottom)
                    multiplyMV(frontLeftBottom, mInvertLeftModelMatrix, leftRightBottom)

                    backRightBottom = backLeftBottom.clone()
                    backRightBottom[0] = backLeftBottom[0] + width

                    frontRightBottom = frontLeftBottom.clone()
                    frontRightBottom[0] = frontLeftBottom[0] + width
                }
            }

            //俯视图
            Control.TOP_LEFT -> {
                topLeftTop[0] += instanceX
                topLeftBottom[0] += instanceX
                width += instanceX

                matrixTools.apply {
                    multiplyMV(backLeftTop, mInvertTopModelMatrix, topLeftTop)
                    multiplyMV(frontLeftTop, mInvertTopModelMatrix, topLeftBottom)

                    backLeftBottom = backLeftTop.clone()
                    backLeftBottom[1] = backLeftTop[1] - height

                    frontLeftBottom = frontLeftTop.clone()
                    frontLeftBottom[1] = frontLeftTop[1] - height
                }
            }
            Control.TOP_RIGHT -> {
                topRightTop[0] += instanceX
                topRightBottom[0] += instanceX
                width += instanceX

                matrixTools.apply {
                    multiplyMV(backRightTop, mInvertTopModelMatrix, topRightTop)
                    multiplyMV(frontRightTop, mInvertTopModelMatrix, topRightBottom)

                    backRightBottom = backRightTop.clone()
                    backRightBottom[1] = backRightTop[1] - height

                    frontRightBottom = frontRightTop.clone()
                    frontRightBottom[1] = frontLeftTop[1] - height
                }
            }
            Control.TOP_TOP -> {
                topLeftTop[1] += instanceY
                topRightTop[1] += instanceY
                depth += instanceY

                matrixTools.apply {
                    multiplyMV(backLeftTop, mInvertTopModelMatrix, topLeftTop)
                    multiplyMV(backRightTop, mInvertTopModelMatrix, topRightTop)

                    backLeftBottom = backLeftTop.clone()
                    backLeftBottom[1] = backLeftTop[1] - height

                    backRightBottom = backRightTop.clone()
                    backRightBottom[1] = backRightTop[1] - height
                }
            }
            Control.TOP_BOTTOM -> {
                topLeftBottom[1] += instanceY
                topRightBottom[1] += instanceY
                depth += instanceY

                matrixTools.apply {
                    multiplyMV(frontLeftTop, mInvertTopModelMatrix, topLeftBottom)
                    multiplyMV(frontRightTop, mInvertTopModelMatrix, topRightBottom)

                    frontLeftBottom = frontLeftTop.clone()
                    frontLeftBottom[1] = frontLeftTop[1] - height

                    frontRightBottom = frontRightTop.clone()
                    frontRightBottom[1] = frontRightTop[1] - height
                }
            }
        }
        initPoints()
    }

}