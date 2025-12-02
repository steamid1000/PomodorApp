package com.RichGuy1.pomodoro

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.RichGuy1.pomodoro.databinding.ActivityOverlayBinding

class OverlayActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOverlayBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOverlayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // disable the inbuilt feature of the view, so we can control the value of the arch
        binding.timeLayer.setOnTouchListener {v,event ->
            when(event.action){
               MotionEvent.ACTION_DOWN ->{
                  true
               }
                       MotionEvent.ACTION_MOVE->{
                         true
                       }

                else -> {
                  true
                }
            }
        }
        binding.timeLayer.setTimerBarColor("#FFFFFF","#FFFFFF")
        binding.timeLayer.setTimerFontColor("#FFFFFF")

        // now lets call the new method to rotate the timer as a watch time
        binding.timeLayer.startClock()


       binding.nextPhaseBtn.setOnClickListener{
           startActivity(Intent(this@OverlayActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
           finish()
       }


        binding.timeLayer.hoursPassed.observe(this@OverlayActivity) { value ->
            binding.hoursPassed.text = value.toString()
        }

        /* overlay drawing.
        binding.overlay.animate().apply {
            duration = 800
            rotationBy(290f)
        }.withEndAction {
            binding.overlay.animate().apply {
                duration = 1200
                rotationBy(-290f)
            }
        }.start()
         */
    }
}