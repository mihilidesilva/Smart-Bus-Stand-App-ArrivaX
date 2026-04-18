package com.example.arrivax.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.arrivax.R
import com.example.arrivax.viewmodel.OnboardingViewModel

class OnboardingActivity : AppCompatActivity() {

    private val viewModel: OnboardingViewModel by viewModels()
    private lateinit var viewPager: ViewPager2
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.onboardingViewPager)
        val nextButton = findViewById<Button>(R.id.nextButton)
        val skipButton = findViewById<Button>(R.id.skipButton)
        backButton = findViewById(R.id.backButton)

        val adapter = OnboardingViewPagerAdapter(this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Corrected logic: only the 'Back' button visibility changes.
                if (position == 0) {
                    backButton.visibility = View.INVISIBLE
                } else {
                    backButton.visibility = View.VISIBLE
                }
            }
        })

        nextButton.setOnClickListener {
            if (viewPager.currentItem < adapter.itemCount - 1) {
                viewPager.currentItem += 1
            } else {
                completeOnboarding()
            }
        }

        backButton.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }

        skipButton.setOnClickListener {
            completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        viewModel.setOnboardingComplete()
        // Navigate to RoleSelectionActivity instead of MainActivity
        val intent = Intent(this, RoleSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }
}
