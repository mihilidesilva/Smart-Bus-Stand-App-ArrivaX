package com.example.arrivax.view

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arrivax.R
import com.example.arrivax.adapter.ScheduleAdapter
import com.example.arrivax.databinding.ActivityScheduleBinding
import com.example.arrivax.model.Schedule
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding
    private lateinit var adapter: ScheduleAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val scheduleList = mutableListOf<Schedule>()
    private val filteredList = mutableListOf<Schedule>()
    private var selectedTimeFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadSchedules()
        setupSearchAndFilters()

        binding.backButton.setOnClickListener { finish() }
        binding.addNewScheduleBtn.setOnClickListener { showScheduleDialog(null) }
        binding.dateFilterBtn.setOnClickListener { showCalendarPicker() }
    }

    private fun setupRecyclerView() {
        adapter = ScheduleAdapter(
            filteredList,
            isAdmin = true,
            onEditClick = { showScheduleDialog(it) },
            onDeleteClick = { deleteSchedule(it) }
        )
        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.scheduleRecyclerView.adapter = adapter
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
            
            val matchesTime = when (selectedTimeFilter) {
                "Morning" -> isTimeInRange(schedule.departureTime, 0, 12)   // 12 AM - 12 PM
                "Afternoon" -> isTimeInRange(schedule.departureTime, 12, 17) // 12 PM - 5 PM
                "Evening" -> isTimeInRange(schedule.departureTime, 17, 24)   // 5 PM - 12 AM
                else -> true
            }

            if (matchesSearch && matchesTime) {
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
                binding.progressBar.visibility = View.GONE
                if (error != null) return@addSnapshotListener
                scheduleList.clear()
                value?.documents?.forEach { doc ->
                    doc.toObject(Schedule::class.java)?.copy(id = doc.id)?.let { scheduleList.add(it) }
                }
                applyFilters()
            }
    }

    private fun showCalendarPicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val dateString = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selection))
            binding.dateFilterBtn.text = dateString
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showScheduleDialog(schedule: Schedule?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val routeEt = dialogView.findViewById<EditText>(R.id.routeEditText)
        val busNumEt = dialogView.findViewById<EditText>(R.id.busNumberEditText)
        val depTimeEt = dialogView.findViewById<EditText>(R.id.departureTimeEditText)
        val arrTimeEt = dialogView.findViewById<EditText>(R.id.arrivalTimeEditText)
        val daysAutoComplete = dialogView.findViewById<AutoCompleteTextView>(R.id.daysAutoComplete)
        val saveBtn = dialogView.findViewById<MaterialButton>(R.id.saveBtn)
        val cancelBtn = dialogView.findViewById<MaterialButton>(R.id.cancelBtn)

        // Setup Dropdown for Days
        val dayOptions = arrayOf("Daily", "Weekdays", "Weekends", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val dayAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dayOptions)
        daysAutoComplete.setAdapter(dayAdapter)

        schedule?.let {
            routeEt.setText(it.route)
            busNumEt.setText(it.busNumber)
            depTimeEt.setText(it.departureTime)
            arrTimeEt.setText(it.arrivalTime)
            daysAutoComplete.setText(it.days, false)
        }

        depTimeEt.setOnClickListener { showTimePicker(depTimeEt) }
        arrTimeEt.setOnClickListener { showTimePicker(arrTimeEt) }

        saveBtn.setOnClickListener {
            val route = routeEt.text.toString()
            val busNum = busNumEt.text.toString()
            val depTime = depTimeEt.text.toString()
            val arrTime = arrTimeEt.text.toString()
            val days = daysAutoComplete.text.toString()

            if (route.isEmpty()) {
                Toast.makeText(this, "Route is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newSchedule = Schedule(
                route = route,
                busNumber = busNum,
                departureTime = depTime,
                arrivalTime = arrTime,
                days = days
            )

            if (schedule == null) firestore.collection("schedules").add(newSchedule)
            else firestore.collection("schedules").document(schedule.id).set(newSchedule)
            
            syncWithRealtimeDatabase(newSchedule)
            dialog.dismiss()
        }

        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun syncWithRealtimeDatabase(schedule: Schedule) {
        val slotId = when {
            schedule.route.contains("01") -> "slot1"
            schedule.route.contains("02") -> "slot2"
            schedule.route.contains("03") -> "slot3"
            schedule.route.contains("04") -> "slot4"
            else -> null
        }
        if (slotId != null) {
            FirebaseDatabase.getInstance().reference.child("slots_info").child(slotId).updateChildren(hashMapOf<String, Any>(
                "route" to schedule.route,
                "expected_arrival" to schedule.departureTime,
                "scheduled_arrival" to schedule.departureTime
            ))
        }
    }

    private fun showTimePicker(editText: EditText) {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, h, m ->
            val amPm = if (h < 12) "AM" else "PM"
            val hour = if (h > 12) h - 12 else if (h == 0) 12 else h
            editText.setText(String.format("%02d:%02d %s", hour, m, amPm))
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
    }

    private fun deleteSchedule(schedule: Schedule) {
        AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Confirm delete?")
            .setPositiveButton("Delete") { _, _ -> firestore.collection("schedules").document(schedule.id).delete() }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
