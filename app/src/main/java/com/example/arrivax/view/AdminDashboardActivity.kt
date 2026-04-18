package com.example.arrivax.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.arrivax.MainActivity
import com.example.arrivax.R
import com.example.arrivax.databinding.ActivityAdminDashboardBinding
import com.example.arrivax.viewmodel.AdminDashboardViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val viewModel: AdminDashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.userName.observe(this) { name ->
            binding.greetingTextView.text = "Hello $name !"
        }

        viewModel.todaysArrivals.observe(this) { count ->
            binding.arrivalsTextView.text = count.toString()
        }

        viewModel.departuresToday.observe(this) { count ->
            binding.departuresTextView.text = count.toString()
        }

        viewModel.busesOnStand.observe(this) { count ->
            binding.busesOnStandTextView.text = count.toString()
        }

        viewModel.activeDelays.observe(this) { count ->
            binding.activeDelaysTextView.text = count.toString()
        }

        viewModel.barChartData.observe(this) { data ->
            setupBarChart(binding.barChart, data)
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        }
    }

    private fun setupClickListeners() {
        binding.manageConductorsCard.setOnClickListener {
            startActivity(Intent(this, ConductorListActivity::class.java))
        }

        // Handle Manage Schedules Click
        binding.manageSchedulesCard.setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java))
        }

        binding.signOutCard.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.bottomNavigationView.selectedItemId = R.id.navigation_profile
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_bus -> {
                    startActivity(Intent(this, BusSlotsActivity::class.java))
                    finish()
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.navigation_profile -> true
                else -> false
            }
        }
    }

    private fun setupBarChart(barChart: BarChart, entries: List<BarEntry>) {
        val dataSet = BarDataSet(entries, "Predicted Delay (mins)")
        dataSet.color = Color.parseColor("#B71C1C")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f

        barChart.data = BarData(dataSet)
        barChart.description.isEnabled = false

        // Exact labels as requested: Slot A01, Slot A02, Slot A03, Slot A04
        val labels = arrayOf("Slot A01", "Slot A02", "Slot A03", "Slot A04")
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        // Force labels to the bottom and ensure all 4 are visible
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.granularity = 1f
        barChart.xAxis.isGranularityEnabled = true
        barChart.xAxis.labelCount = 4

        barChart.xAxis.textColor = Color.BLACK
        barChart.axisLeft.textColor = Color.BLACK
        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.animateY(1200)
        barChart.invalidate()
    }
}