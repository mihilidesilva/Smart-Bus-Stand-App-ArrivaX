package com.example.arrivax

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arrivax.databinding.ActivityMainBinding
import com.example.arrivax.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var homeAdapter: HomeContentAdapter
    private var userRole: String? = null
    private var currentCategory: String = "ALL"
    
    // CONDUCTOR FEATURE: Real-time listener registration to track profile changes
    private var userListener: ListenerRegistration? = null

    // HOME FEATURE: Handler and Runnable to update time/weather in real-time
    private val handler = Handler(Looper.getMainLooper())
    private val timeUpdater = object : Runnable {
        override fun run() {
            // HOME FEATURE: Update the Home content specifically for the "ALL" category every second
            if (currentCategory == "ALL" && binding.mainContentGroup.visibility == View.VISIBLE) {
                updateHomeContent("ALL")
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val currentUser = firebaseAuth.currentUser

        if (currentUser == null || !currentUser.isEmailVerified) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        startUserRealtimeListener(currentUser.uid)
        setupBottomNavigation()

        binding.logoImageView.setOnClickListener {
            navigateToProfile()
        }

        if (savedInstanceState == null) {
            showMainContent()
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(timeUpdater)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(timeUpdater)
    }

    override fun onDestroy() {
        super.onDestroy()
        userListener?.remove()
    }

    private fun setupRecyclerView() {
        homeAdapter = HomeContentAdapter(emptyList())
        binding.contentRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = homeAdapter
        }
    }

    private fun setupListeners() {
        binding.categoryChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val checkedId = checkedIds.firstOrNull()
            
            when (checkedId) {
                R.id.chipSchedules -> {
                    currentCategory = "SCHEDULES"
                    showFragment(ScheduleFragment())
                }
                else -> {
                    currentCategory = when (checkedId) {
                        R.id.chipAll -> "ALL"
                        R.id.chipAbout -> "ABOUT"
                        R.id.chipContact -> "CONTACT"
                        R.id.chipServices -> "SERVICES"
                        else -> "ALL"
                    }
                    showMainContent()
                }
            }
        }
    }

    private fun updateHomeContent(category: String) {
        val contentData = when (category) {
            "ABOUT" -> listOf(
                HomeItem("Smart Bus Stand", "ArrivaX is an IoT-enabled system designed to modernize bus stand management."),
                HomeItem("Our Vision", "To reduce waiting times and improve efficiency for passengers and conductors."),
                HomeItem("Instant Information", "From bus delays to slot availability, everything is synced directly to your phone.")
            )
            "CONTACT" -> listOf(
                HomeItem("Support Line", "+94 11 234 5678", "PHONE"),
                HomeItem("WhatsApp Help", "+94 77 123 4567", "WHATSAPP"),
                HomeItem("Email", "support@arrivax.com", "TEXT")
            )
            "SERVICES" -> listOf(
                HomeItem("Real-time Tracking", "Monitor bus locations and slot occupancy instantly."),
                HomeItem("Delay Predictions", "ML-based analytics to predict upcoming delays."),
                HomeItem("Schedule Management", "View and update bus schedules dynamically.")
            )
            else -> listOf(
                HomeItem("Welcome to ArrivaX", "Your real-time guide to smarter, more transparent bus travel."),
                HomeItem("Today's Weather", getDetailedNaturalWeather())
            )
        }
        homeAdapter.updateData(contentData)
    }

    private fun getDetailedNaturalWeather(): String {
        val calendar = Calendar.getInstance()
        val dayAndTimeFormat = SimpleDateFormat("EEEE, hh:mm:ss a", Locale.getDefault())
        val dayAndTime = dayAndTimeFormat.format(calendar.time)
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val dateFull = dateFormat.format(calendar.time)
        val temp = 29
        val condition = "Partly sunny"
        return "$dayAndTime | $dateFull\n$condition - $temp°C. Good day for travel!"
    }

    private fun startUserRealtimeListener(userId: String) {
        userListener = firestore.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    Toast.makeText(this, "Error listening for updates.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "User"
                    userRole = document.getString("role")?.trim() ?: "PASSENGER"
                    binding.helloTextView.text = "Hello $name !"
                    binding.roleTextView.text = userRole
                }
            }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    // Reset to "All" chip when clicking home
                    binding.chipAll.isChecked = true
                    showMainContent()
                    true
                }
                R.id.navigation_bus -> {
                    startActivity(Intent(this, BusSlotsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_profile -> {
                    navigateToProfile()
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToProfile() {
        val role = userRole?.uppercase()?.trim() ?: "PASSENGER"
        when (role) {
            "ADMIN" -> {
                startActivity(Intent(this, AdminDashboardActivity::class.java))
            }
            "CONDUCTOR" -> {
                showFragment(ConductorProfileFragment())
            }
            else -> {
                startActivity(Intent(this, PassengerProfileActivity::class.java))
            }
        }
        overridePendingTransition(0, 0)
    }

    private fun showMainContent() {
        binding.mainContentGroup.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.GONE
        updateHomeContent(currentCategory)
    }

    private fun showFragment(fragment: Fragment) {
        binding.mainContentGroup.visibility = View.GONE
        binding.fragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
