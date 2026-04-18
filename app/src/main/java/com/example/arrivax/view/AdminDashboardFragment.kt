package com.example.arrivax.view

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.arrivax.R
import com.example.arrivax.databinding.FragmentAdminDashboardBinding
import com.example.arrivax.viewmodel.AdminDashboardViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminDashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.greetingTextView.text = "Hello $name !"
        }

        viewModel.todaysArrivals.observe(viewLifecycleOwner) { count ->
            binding.arrivalsTextView.text = count.toString()
        }

        viewModel.departuresToday.observe(viewLifecycleOwner) { count ->
            binding.departuresTextView.text = count.toString()
        }

        viewModel.busesOnStand.observe(viewLifecycleOwner) { count ->
            binding.busesOnStandTextView.text = count.toString()
        }

        viewModel.activeDelays.observe(viewLifecycleOwner) { count ->
            binding.activeDelaysTextView.text = count.toString()
        }

        viewModel.pieChartData.observe(viewLifecycleOwner) { data ->
            setupPieChart(binding.pieChart, data)
        }

        viewModel.barChartData.observe(viewLifecycleOwner) { data ->
            setupBarChart(binding.barChart, data)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
        }
    }

    private fun setupClickListeners() {
        binding.manageConductorsCard.setOnClickListener {
            startActivity(Intent(activity, ConductorListActivity::class.java))
        }

        // FINAL FIX: Add the sign out click listener
        binding.signOutCard.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun setupPieChart(pieChart: PieChart, entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "Referer of a Website")
        dataSet.colors = listOf(
            Color.parseColor("#FFAB91"), // No alternative in colors.xml, so hardcoding is necessary
            ContextCompat.getColor(requireContext(), R.color.dark_red)
        )
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        pieChart.data = PieData(dataSet)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = false
        pieChart.legend.textColor = Color.BLACK
        pieChart.animateY(1200)
        pieChart.invalidate()
    }

    private fun setupBarChart(barChart: BarChart, entries: List<BarEntry>) {
        val dataSet = BarDataSet(entries, "Average Delay")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.dark_red)
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 10f

        barChart.data = BarData(dataSet)
        barChart.description.isEnabled = false

        // Use the exact names requested: Slot A01, Slot A02, Slot A03, Slot A04
        val labels = arrayOf("Slot A01", "Slot A02", "Slot A03", "Slot A04")
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        // Fix labels showing at the bottom and ensuring all 4 are visible
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}