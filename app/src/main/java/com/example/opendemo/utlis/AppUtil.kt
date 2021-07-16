package com.example.opendemo.utlis

import android.content.Context
import android.content.Intent

/**
 *  app工具类
 */
object AppUtil {

    /**
     *  通过实例化目标Activity泛型，必须是内联函数
     */
    inline fun <reified T> startActivity(context: Context, block: Intent.() -> Unit) {
        val intent = Intent(context, T::class.java)
        intent.block()
        context.startActivity(intent)
    }
}