package com.RichGuy1.pomodoro.CustomViews

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.RichGuy1.pomodoro.R

class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr)  {

    // brushes and modes
    private val imageBrush: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)

    private var x : Float = 0f
    private var y: Float = 0f

    private val imageBufferSrc: Bitmap =
        ResourcesCompat.getDrawable(context.resources, R.drawable.src_min,null).toBitmap()
    private val imageBufferDst: Bitmap =
        ResourcesCompat.getDrawable(context.resources, R.drawable.dest_min,null).toBitmap()

    private var imageBufferS: Bitmap? = null
    private var imageBufferD: Bitmap? = null

    // variables
    private var centerX = 0f
    private var centerY = 0f


    // init block
    init {
        imageBufferS = scaleBitmap(imageBufferSrc,200,200)
        imageBufferD = scaleBitmap(imageBufferDst,200,200)
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        if (bitmap.width <= maxWidth && bitmap.height <= maxHeight) {
            return bitmap // No scaling needed
        }

        val ratioX = maxWidth.toDouble() / bitmap.width.toDouble()
        val ratioY = maxHeight.toDouble() / bitmap.height.toDouble()
        val ratio = kotlin.math.min(ratioX, ratioY)

        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        // calculate the center of the view
        centerX = ((left + right) / 2).toFloat()
        centerY = ((top + bottom) / 2).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // draw a circle
        canvas.drawBitmap(imageBufferS!!,100f,100f,imageBrush)
        imageBrush.xfermode = mode
        canvas.drawBitmap(imageBufferD!!,140f,140f,imageBrush)
        imageBrush.xfermode = null
    }
}