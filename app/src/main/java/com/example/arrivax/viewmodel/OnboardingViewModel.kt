package com.example.arrivax.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("arrivax_prefs", Context.MODE_PRIVATE)

    fun setOnboardingComplete() {
        with(sharedPreferences.edit()) {
            putBoolean(KEY_ONBOARDING_COMPLETE, true)
            apply()
        }
    }

    fun isOnboardingComplete(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    companion object {
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }
}
