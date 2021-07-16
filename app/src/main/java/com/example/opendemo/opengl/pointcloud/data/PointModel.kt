package com.example.opendemo.opengl.pointcloud.data

import kotlin.math.sqrt

/**
 *  立体框选
 */
enum class Control {
    NONE,
    LEFT_LEFT, LEFT_RIGHT, LEFT_TOP, LEFT_BOTTOM,
    TOP_LEFT, TOP_RIGHT, TOP_TOP, TOP_BOTTOM,
    MAIN_LEFT, MAIN_RIGHT, MAIN_TOP, MAIN_BOTTOM
}

/**
 *  画板模式
 */
enum class PaletteMode {
    DRAW,
    ERASER
}

/**
 *  点坐标
 */
data class Point3f(
    var x: Float,
    var y: Float,
    var z: Float
) {
    fun translateY(distance: Float): Point3f {
        return Point3f(x, y + distance, z)
    }

    fun translate(vector: Vector): Point3f {
        return Point3f(
            x + vector.x,
            y + vector.y,
            z + vector.z
        )
    }
}

/**
 *  向量
 */
data class Vector(
    var x: Float,
    var y: Float,
    var z: Float
) {
    fun length(): Float {
        return sqrt(
            (x * x + y * y + z * z).toDouble()
        ).toFloat()
    }

    fun crossProduct(other: Vector): Vector {
        return Vector(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    fun dotProduct(other: Vector): Float {
        return x * other.x + y * other.y + z * other.z
    }

    fun scale(f: Float): Vector {
        return Vector(
            x * f,
            y * f,
            z * f
        )
    }

    fun normalize(): Vector {
        return scale(1f / length())
    }
}

/**
 *  射线
 */
data class Ray(
    var point: Point3f,
    var vector: Vector
)

/**
 *  圆
 */
data class Circle(
    var center: Point3f,
    var radius: Float
) {
    fun scale(scale: Float): Circle {
        return Circle(center, radius * scale)
    }
}

/**
 *  圆柱
 */
data class Cylinder(
    var center: Point3f,
    var radius: Float,
    var height: Float
)

/**
 *  球体
 */
data class Sphere(
    var center: Point3f,
    var radius: Float
)

/**
 *  平面
 */
data class Plane(var point: Point3f,
                 var normal: Vector
)

/**
 *  光源里点3个同心圆
 */
data class ConcentricCircles(var circleInner: ArrayList<Point3f>,
                             var circleMiddle: ArrayList<Point3f>,
                             var circleOut: ArrayList<Point3f>
)

/**
 *  光源里点3个同心环List
 */
data class ConcentricRingList(var ringInner: ArrayList<Point3f>,
                              var ringMiddle: ArrayList<Point3f>,
                              var ringOut: ArrayList<Point3f>
)

/**
 *  光源里点3个同心环数组
 */
data class ConcentricRingArray(var ringInner: FloatArray,
                               var ringMiddle: FloatArray,
                               var ringOut: FloatArray)


data class TouchArea(
    var xMin: Float,
    var xMax: Float,
    var yMin: Float,
    var yMax: Float
)

data class CubeArea(
    var xMin: Float,
    var xMax: Float,
    var yMin: Float,
    var yMax: Float,
    var zMin: Float,
    var zMax: Float
)

data class CenterPoint(
    var x: Float,
    var y: Float
)

fun TouchArea.contains(x: Float, y: Float) = x in xMin..xMax && y in yMin..yMax

fun CubeArea.contains(x: Float, y: Float, z: Float) = x in xMin..xMax && y in yMin..yMax && z in zMin..zMax
