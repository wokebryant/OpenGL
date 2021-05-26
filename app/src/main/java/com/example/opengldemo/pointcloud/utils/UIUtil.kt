package com.example.opengldemo.pointcloud.utils

import android.R
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.example.opengldemo.pointcloud.utils.BaseUtil.applicationContext
import java.math.BigDecimal

object UIUtil {

    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        Log.i("scale= ", scale.toString())
        return (dpValue * scale + 0.5f).toInt()
    }

    fun dip2px(dpValue: Float): Int {
        val scale = applicationContext!!.resources.displayMetrics.density
        Log.i("scale= ", scale.toString())
        return (dpValue * scale + 0.5f).toInt()
    }

    fun convertBitmap2Drawable(bitmap: Bitmap?): Drawable {
        val res = applicationContext!!.resources
        return BitmapDrawable(res, bitmap)
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun getRootView(context: Activity): View {
        return (context.findViewById<View>(R.id.content) as ViewGroup).getChildAt(0)
    }

    fun getScreenWidth(context: Context): Int {
        val metric = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metric)
        return metric.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        val metric = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(metric)
        return metric.heightPixels
    }

    /**
     * 获取顶部status bar高度
     */
    fun getStatusBarHeight(context: Context): Int {
        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    //weex尺寸转换px
    fun vp2px(context: Context, vpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        val px = (vpValue * getScreenWidth(context) / 750).toInt()
        Log.i("px= ", px.toString() + " " + getScreenWidth(context).toString())
        return px
    }

    //weex尺寸转换dp
    fun vp2dp(context: Context, vpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (vpValue * getScreenWidth(context) * 750 / scale).toInt()
    }

    fun formatNum(num: String, kBool: Boolean?): String {
        var kBool = kBool
        val sb = StringBuffer()
        //if (!StringUtils.isNumeric(num)) {
        //    return "0";
        //}
        if (kBool == null) {
            kBool = false
        }
        val b0 = BigDecimal("1000")
        val b1 = BigDecimal("10000")
        val b2 = BigDecimal("100000000")
        val b3 = BigDecimal(num)
        var formatNumStr = ""
        var nuit = ""

        // 以千为单位处理
        if (kBool) {
            return if (b3.compareTo(b0) == 0 || b3.compareTo(b0) == 1) {
                "999+"
            } else num
        }

        // 以万为单位处理
        if (b3.compareTo(b1) == -1) {
            sb.append(b3.toString())
        } else if (b3.compareTo(b1) == 0 && b3.compareTo(b1) == 1
            || b3.compareTo(b2) == -1
        ) {
            formatNumStr = b3.divide(b1).toString()
            nuit = "万"
        } else if (b3.compareTo(b2) == 0 || b3.compareTo(b2) == 1) {
            formatNumStr = b3.divide(b2).toString()
            nuit = "亿"
        }
        if ("" != formatNumStr) {
            var i = formatNumStr.indexOf(".")
            if (i == -1) {
                sb.append(formatNumStr).append(".0").append(nuit)
            } else {
                i = i + 1
                val v = formatNumStr.substring(i, i + 1)
                if (v != "0") {
                    sb.append(formatNumStr.substring(0, i + 1)).append(nuit)
                } else {
                    sb.append(formatNumStr.substring(0, i + 1)).append(nuit)
                }
            }
        }
        return if (sb.length == 0) {
            "0"
        } else sb.toString()
    }

    fun zoomImage(context: Context, resId: Int, w: Int, h: Int): Drawable {
        val res = context.resources
        val oldBmp = BitmapFactory.decodeResource(res, resId)
        val newBmp = Bitmap.createScaledBitmap(oldBmp, w, h, true)
        return BitmapDrawable(res, newBmp)
    }

    fun getMemoryInfo(activityManager: ActivityManager?) {
        if (activityManager != null) {
            //最大分配内存
            val memory = activityManager.memoryClass
            //最大分配内存获取方法2
            val maxMemory = (Runtime.getRuntime().maxMemory() * 1.0 / (1024 * 1024)).toFloat()
            //当前分配的总内存
            val totalMemory = (Runtime.getRuntime().totalMemory() * 1.0 / (1024 * 1024)).toFloat()
            //剩余内存
            val freeMemory = (Runtime.getRuntime().freeMemory() * 1.0 / (1024 * 1024)).toFloat()
            Log.i(
                "内存信息 ",
                "memory: " + maxMemory + "m" + " realMemory: " + totalMemory + "m" + " freeMemory " + freeMemory + "m"
            )
        }
    }
}