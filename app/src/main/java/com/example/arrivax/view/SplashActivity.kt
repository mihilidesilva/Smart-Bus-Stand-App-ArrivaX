package com.example.arrivax.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.arrivax.MainActivity
import com.example.arrivax.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Find the ImageView for the logo
        val splashLogo: ImageView = findViewById(R.id.splash_logo)

        // Load the fade-in animation
        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        // Start the animation on the logo
        splashLogo.startAnimation(fadeInAnimation)

        // Use a Handler to delay the navigation to the main screen
        Handler(Looper.getMainLooper()).postDelayed({
            // Create an Intent to start MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            // Finish SplashActivity so the user cannot navigate back to it
            finish()
        }, 2500) // 2500 milliseconds = 2.5 seconds
    }
}