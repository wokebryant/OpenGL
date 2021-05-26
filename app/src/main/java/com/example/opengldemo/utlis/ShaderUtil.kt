package com.example.opengldemo.utlis

import android.opengl.GLES20

/**
 * 着色器工具类
 */
object ShaderUtil {

    const val vertexShaderType = GLES20.GL_VERTEX_SHADER
    const val fragmentShaderType = GLES20.GL_FRAGMENT_SHADER

    /**
     *  attritude：一般用于各个顶点各不相同的量。如顶点位置、纹理坐标、法向量、颜色等等
     *  uniform：一般用于对于物体中所有顶点或者所有的片段都相同的量。比如光源位置、统一变换矩阵、颜色等。
     *  varying：表示易变量，一般用于顶点着色器传递到片段着色器的量。
     */

    /**
     * 顶点着色器代码
     */
    const val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "attribute vec2 aTextureCoord;" +
        "varying vec2 vTextureCoord;" +
/*        "attribute vec4 a_Color;" +     //a_Color:从外部传来的每个顶点的颜色值
        "varying vec4 v_Color;" +       //v_Color:将每个顶点的颜色值传给片段着色器*/
                "void main() {" +
//                "v_Color = a_Color;" +
                "vTextureCoord = aTextureCoord;" +
                "gl_Position = uMVPMatrix * vPosition;" +
                "}"


    /**
     * 片段着色器代码
     */
    const val fragmentShaderCode =
        "precision mediump float;" +
//                "uniform vec4 vColor;" +
        "uniform sampler2D vTexture;" +
        "varying vec2 vTextureCoord;" +
//        "varying vec4 v_Color;" +       // v_Color：从顶点着色器传递过来的颜色值
                "void main() {" +
//                "  gl_FragColor = vColor;" +
                "gl_FragColor = texture2D(vTexture, vTextureCoord);"  +
                "}"

    const val sampleVertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
//        "attribute vec4 a_Color;" +
        "varying vec4 v_Color;"  +
                "void main() {" +
                "  float color;"            +
                "  if(vPosition.z > 0.0) {" +
                "  color = vPosition.z;"    +
                "   } else {" +
                "   color = -vPosition.z;" +
                "   }"                  +
                "  v_Color = vec4(color,color,color,1.0);" +
                "  gl_Position = uMVPMatrix * vPosition;" +
                "}"

    const val sampleFragmentShaderCode =
        "precision mediump float;" +
//                "uniform vec4 vColor;" +
                "varying vec4 v_Color;" +
                "void main() {" +
                "  gl_FragColor = v_Color;" +
                "}"


    /**
     * 获取顶点着色器
     */
    fun loadVertexShader(): Int {
        return loadShader(vertexShaderType, vertexShaderCode)
    }

    fun loadSampleVertexShader(): Int {
        return loadShader(vertexShaderType, sampleVertexShaderCode)
    }

    /**
     * 获取片段着色器
     */
    fun loadFragmentShader(): Int {
        return loadShader(fragmentShaderType, fragmentShaderCode)
    }

    fun loadSampleFragmentShader(): Int {
        return loadShader(fragmentShaderType, sampleFragmentShaderCode)
    }



    /**
     * 获取着色器
     */
    fun loadShader(type: Int, shaderCode: String) =
        GLES20.glCreateShader(type).also {shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }



}