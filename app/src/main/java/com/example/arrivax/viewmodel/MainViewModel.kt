package com.example.arrivax.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        // Simulate loading data, e.g., from a repository
        viewModelScope.launch {
            delay(2000) // Simulate a 2-second network call or data load
            _isLoading.value = false
        }
    }
}