package com.example.opendemo.opengl.pointcloud.utils

import android.opengl.Matrix
import java.util.*

/**
 * 矩阵变换
 */
class MatrixTools {

    private val mModelStack: Stack<FloatArray> = Stack() //变换矩阵堆栈
    private val mViewStack: Stack<FloatArray> = Stack() //相机矩阵堆栈

    private var mMatrixView = FloatArray(16) //相机矩阵
    private val mMatrixProjection = FloatArray(16) //投影矩阵

    var modelMatrix = floatArrayOf(
        1f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f,
        0f, 0f, 1f, 0f,
        0f, 0f, 0f, 1f)
        private set

    //保护模型现场
    fun pushModelMatrix() {
        mModelStack.push(modelMatrix.copyOf(16))
    }

    //恢复模型现场
    fun popModelMatrix() {
        modelMatrix = mModelStack.pop()
    }

    //保护视图现场
    fun pushViewMatrix() {
        mViewStack.push(mMatrixView.copyOf(16))
    }

    //恢复视图现场
    fun popViewMatrix() {
        mMatrixView = mViewStack.pop()
    }

    fun clearStack() {
        mModelStack.clear()
        mViewStack.clear()
    }

    //设置初始矩阵
    fun setIdentity() {
        Matrix.setIdentityM(modelMatrix, 0)
    }

    //平移变换
    fun translate(x: Float, y: Float, z: Float) {
        Matrix.translateM(modelMatrix, 0, x, y, z)
    }

    //旋转变换
    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        Matrix.rotateM(modelMatrix, 0, angle, x, y, z)
    }

    //缩放变换
    fun scale(x: Float, y: Float, z: Float) {
        Matrix.scaleM(modelMatrix, 0, x, y, z)
    }

    //设置相机
    fun setCamera(ex: Float, ey: Float, ez: Float,
                  cx: Float, cy: Float, cz: Float,
                  ux: Float, uy: Float, uz: Float) {
        Matrix.setLookAtM(mMatrixView, 0, ex, ey, ez, cx, cy, cz, ux, uy, uz)
    }

    //透视投影
    fun setFrustum(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        Matrix.frustumM(mMatrixProjection, 0, left, right, bottom, top, near, far)
    }

    //正交投影
    fun setOrtho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        Matrix.orthoM(mMatrixProjection, 0, left, right, bottom, top, near, far)
    }

    val finalMatrix: FloatArray
        get() {
            val ans = FloatArray(16)
            Matrix.multiplyMM(ans, 0, mMatrixProjection, 0, mMatrixView, 0)
            Matrix.multiplyMM(ans, 0, ans, 0, modelMatrix, 0)
            return ans
        }

    val invertVpMatrix: FloatArray
        get() {
            val ans = FloatArray(16)
            val vp = FloatArray(16)
            Matrix.multiplyMM(vp, 0, mMatrixProjection, 0, mMatrixView, 0)
            Matrix.invertM(ans, 0, vp, 0)
            return ans
        }

    val invertModelMatrix: FloatArray
        get() {
            val invertMatrix = FloatArray(16)
            Matrix.invertM(invertMatrix, 0, modelMatrix, 0)
            return invertMatrix
        }

    fun multiplyMV(targetFloat: FloatArray?, matrix: FloatArray?, needFloat: FloatArray?) {
        Matrix.multiplyMV(targetFloat, 0, matrix, 0, needFloat, 0)
    }

    fun invert(targetFloat: FloatArray?, needFloat: FloatArray?) {
        Matrix.invertM(targetFloat, 0, needFloat, 0)
    }

}