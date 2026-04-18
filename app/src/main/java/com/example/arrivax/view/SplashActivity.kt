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
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val splashLogo: ImageView = findViewById(R.id.splashLogo)
        splashLogo.startAnimation(fadeInAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            checkUserAndRedirect()
        }, 1800)
    }

    private fun checkUserAndRedirect() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser == null) {
            // No user is signed in, proceed to the standard onboarding flow
            startActivity(Intent(this, OnboardingActivity::class.java))
        } else {
            // User is signed in, ALWAYS go to MainActivity.
            // MainActivity will handle showing the correct screen based on the user's role.
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish() // Finish SplashActivity in both cases
    }
}
