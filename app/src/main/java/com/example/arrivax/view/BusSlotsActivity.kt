package com.example.arrivax.view

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.arrivax.MainActivity
import com.example.arrivax.R
import com.example.arrivax.databinding.ActivityBusSlotsBinding
import com.example.arrivax.model.SlotData
import com.example.arrivax.viewmodel.BusSlotsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BusSlotsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBusSlotsBinding
    private val viewModel: BusSlotsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBusSlotsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()
        setupCardClickListeners()
        setupBottomNavigation()

        binding.profileImageView.setOnClickListener { navigateToProfile() }
    }

    private fun observeViewModel() {
        viewModel.userName.observe(this) { name ->
            binding.helloTextView.text = "Hello $name !"
        }

        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        viewModel.slot1.observe(this) { slotData ->
            updateSlotUI(binding.slot1Card, binding.slot1Status, binding.slot1Route, binding.slot1Delay, binding.slot1Reason, binding.slot1Arrival, binding.slot1Updated, slotData)
        }

        viewModel.slot2.observe(this) { slotData ->
            updateSlotUI(binding.slot2Card, binding.slot2Status, binding.slot2Route, binding.slot2Delay, binding.slot2Reason, binding.slot2Arrival, binding.slot2Updated, slotData)
        }

        viewModel.slot3.observe(this) { slotData ->
            updateSlotUI(binding.slot3Card, binding.slot3Status, binding.slot3Route, binding.slot3Delay, binding.slot3Reason, binding.slot3Arrival, binding.slot3Updated, slotData)
        }

        viewModel.slot4.observe(this) { slotData ->
            updateSlotUI(binding.slot4Card, binding.slot4Status, binding.slot4Route, binding.slot4Delay, binding.slot4Reason, binding.slot4Arrival, binding.slot4Updated, slotData)
        }
    }

    private fun updateSlotUI(card: MaterialCardView, statusTv: TextView, routeTv: TextView, delayTv: TextView, reasonTv: TextView, arrivalTv: TextView, updatedTv: TextView, slotData: SlotData) {
        routeTv.text = "🚌 ${slotData.route}"
        arrivalTv.text = "Expected Arrival: ${slotData.expectedArrival}"
        arrivalTv.setTypeface(null, Typeface.BOLD)
        
        when {
            slotData.delay > 0 -> {
                delayTv.text = "Delay: ${slotData.delay} Minutes"
                delayTv.setTextColor(ContextCompat.getColor(this, R.color.dark_red))
                delayTv.setTypeface(null, Typeface.BOLD)
                reasonTv.text = "Reason: ${slotData.reason}"
                reasonTv.visibility = View.VISIBLE
                reasonTv.setTextColor(ContextCompat.getColor(this, R.color.dark_red))
                arrivalTv.setTextColor(ContextCompat.getColor(this, R.color.dark_red))
            }
            slotData.delay == -1L -> {
                delayTv.text = "Delay: Delayed"
                delayTv.setTextColor(ContextCompat.getColor(this, R.color.dark_red))
                delayTv.setTypeface(null, Typeface.BOLD)
                reasonTv.text = "Reason: ${slotData.reason}"
                reasonTv.visibility = View.VISIBLE
                reasonTv.setTextColor(ContextCompat.getColor(this, R.color.dark_red))
                arrivalTv.setTextColor(ContextCompat.getColor(this, R.color.dark_red))
            }
            else -> {
                delayTv.text = "Delay: On Time"
                delayTv.setTextColor(ContextCompat.getColor(this, R.color.status_active))
                delayTv.setTypeface(null, Typeface.NORMAL)
                reasonTv.visibility = View.GONE
                arrivalTv.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }

        updatedTv.text = "Actual Time: ${slotData.lastUpdated}"
        statusTv.setTextColor(Color.WHITE)
        if (slotData.status.equals("OCCUPIED", ignoreCase = true)) {
            statusTv.text = "OCCUPIED"
            statusTv.setBackgroundResource(R.drawable.status_bg_occupied)
            card.strokeColor = ContextCompat.getColor(this, R.color.dark_red)
        } else {
            statusTv.text = "FREE"
            statusTv.setBackgroundResource(R.drawable.status_bg_free)
            card.strokeColor = ContextCompat.getColor(this, R.color.status_active)
        }
    }

    private fun setupCardClickListeners() {
        binding.slot1Card.setOnClickListener { viewModel.slot1.value?.let { handleSlotClick(it) } }
        binding.slot2Card.setOnClickListener { viewModel.slot2.value?.let { handleSlotClick(it) } }
        binding.slot3Card.setOnClickListener { viewModel.slot3.value?.let { handleSlotClick(it) } }
        binding.slot4Card.setOnClickListener { viewModel.slot4.value?.let { handleSlotClick(it) } }
    }

    private fun handleSlotClick(slotData: SlotData) {
        val userRole = viewModel.userRole.value ?: "PASSENGER"
        
        if (userRole == "ADMIN") {
            openEditableDialog(slotData)
            return
        }

        if (userRole == "CONDUCTOR") {
            val myBus = viewModel.assignedBusNumber.value?.replace(Regex("[^A-Za-z0-9]"), "")?.uppercase() ?: ""
            val slotBus = slotData.busNumber.replace(Regex("[^A-Za-z0-9]"), "").uppercase()
            val routeText = slotData.route.replace(Regex("[^A-Za-z0-9]"), "").uppercase()

            val isAuthorized = myBus.isNotEmpty() && (
                slotBus.contains(myBus) || 
                myBus.contains(slotBus) || 
                routeText.contains(myBus)
            )

            if (isAuthorized) {
                openEditableDialog(slotData)
            } else {
                val assignedDisplayName = viewModel.assignedBusNumber.value ?: "Unknown"
                Toast.makeText(this, "Denied: You manage $assignedDisplayName, not ${slotData.busNumber}", Toast.LENGTH_LONG).show()
                openViewOnlyDialog(slotData)
            }
        } else {
            openViewOnlyDialog(slotData)
        }
    }

    private fun openViewOnlyDialog(slotData: SlotData) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_slot_details, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()
        dialogView.findViewById<TextView>(R.id.dialogSlotId).text = slotData.displayName
        dialogView.findViewById<TextView>(R.id.dialogRoute).text = "Route: ${slotData.route}"
        dialogView.findViewById<TextView>(R.id.dialogStatus).text = "Status: ${slotData.status}"
        val delayTv = dialogView.findViewById<TextView>(R.id.dialogDelay)
        val reasonTv = dialogView.findViewById<TextView>(R.id.dialogReason)
        when {
            slotData.delay > 0 -> {
                delayTv.text = "Delay: ${slotData.delay} Minutes"
                delayTv.setTextColor(ContextCompat.getColor(this, R.color.dark_red))
                reasonTv.text = "Reason: ${slotData.reason}"
                reasonTv.visibility = View.VISIBLE
            }
            slotData.delay == -1L -> {
                delayTv.text = "Delay: Delayed"
                delayTv.setTextColor(ContextCompat.getColor(this, R.color.dark_red))
                reasonTv.text = "Reason: ${slotData.reason}"
                reasonTv.visibility = View.VISIBLE
            }
            else -> {
                delayTv.text = "Delay: On Time"
                delayTv.setTextColor(ContextCompat.getColor(this, R.color.status_active))
                reasonTv.visibility = View.GONE
            }
        }
        dialogView.findViewById<TextView>(R.id.lastUpdatedTextView).apply {
            text = "Last Updated: ${slotData.lastUpdated}"
            visibility = View.VISIBLE
        }
        dialogView.findViewById<MaterialButton>(R.id.closeButton).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun openEditableDialog(slotData: SlotData) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_slot, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val routeTv = dialogView.findViewById<TextView>(R.id.dialogRouteTextView)
        val assignedBusTv = dialogView.findViewById<TextView>(R.id.dialogAssignedBusTextView)
        val slotIdTv = dialogView.findViewById<TextView>(R.id.dialogSlotIdTextView)
        val statusTv = dialogView.findViewById<TextView>(R.id.dialogStatusTextView)
        val delayEt = dialogView.findViewById<TextInputEditText>(R.id.delayMinutesEditText)
        val reasonAutoComplete = dialogView.findViewById<AutoCompleteTextView>(R.id.delayReasonAutoComplete)
        val delayedSwitch = dialogView.findViewById<SwitchMaterial>(R.id.markDelayedSwitch)
        val updateBtn = dialogView.findViewById<MaterialButton>(R.id.updateSlotButton)
        val cancelBtn = dialogView.findViewById<MaterialButton>(R.id.cancelButton)

        routeTv.text = slotData.route
        slotIdTv.text = "Slot ID: ${slotData.displayName}"
        statusTv.text = "Status: ${slotData.status}"
        
        if (viewModel.userRole.value == "CONDUCTOR") {
            assignedBusTv.visibility = View.VISIBLE
            assignedBusTv.text = "Authorized Bus: ${viewModel.assignedBusNumber.value}"
        }

        val reasons = arrayOf("Traffic", "Mechanical Issue", "Weather", "Other")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, reasons)
        reasonAutoComplete.setAdapter(adapter)
        
        reasonAutoComplete.setOnClickListener {
            reasonAutoComplete.showDropDown()
        }

        delayedSwitch.isChecked = slotData.delay == -1L
        if (slotData.delay > 0) delayEt.setText(slotData.delay.toString())
        
        if (slotData.reason.isNotEmpty() && reasons.contains(slotData.reason)) {
            reasonAutoComplete.setText(slotData.reason, false)
        }

        updateBtn.setOnClickListener {
            val reason = reasonAutoComplete.text.toString().ifEmpty { "Other" }
            if (delayedSwitch.isChecked) {
                viewModel.markBusAsDelayed(slotData.id, reason)
            } else {
                val newDelayValue = delayEt.text.toString().toLongOrNull() ?: 0L
                viewModel.updateSlotDelay(slotId = slotData.id, delayMinutes = newDelayValue, reason = reason)
            }
            dialog.dismiss()
        }

        cancelBtn.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_bus
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    true
                }
                R.id.navigation_bus -> true
                R.id.navigation_profile -> {
                    navigateToProfile()
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToProfile() {
        val currentRole = viewModel.userRole.value
        if (currentRole != null) redirectByRole(currentRole)
        else {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                FirebaseFirestore.getInstance().collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        val role = document.getString("role")?.trim() ?: "PASSENGER"
                        redirectByRole(role)
                    }
                    .addOnFailureListener {
                        val intent = Intent(this, PassengerProfileActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        startActivity(intent)
                    }
            } else {
                val intent = Intent(this, PassengerProfileActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
            }
        }
    }

    private fun redirectByRole(role: String) {
        val intent = if (role.equals("ADMIN", ignoreCase = true)) Intent(this, AdminDashboardActivity::class.java)
        else Intent(this, PassengerProfileActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }
}
