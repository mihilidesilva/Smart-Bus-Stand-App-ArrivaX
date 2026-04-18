package com.example.arrivax.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arrivax.R
import com.example.arrivax.adapter.ScheduleAdapter
import com.example.arrivax.databinding.FragmentScheduleBinding
import com.example.arrivax.model.Schedule
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ScheduleAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val scheduleList = mutableListOf<Schedule>()
    private val filteredList = mutableListOf<Schedule>()
    private var selectedSlot: String = "All Slots"
    private var selectedTimeFilter: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupTabs()
        setupSearchAndFilters()
        loadSchedules()
    }

    private fun setupRecyclerView() {
        adapter = ScheduleAdapter(filteredList, isAdmin = false)
        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.scheduleRecyclerView.adapter = adapter
    }

    private fun setupTabs() {
        binding.slotTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedSlot = tab?.text.toString()
                applyFilters()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSearchAndFilters() {
        // Search Logic
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Time Chip Filters
        binding.timeFilterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedTimeFilter = when (checkedIds.firstOrNull()) {
                R.id.chipMorning -> "Morning"
                R.id.chipAfternoon -> "Afternoon"
                R.id.chipEvening -> "Evening"
                else -> null
            }
            applyFilters()
        }
    }

    private fun applyFilters() {
        val query = binding.searchEt.text.toString().lowercase(Locale.getDefault())
        filteredList.clear()

        scheduleList.forEach { schedule ->
            val matchesSearch = schedule.route.lowercase(Locale.getDefault()).contains(query) ||
                                schedule.busNumber.lowercase(Locale.getDefault()).contains(query)
            
            val matchesSlot = if (selectedSlot == "All Slots") {
                true
            } else {
                val slotDigit = selectedSlot.filter { it.isDigit() }
                schedule.route.contains(slotDigit) || schedule.route.contains(selectedSlot, ignoreCase = true)
            }

            val matchesTime = when (selectedTimeFilter) {
                "Morning" -> isTimeInRange(schedule.departureTime, 0, 12)
                "Afternoon" -> isTimeInRange(schedule.departureTime, 12, 17)
                "Evening" -> isTimeInRange(schedule.departureTime, 17, 24)
                else -> true
            }

            if (matchesSearch && matchesSlot && matchesTime) {
                filteredList.add(schedule)
            }
        }
        
        adapter.updateData(filteredList)
        binding.noSchedulesTextView.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun isTimeInRange(timeStr: String, startHour: Int, endHour: Int): Boolean {
        if (timeStr.isEmpty() || timeStr == "N/A") return false
        return try {
            val sdf = SimpleDateFormat("h:mm a", Locale.US)
            val date = sdf.parse(timeStr.trim()) ?: return false
            val cal = Calendar.getInstance().apply { time = date }
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            hour in startHour until endHour
        } catch (e: Exception) {
            false
        }
    }

    private fun loadSchedules() {
        binding.progressBar.visibility = View.VISIBLE
        firestore.collection("schedules")
            .addSnapshotListener { value, error ->
                if (!isAdded) return@addSnapshotListener
                binding.progressBar.visibility = View.GONE
                if (error != null) return@addSnapshotListener

                scheduleList.clear()
                value?.documents?.forEach { doc ->
                    doc.toObject(Schedule::class.java)?.copy(id = doc.id)?.let { scheduleList.add(it) }
                }
                applyFilters()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
