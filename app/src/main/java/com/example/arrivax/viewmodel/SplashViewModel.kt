package com.example.arrivax.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val _navigateToNextScreen = MutableLiveData<Boolean>()
    val navigateToNextScreen: LiveData<Boolean> = _navigateToNextScreen

    init {
        // Launch a coroutine in the viewModelScope
        viewModelScope.launch {
            // Wait for 2.5 seconds
            delay(2500)
            // Post an event to navigate to the next screen
            _navigateToNextScreen.postValue(true)
        }
    }
}