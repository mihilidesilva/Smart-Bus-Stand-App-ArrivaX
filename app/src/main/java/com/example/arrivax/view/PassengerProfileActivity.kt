package com.example.arrivax.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.arrivax.MainActivity
import com.example.arrivax.R
import com.example.arrivax.databinding.ActivityPassengerProfileBinding
import com.example.arrivax.viewmodel.PassengerProfileViewModel
import com.google.firebase.auth.FirebaseAuth

class PassengerProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPassengerProfileBinding
    private val viewModel: PassengerProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Make the activity edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = ActivityPassengerProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWindowInsets()
        setupClickListeners()
        setupBottomNavigation()
        observeViewModel()

        viewModel.loadPassengerProfile()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
    }

    private fun observeViewModel() {
        viewModel.passenger.observe(this) { passenger ->
            if (passenger != null) {
                binding.passengerNameTextView.text = passenger.name
                binding.passengerEmailTextView.text = passenger.email
                binding.mobileNumberTextView.text = if (passenger.mobileNumber.isEmpty()) "Mobile: Not Set" else passenger.mobileNumber
                binding.statusTextView.text = passenger.status
                
                if (passenger.status == "ACTIVE") {
                    binding.statusTextView.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                } else {
                    binding.statusTextView.setTextColor(android.graphics.Color.RED)
                }
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_profile
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_bus -> {
                    startActivity(Intent(this, BusSlotsActivity::class.java))
                    finish()
                    true
                }
                R.id.navigation_profile -> true
                else -> false
            }
        }
    }

    private fun setupClickListeners() {
        // Dropdown Menu for Settings (Edit, Logout & Delete)
        binding.settingsButton.setOnClickListener { view ->
            val contextWrapper: Context = ContextThemeWrapper(this, R.style.PopupMenuTheme)
            val popup = PopupMenu(contextWrapper, view)
            popup.menuInflater.inflate(R.menu.passenger_profile_menu, popup.menu)
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit_profile -> {
                        openEditProfilePopup()
                        true
                    }
                    R.id.menu_logout -> {
                        performLogout()
                        true
                    }
                    R.id.menu_delete_account -> {
                        showDeleteConfirmationDialog()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun openEditProfilePopup() {
        val editFragment = EditConductorProfileFragment()
        editFragment.show(supportFragmentManager, "EditProfile")
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val user = FirebaseAuth.getInstance().currentUser
                user?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account deleted successfully.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Session expired. Please re-login to delete account.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
