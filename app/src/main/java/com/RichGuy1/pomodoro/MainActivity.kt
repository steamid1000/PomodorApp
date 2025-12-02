package com.RichGuy1.pomodoro

import android.app.StatusBarManager
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.rotationMatrix
import androidx.core.graphics.toColorInt
import androidx.core.util.TimeUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import com.RichGuy1.pomodoro.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var state = false
    private lateinit var media: MediaPlayer
    private lateinit var ambience: MediaPlayer
    var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge(
            SystemBarStyle.auto(
                Color.parseColor("#000512"),
                Color.parseColor("#000512")
            )
        )
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        media = MediaPlayer.create(this@MainActivity, R.raw.timer_end_sound).apply {
            isLooping = false
        }
        ambience = MediaPlayer.create(this@MainActivity, R.raw.ambience).apply {
            isLooping = true
        }

        // start the ambience music
        ambience.start()

        media.setOnCompletionListener {
            media.stop()
            media.seekTo(0)
        }

        // initial animations for the timer layer
        binding.timeLayer.alpha = 0f
        binding.timeLayer.animate().apply {
            duration = 1200
            alpha(1f)
        }.start()

        // animation for the text
        binding.minsLeft.x = binding.minsLeft.x + 400
        binding.minJustText.alpha = 0f
        binding.minsLeft.animate().apply {
            duration = 1400
            translationXBy(-400f)
        }.withEndAction {
            binding.minJustText.animate().apply {
                duration = 400
                alpha(1f)
            }
        }.start()

        // listener for the timer value change
        binding.timeLayer.lastArchAngle.observe(this@MainActivity) { angle ->
            // here i am converting the angle to minutes by dividing it by 6
            binding.minsLeft.text = (angle.toInt() / 6).toString()
        }
        binding.timeLayer.setTimerCenterColour("#FFA07A")
        binding.timeLayer.setTimerFontColor("#6AFACD")
        binding.timeLayer.setTimerArchColor("#12FCAC")

        // play pause listener
        binding.controls.setOnClickListener {
            // disable the timer
            val angle = binding.timeLayer.lastArchAngle.value!!

            timer = object : CountDownTimer((angle.toLong() * 6) * 60000, 250) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.timeLayer.countDownTimer(0.004166666675f * 6f,{ result ->
                        if (result == false) onFinish()
                    })
                }

                override fun onFinish() {
                    binding.controls.setBackgroundResource(R.drawable.play_icon)
                    state = false
                }

            }
            if (state == false) {
                binding.controls.setBackgroundResource(R.drawable.pause_icon)
                timer?.cancel()
                timer?.start()
                state = true
            } else {
                binding.controls.setBackgroundResource(R.drawable.play_icon)
            }
        }


        // exp for the overlay activity listener
        binding.overlayBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    OverlayActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }

        binding.timeLayer.doOnLayout {
            try {
                binding.timeLayer.enableBackgroundStars()
            } catch (e: Exception) {
                Log.e("Main Activity", e.message.toString())
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        media.release()
        ambience.stop()
        ambience.release()
        timer?.cancel()
    }

    override fun onPause() {
        super.onPause()
        ambience.pause()
    }

    override fun onResume() {
        super.onResume()
        ambience.start()

    }
}