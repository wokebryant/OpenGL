package com.example.opendemo.opengl.pointcloud.renderer

import android.opengl.GLES20
import android.util.Log
import com.example.opendemo.opengl.pointcloud.data.ConcentricRingArray
import com.example.opendemo.opengl.pointcloud.data.PointData
import com.example.opendemo.opengl.pointcloud.data.Repository
import com.example.opendemo.opengl.pointcloud.utils.ShaderUtil
import com.example.opendemo.opengl.pointcloud.utils.MatrixTools
import com.example.opendemo.utlis.BufferUtil
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

class PointCloudGLRenderer : BaseGLRenderer() {

    //顶点坐标
    private var mVertexPoints: FloatArray? = null
    private lateinit var mLightSourcePoints: ConcentricRingArray
    private var mSmearPoints: FloatArray = floatArrayOf()
    private var mCorrectPartPoints: FloatArray = floatArrayOf()
    private var mCorrectAnswerPoints: FloatArray = floatArrayOf()
    //顶点Buffer缓存
    private var mVertexBuffer: FloatBuffer? = null
    private lateinit var mLightInnerBuffer: FloatBuffer
    private lateinit var mLightMiddleBuffer: FloatBuffer
    private lateinit var mLightOutBuffer: FloatBuffer
    private lateinit var mSmearBuffer: FloatBuffer
    private lateinit var mCorrectPartBuffer: FloatBuffer
    private lateinit var mCorrectAnswerBuffer: FloatBuffer

    private val matrixTools = MatrixTools()

    //光源点放大系数
    private val enlargeFactor = 1.5f
    //是否在涂抹
    private var isInSmear = false
    //是否展示涂抹前区域里的正确部分
    private var isShowCorrectPart = false
    //是否展示正确答案
    private var isShowCorrectAnswer = false

    companion object {
        private const val TAG = "PointCloudGLRenderer"
    }

    /**
     *  获取顶点坐标并存入Buffer
     */
    override fun initPoints() {
        val start = System.currentTimeMillis()

        mLightSourcePoints = Repository.getMockLightSourcePoint()
        Log.i("GET_ALL_POINT_COST ", "${System.currentTimeMillis() - start}")

        mLightInnerBuffer = BufferUtil.getFloatBuffer(mLightSourcePoints.ringInner)
        mLightMiddleBuffer = BufferUtil.getFloatBuffer(mLightSourcePoints.ringMiddle)
        mLightOutBuffer = BufferUtil.getFloatBuffer(mLightSourcePoints.ringOut)
    }

    fun updateVertexPoint(points: FloatArray) {
        mVertexPoints = points
        mVertexBuffer = BufferUtil.getFloatBuffer(points)
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
//            //左视图：ex=-6, uy=1
//            //主视图：ez=6, uy=1
//            //俯视图：ey=6, uz=-1
//            setCamera(
//                0f, 0f, 6f,
//                0f, 0f, 0f,
//                0f, 1f, 0f
//            )

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
        matrixTools.apply {
            setIdentity()
            rotate(angleY, -1f, 0f, 0f)
            rotate(angleX, 0f, 1f, 0f)
            scale(scale, scale, scale)
            translate(transX, transY, transZ)

            //绘制点云
            if (mVertexPoints != null && mVertexBuffer != null) {
                draw(matrixTools.finalMatrix,
                    mVertexBuffer!!,
                    whiteColor,
                    mVertexPoints!!.size / POINT_PER_VERTEX)
            }

            //绘制内部光源
            draw(matrixTools.finalMatrix,
                mLightInnerBuffer,
                lightColorInner,
                mLightSourcePoints.ringInner.size / POINT_PER_VERTEX)

            //绘制中部光源
            draw(matrixTools.finalMatrix,
                mLightMiddleBuffer,
                lightColorMiddle,
                mLightSourcePoints.ringMiddle.size / POINT_PER_VERTEX)

            //绘制外部光源
            draw(matrixTools.finalMatrix,
                mLightOutBuffer,
                lightColorOut,
                mLightSourcePoints.ringOut.size / POINT_PER_VERTEX)

            //绘制涂抹区域
            if (isInSmear) {
                draw(matrixTools.finalMatrix,
                    mSmearBuffer,
                    yellowColor,
                    mSmearPoints.size / POINT_PER_VERTEX)
            }

            //绘制涂抹区域中的正确部分
            if (isShowCorrectPart) {
                draw(matrixTools.finalMatrix,
                    mCorrectPartBuffer,
                    purpleColor,
                    mCorrectPartPoints.size / POINT_PER_VERTEX)
            }

            //绘制正确答案区域
            if (isShowCorrectAnswer) {
                draw(matrixTools.finalMatrix,
                    mCorrectAnswerBuffer,
                    greenColor,
                    mCorrectAnswerPoints.size / POINT_PER_VERTEX)
            }
        }
    }

    private fun draw(matrix: FloatArray, buffer: FloatBuffer, color: FloatArray, count: Int) {
        GLES20.glEnableVertexAttribArray(mVertexHandle)

        //绘制pcd坐标
        GLES20.glVertexAttribPointer(
            mVertexHandle,
            POINT_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            POINT_PER_VERTEX * 4,
            buffer
        )
        GLES20.glUniform4fv(mColorHandle, 1, color, 0)
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, matrix, 0)

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, count)

        GLES20.glDisableVertexAttribArray(mVertexHandle)
    }

    /**
     *  处理涂抹的点集
     */
    fun handelSmearPoint() {
        mSmearPoints = PointData.selectedSmearPoint
        mSmearBuffer = BufferUtil.getFloatBuffer(mSmearPoints)
        isInSmear = true
        Log.i(TAG, "save end")
    }

    /**
     *  处理涂抹区域中正确的部分
     */
    fun handelCorrectPartPoint() {
        mCorrectPartPoints = PointData.correctPartPoint
        mCorrectPartBuffer = BufferUtil.getFloatBuffer(mCorrectPartPoints)
        isShowCorrectPart = true
    }

    /**
     *  处理正确答案点集
     */
    fun handelCorrectAnswerPoint() {
        mCorrectAnswerPoints = PointData.correctAnswerPoint
        mCorrectAnswerBuffer = BufferUtil.getFloatBuffer(mCorrectAnswerPoints)
        isShowCorrectAnswer = true
    }

    /**
     *  清除涂抹的轨迹点集
     */
    fun clearSmearPoint() {
        PointData.selectedSmearPoint = floatArrayOf()
        handelSmearPoint()
    }

    override fun handleRotate(normalizedX: Float, normalizedY: Float, isDownPress: Boolean) {
        setInvertVPMatrix(matrixTools.invertVpMatrix)
        setModelMatrix(matrixTools.modelMatrix)
        super.handleRotate(normalizedX, normalizedY, isDownPress)
    }

    fun getInvertVPMatrix(): FloatArray = matrixTools.invertVpMatrix

    fun getModelMatrix(): FloatArray = matrixTools.modelMatrix

    fun getInvertModelMatrix(): FloatArray = matrixTools.invertModelMatrix

}