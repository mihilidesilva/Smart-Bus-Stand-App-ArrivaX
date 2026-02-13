package com.example.arrivax

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.arrivax.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Install the splash screen
        installSplashScreen().apply {
            // Keep the splash screen on-screen until the UI state is loaded.
            // This line keeps the splash screen visible as long as viewModel.isLoading is true.
            setKeepOnScreenCondition {
                viewModel.isLoading.value
            }
        }

        enableEdgeToEdge()
        // Set the content view to the XML layout
        setContentView(R.layout.activity_main)
    }
}
