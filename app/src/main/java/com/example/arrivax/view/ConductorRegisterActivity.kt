package com.example.arrivax.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.arrivax.databinding.ActivityConductorRegisterBinding
import com.example.arrivax.viewmodel.ConductorRegisterViewModel
import com.example.arrivax.viewmodel.RegistrationState

class ConductorRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConductorRegisterBinding
    private val viewModel: ConductorRegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConductorRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.registerButton.setOnClickListener {
            // CORRECT: Get text directly from the EditText widgets
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val busNumber = binding.busNumberEditText.text.toString().trim()
            val pass = binding.passwordEditText.text.toString()
            val confirmPass = binding.confirmPasswordEditText.text.toString()

            if (name.isEmpty() || email.isEmpty() || busNumber.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            viewModel.registerConductor(name, email, busNumber, pass)
        }
    }

    private fun observeViewModel() {
        viewModel.registrationState.observe(this, Observer { state ->
            when (state) {
                is RegistrationState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.registerButton.isEnabled = false
                }
                is RegistrationState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    // Registration successful, finish and return to the list
                    finish()
                }
                is RegistrationState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.registerButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}