package com.RichGuy1.pomodoro.CustomViews

import android.R
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.animation.doOnCancel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.core.graphics.toColorInt

// data classes for the clouds
data class CloudXY(val x: Float, val y: Float, val radius: Float)

class ClockViewComponent
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    val TAG: String = "Time Component"

    // paint objects
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 5f
    }
    private val barIntervalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 3f
    }

    private val middlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#F8F4E3".toColorInt()
        strokeWidth = 5f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#A2A392".toColorInt()
        strokeWidth = 12f
        textSize = 30f
    }
    private val archPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#D4CDC3".toColorInt()
        style = Paint.Style.FILL
    }
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#27d6db".toColorInt()
        style = Paint.Style.FILL
    }

    // this are variables that set objects that are drawn constantly, and the variables that show the length of the minute bars and so on.
    private val tickLength = 20f
    private var oval = RectF()
    private var startAngle = -90f
    private var randomValue = 1

    // useful variables for the calculations
    var centerX = 0f
    var centerY = 0f
    var radius = 0f
    var lastTouchX = 0f
    var lastTouchY = 0f
    var leftExtreme: Int = 0
    var rightExtreme: Int = 0

    // clouds list randomly generated every time
    private val clouds: MutableList<CloudXY> = mutableListOf()

    // variables we will watch to set the timer text and then start the countdown from there.
    private var arcAngle = MutableLiveData<Float>()
    var lastArchAngle: LiveData<Float> = arcAngle
    var hoursPassed = MutableLiveData<Short>()
    var hours: Short = 0

    fun animateTimer(rotateBy: Float) {
        ValueAnimator.ofFloat(arcAngle.value!!, arcAngle.value!! + rotateBy).apply {
            duration = 200
            addUpdateListener { animator ->
                arcAngle.value = animator.animatedValue as Float

                if (lastArchAngle.value!! > 360f) arcAngle.value = 0f
                if (lastArchAngle.value!! < 0f) arcAngle.value = 360f
                invalidate()
            }
            start()
        }

        /*        arcAngle.postValue(arcAngle.value?.plus(rotateBy))
        //        animate().apply {
        //            duration = 200
        //            rotationBy(rotateBy)
        //        }.start()
        //
        //       if (arcAngle.value!! > 360f || arcAngle.value!! < 1) arcAngle.value = 1f
        */

    }

    fun countDownTimer(rotateBy: Float,onAnimatedEnd:(Boolean) -> Unit) {
        var result = true
        ValueAnimator.ofFloat(arcAngle.value!!, arcAngle.value!! - rotateBy).apply {
            duration = 300
            addUpdateListener { animator ->
                arcAngle.value = animator.animatedValue as Float

                if (arcAngle.value!! < 0f || arcAngle.value!! > 360f) {
                    arcAngle.value = 0f;

                    result = false
                    cancel()
                }
                invalidate()
            }
            start()
        }.doOnCancel {
            if (result == false) {
                // increase the hour count
                ++hours
                hoursPassed.postValue(hours)
            }
            onAnimatedEnd(result)
        }

/*        arcAngle.value = arcAngle.value?.minus(rotateBy)

//        if (arcAngle.value!! < 0f || arcAngle.value!!>360f) {
//            arcAngle.value = 0f;
//
//            // increase the hour count
//            ++hours
//            hoursPassed.postValue(hours)
//            return false
//        }
        invalidate() */
//        return result
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        // initialize the arch-angle to 1 degree
        arcAngle.value = 0f
        hoursPassed.value = 0

        centerX = width / 2f
        centerY = height / 2f

        // center the oval of the arch
        val ovalHeight = dpToPx(250f)
        val ovalWidth = ovalHeight


        oval.set(
            centerX - ovalWidth / 2, // Left
            centerY - ovalHeight / 2, // Top
            centerX + ovalWidth / 2, // Right
            centerY + ovalHeight / 2  // Bottom
        )

        // left and right of the view
        leftExtreme = left
        rightExtreme = right
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        centerX = width / 2f
        centerY = height / 2f
        radius = (centerX + centerY) / 3

        // take a random value on each draw call
        randomValue = Random.nextInt(1, clouds.size + 2)

        // drawing the bars of the minutes
        for (i in 0 until 60) {
            val angle =
                if (i == 0) 0.0 else Math.toRadians((i * 6).toDouble()) - Math.toRadians(90.0)
            val startX = centerX + (radius * cos(angle)).toFloat()
            val startY = centerY + (radius * sin(angle)).toFloat()


            // this will handel the extra height of the stroke when it is a multiple of 5 min
            if (i % 5 != 0) {
                val endX = centerX + ((radius + tickLength) * cos(angle)).toFloat()
                val endY = centerY + ((radius + tickLength) * sin(angle)).toFloat()
                canvas.drawLine(startX, startY, endX, endY, barPaint)
            } else {
                val endX = centerX + ((radius + tickLength + 10f) * cos(angle)).toFloat()
                val endY = centerY + ((radius + tickLength + 10f) * sin(angle)).toFloat()
                canvas.drawLine(startX, startY, endX, endY, barIntervalPaint)

                // Calculate text offset based on angle
                val textOffsetX =
                    20 * cos(angle).toFloat() // Adjust the multiplier for distance from the tick
                val textOffsetY =
                    20 * sin(angle).toFloat() // Adjust the multiplier for distance from the tick

                if (i != 0) {
                    canvas.drawText(
                        i.toString(),
                        endX + textOffsetX - tickLength,
                        endY + textOffsetY + (tickLength / 2),
                        textPaint
                    )
                }
            }
        }

        // drawing the clouds surrounding it
        for (x in 0 until clouds.size - 1) {
            // blinking of the stars
            if (x > 0 && randomValue % x == 0) canvas.drawCircle(
                clouds[x].x,
                clouds[x].y,
                clouds[x].radius + 0.4f,
                starPaint
            )
            else {
                canvas.drawCircle(clouds[x].x, clouds[x].y, clouds[x].radius, starPaint)
            }

        }

        // minutes overlay drawing
        canvas.drawArc(oval, startAngle, lastArchAngle.value!!, true, archPaint)

        // draw the middle circle
        canvas.drawCircle(centerX, centerY, 48f, middlePaint)

    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    /**
     * Method generates a bunch of stars which are basically a circle and somme of them will blink
     * @param startPoint : The far-left of the view from where you want to the stars to appear
     * @param endPoint : The far-right of the view until where you want to the stars to appear
     */
    private fun getCloudCoordinates(startPoint: Double, endPoint: Double) {
        for (i in 0 until 524)
            clouds.add(
                CloudXY(
                    Random.nextDouble(startPoint, endPoint).toFloat(),
                    Random.nextDouble(startPoint, endPoint).toFloat(),
                    Random.nextDouble(0.2, 1.2).toFloat()
                )
            )
    }


    /**
     * Look for circular motion on the timer to determine whether to increase or decrease the angle of the timer
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                val centerX = (oval.left + oval.right) / 2
                val centerY = (oval.top + oval.bottom) / 2

                val currentAngle = Math.toDegrees(
                    atan2(
                        (event.y - centerY).toDouble(),
                        (event.x - centerX).toDouble()
                    )
                ).toFloat()

                val lastAngle = Math.toDegrees(
                    atan2(
                        (lastTouchY - centerY).toDouble(),
                        (lastTouchX - centerX).toDouble()
                    )
                ).toFloat()

                val angleDifference = currentAngle - lastAngle

                if (angleDifference > 0) {
                    animateTimer(+angleDifference * 0.70f)
                } else {
                    animateTimer(angleDifference * 0.70f)
                }

                invalidate()
            }
        }
        return true
    }

    /**
     * This method is to change the background color of the center of the component
     */
    fun setTimerCenterColour(color: String) {
        try {
            middlePaint.color = color.toColorInt()
        } catch (e: IllegalArgumentException) {
            Log.e(
                TAG,
                "Can't set the colour to the center of the timer component. \n Error: ${e.message.toString()}"
            )
        }
        reDraw()
    }

    /**
     * set timer font
     */
    fun setTimerFont(fontTypeface: Typeface) {
        try {
            textPaint.setTypeface(fontTypeface)
        } catch (e: Exception) {
            Log.e(TAG, "Can't set the font to timer component. \n Error: ${e.message.toString()}")
        }
        reDraw()
    }

    /**
     * set the timer text colour
     */
    fun setTimerFontColor(color: String) {
        try {
            textPaint.color = color.toColorInt()
        } catch (e: IllegalArgumentException) {
            Log.e(
                TAG,
                "Can't set the font colour fo the timer component. \n Error: ${e.message.toString()}"
            )
        }
        reDraw()
    }

    /**
     * set the colour of the actual arch of the timer component
     */
    fun setTimerArchColor(color: String) {
        try {
            archPaint.color = color.toColorInt()
        } catch (e: IllegalArgumentException) {
            Log.e(
                TAG,
                "Can't set the font colour fo the timer component. \n Error: ${e.message.toString()}"
            )
        }
        reDraw()
    }

    /**
     * Call this method if you want to have a background of the stars
     * Call the @return disableBackgroundStars() to disable the stars
     * @exception Exception: Only call this method when the layout of the view is loaded
     */
    fun enableBackgroundStars() {
        clouds.clear()
        if (left == 0 && right == 0) throw Exception("The layout of the component is not yet completely initialized, call after layout is calculated, doOnLayout{}")
        getCloudCoordinates(left.toDouble(), right.toDouble())
        reDraw()
    }

    fun disableBackgroundStars() {
        clouds.clear()
        reDraw()
    }

    /**
     * set the colour of the segment bar of the timer clock
     */
    fun setTimerBarColor(barColor: String, intervalColor: String? = "#FFFFFF") {
        try {
            barPaint.color = barColor.toColorInt();barIntervalPaint.color =
                intervalColor!!.toColorInt()
        } catch (e: IllegalArgumentException) {
            Log.e(
                TAG,
                "Can't set the bar colour fo the timer component. \n Error: ${e.message.toString()}"
            )
        }
        reDraw()
    }

    /**
     * This method is used when we want to use the component as a clock which goes forward.
     */
    fun startClock() {
        val timer = object : CountDownTimer((1.toLong() * 6) * 60000, 250) {
            override fun onTick(millisUntilFinished: Long) {
                // minus will change to plus when it is subtracted in the countDownTimer method
                // replace with this countDownTimer(-0.004166666675f * 6f)
                countDownTimer(-1.333333334f * 6f,{ result ->
                        if(result == false) onFinish()
                })
            }

            override fun onFinish() {}
        }
        timer.start()
    }

    /**
     * Call this method to invalidate the cache and re-draw the component after cosmetic changes
     */
    fun reDraw() = invalidate()

    /**
     * converts the dp to px, standard method
     */
    fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

}