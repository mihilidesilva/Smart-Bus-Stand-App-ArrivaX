package com.example.arrivax.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.arrivax.R

class RoleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        val passengerButton: Button = findViewById(R.id.passengerButton)
        val conductorButton: Button = findViewById(R.id.conductorButton)
        val adminButton: Button = findViewById(R.id.adminButton)

        passengerButton.setOnClickListener {
            // Navigate to the standard registration screen for passengers
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        conductorButton.setOnClickListener {
            // Corrected: Navigate directly to the Login page for conductors
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        adminButton.setOnClickListener {
            // Navigate to the new Admin Registration screen
            val intent = Intent(this, AdminRegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
