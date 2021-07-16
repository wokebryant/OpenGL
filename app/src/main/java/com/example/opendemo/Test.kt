package com.example.opendemo.opengl.pointcloud.utils

var a = floatArrayOf(1f,2f,3f,4f)
var b = floatArrayOf(5f,6f,7f,8f)
var c = floatArrayOf(9f,10f,11f,12f)
var d = floatArrayOf(13f,14f,15f,16f)




fun main() {
    var list = ArrayList<FloatArray>()
    var a = list.apply {
        add(a)
        add(b)
        add(c)
        add(d)
    }
    var floatList = getFacePoint(a)
    floatList.forEach {
        println(it)
    }
}

fun getFacePoint(arrayList: ArrayList<FloatArray>): FloatArray {
    val list = ArrayList<Float>()
    for (index in 0 until arrayList.size) {
        arrayList[index].forEach {
            list.add(it)
        }
    }
    var step = 0
    val array = FloatArray(12)
    for (index in 0 until list.size) {
        if (index % 4 != 3) {
            array[step++] = list[index]
        }
    }
    return array
}