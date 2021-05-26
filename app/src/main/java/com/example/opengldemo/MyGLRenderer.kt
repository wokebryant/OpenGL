package com.example.opengldemo

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.constraintlayout.solver.widgets.Analyzer
import com.example.opengldemo.shape.*
import com.example.opengldemo.utlis.TextureUtil
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * OpenGL渲染类
 */
class MyGLRenderer : GLSurfaceView.Renderer {

    private lateinit var mTriangle: Triangle
    private lateinit var mSquare: Square
    private lateinit var mCircle: Circle
    private lateinit var mCone: Cone
    private lateinit var mCylinder: Cylinder
    private lateinit var mBall: Ball

    //定义各种矩阵
    private val vPMatrix = FloatArray(16)
    private val projectMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val rotationMatrix = FloatArray(16)

    @Volatile
    var angle: Float = 0f
    @Volatile
    var scale: Float = 1.0f

    @Volatile
    lateinit var context: Context

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //设置背景参数，参数为RGB色值以及透明度
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mTriangle = Triangle()
        mSquare = Square(context)
        mCircle = Circle()
        mCone = Cone()
        mCylinder = Cylinder()
        mBall = Ball(context)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        //设置相机矩阵
        //eye代表相机的坐标
        //center 代表观测物体的中心坐标
        //up代表相机观测方向
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, -5f,
            0f, 0f, 0f,
            0f, 1f, 0f)
        Matrix.multiplyMM(vPMatrix, 0, projectMatrix, 0, viewMatrix, 0)

        val scratch = FloatArray(16)
        //设置旋转矩阵
        //x,y,z代表旋转轴
        //a:旋转角度，正数，逆时针。负数：顺时针
        Matrix.setRotateM(rotationMatrix, 0, angle, 0f, 1f, 0f)
        Matrix.scaleM(rotationMatrix, 0, scale, scale, scale)

        Matrix.multiplyMM(scratch, 0, vPMatrix, 0, rotationMatrix, 0)

        //设置缩放矩阵
//        Matrix.multiplyMM(total, 0, scratch, 0, scaleMatrix, 0)
//        mSquare.draw(scratch)
//        mTriangle.draw(scratch)
//        mCircle.draw(scratch)
//        mCone.draw(scratch)
//        mCylinder.draw(scratch)
        mBall.draw(scratch)
    }

    /**
     * 在屏幕方向发生变化时可重新设置观察物体的形变比例
     */
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //设置Surface显示区域，坐标原点和右上角坐标
        GLES20.glViewport(0, 0, width, height)

        val ratio: Float = width.toFloat() / height.toFloat()
        //设置被观测物体投影矩阵
        //left,right决定物体上下形变
        //bottom, top决定物体左右形变
        //near，近观测面,距离为相机距离近视面的距离
        //far，远观测面，距离为相机距离远视面的距离
        // near<= 相机距离 <= far
        Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1f, 1f, 2f,17f)
    }

    /**
     * http://wiki.jikexueyuan.com/project/opengl-es-basics/coordinate-transformation.html
     *  Translate & Rotate （平移和旋转组合变换）,两者组合变换等顺序会影响最终结果，因为旋转后坐标系变了
     *  Translate & Scale（平移和缩放组合变换），两者组合变换等顺序会影响最终结果，因为缩放后的值为当前坐标值*缩放比例
     *
     */

}