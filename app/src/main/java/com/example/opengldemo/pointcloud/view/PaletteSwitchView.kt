package com.example.opengldemo.pointcloud.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import com.example.opengldemo.R
import kotlinx.android.synthetic.main.layout_palette_switch.view.*

/**
 *  涂抹板切换控件
 */
class PaletteSwitchView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr) {

    private var mListener: OnClickListener? = null

    private val onClickListener = OnClickListener {
        when (it.id) {
            R.id.pen -> {
                pen.isSelected = true
                eraser.isSelected = false
                mListener?.onPenClick()
            }

            R.id.eraser -> {
                pen.isSelected = false
                eraser.isSelected = true
                mListener?.onEraserClick()
            }
        }

    }

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_palette_switch, this)
        pen.setOnClickListener(onClickListener)
        eraser.setOnClickListener(onClickListener)
        pen.isSelected = true
        eraser.isSelected = false
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }

    interface OnClickListener {
        fun onPenClick()

        fun onEraserClick()
    }

}