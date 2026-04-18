package com.example.arrivax.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.arrivax.model.Schedule
import com.example.arrivax.model.SlotData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*

class BusSlotsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val rtdb = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private val _slot1 = MutableLiveData<SlotData>()
    val slot1: LiveData<SlotData> = _slot1

    private val _slot2 = MutableLiveData<SlotData>()
    val slot2: LiveData<SlotData> = _slot2

    private val _slot3 = MutableLiveData<SlotData>()
    val slot3: LiveData<SlotData> = _slot3

    private val _slot4 = MutableLiveData<SlotData>()
    val slot4: LiveData<SlotData> = _slot4

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    private val _userRole = MutableLiveData<String>()
    val userRole: LiveData<String> = _userRole
    
    private val _assignedBusNumber = MutableLiveData<String?>()
    val assignedBusNumber: LiveData<String?> = _assignedBusNumber

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage

    private var rtdbListener: ValueEventListener? = null
    private var firestoreListener: ListenerRegistration? = null

    init {
        fetchUserData()
        listenToSchedulesInRealtime()
        listenToSlotsInRealtime()
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    _userName.value = document.getString("name") ?: "User"
                    val role = document.getString("role")?.trim()?.uppercase() ?: "PASSENGER"
                    _userRole.value = role
                    if (role == "CONDUCTOR") {
                        _assignedBusNumber.value = document.getString("busNumber")
                    }
                }
            }
    }

    private fun listenToSchedulesInRealtime() {
        firestoreListener = firestore.collection("schedules")
            .addSnapshotListener { _, e ->
                if (e != null) return@addSnapshotListener
                syncAllSlotsWithCurrentTime()
            }
    }

    private fun syncAllSlotsWithCurrentTime() {
        findNextScheduledBus("slot1", "Slot A-01")
        findNextScheduledBus("slot2", "Slot A-02")
        findNextScheduledBus("slot3", "Slot A-03")
        findNextScheduledBus("slot4", "Slot A-04")
    }

    private fun listenToSlotsInRealtime() {
        // Listening to the root to catch both status and info changes simultaneously
        rtdbListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val slotsNode = snapshot.child("slots")
                val infoNode = snapshot.child("slots_info")

                updateAndSwitchIfNeeded(slotsNode.child("slot1"), infoNode.child("slot1"), "slot1", "Slot A-01", _slot1)
                updateAndSwitchIfNeeded(slotsNode.child("slot2"), infoNode.child("slot2"), "slot2", "Slot A-02", _slot2)
                updateAndSwitchIfNeeded(slotsNode.child("slot3"), infoNode.child("slot3"), "slot3", "Slot A-03", _slot3)
                updateAndSwitchIfNeeded(slotsNode.child("slot4"), infoNode.child("slot4"), "slot4", "Slot A-04", _slot4)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("RTDB", "Listener cancelled: ${error.message}")
            }
        }
        rtdb.addValueEventListener(rtdbListener!!)
    }

    private fun updateAndSwitchIfNeeded(statusSnap: DataSnapshot, infoSnap: DataSnapshot, id: String, defaultName: String, liveData: MutableLiveData<SlotData>) {
        val newStatus = statusSnap.getValue(String::class.java) ?: "FREE"
        val oldStatus = liveData.value?.status
        val slotData = parseSlot(statusSnap, infoSnap, id, defaultName)
        
        // Auto-detect late arrival (Only if no manual delay is already set)
        if (newStatus == "FREE" && slotData.delay == 0L) {
            val scheduledTimeStr = infoSnap.child("scheduled_arrival").getValue(String::class.java)
            if (scheduledTimeStr != null && scheduledTimeStr != "N/A" && scheduledTimeStr != "Finding Next Bus...") {
                try {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                    val scheduledDate = sdf.parse(scheduledTimeStr.trim().uppercase())
                    val now = Calendar.getInstance()
                    val currentTime = sdf.parse(sdf.format(now.time))
                    if (scheduledDate != null && currentTime != null && currentTime.after(scheduledDate)) {
                        rtdb.child("slots_info").child(id).updateChildren(hashMapOf<String, Any>(
                            "delay" to -1L,
                            "reason" to "Late Arrival"
                        ))
                    }
                } catch (e: Exception) {
                    Log.e("AutoDelay", "Parse error for $scheduledTimeStr: ${e.message}")
                }
            }
        }
        
        liveData.value = slotData

        // Logic for physical hardware state changes (FREE <-> OCCUPIED)
        if (oldStatus != null && newStatus != oldStatus) {
            if (newStatus == "FREE") {
                val finishedTime = infoSnap.child("scheduled_arrival").getValue(String::class.java) ?: "N/A"
                rtdb.child("slots_info").child(id).updateChildren(hashMapOf<String, Any>(
                    "last_departure_time" to finishedTime,
                    "route" to "Finding Next Bus...",
                    "bus_number" to "N/A",
                    "delay" to 0L,
                    "reason" to "On Schedule",
                    "expected_arrival" to "N/A",
                    "scheduled_arrival" to "N/A"
                )).addOnSuccessListener {
                    findNextScheduledBus(id, defaultName, onlyFuture = true)
                }
            } else if (newStatus == "OCCUPIED") {
                val currentTime = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
                rtdb.child("slots_info").child(id).updateChildren(hashMapOf<String, Any>(
                    "last_updated" to currentTime,
                    "delay" to 0L,
                    "reason" to "Arrived"
                ))
            }
        } else if (oldStatus == null) {
            validateCurrentAssignment(id, slotData, infoSnap)
        }
    }

    private fun validateCurrentAssignment(id: String, slotData: SlotData, infoSnap: DataSnapshot) {
        val sched = infoSnap.child("scheduled_arrival").getValue(String::class.java) ?: "N/A"
        if (sched == "N/A" || sched == "Finding Next Bus...") {
            findNextScheduledBus(id, slotData.displayName)
            return
        }
        
        try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.US)
            val scheduledDate = sdf.parse(sched.trim().uppercase())
            val now = Calendar.getInstance()
            val currentTime = sdf.parse(sdf.format(now.time))
            
            if (scheduledDate != null && currentTime != null) {
                val diffMins = (currentTime.time - scheduledDate.time) / 60000
                if (diffMins > 120 && slotData.status == "FREE" && slotData.delay <= 0) {
                    findNextScheduledBus(id, slotData.displayName)
                }
            }
        } catch (e: Exception) {}
    }

    private fun findNextScheduledBus(slotId: String, slotName: String, onlyFuture: Boolean = false) {
        rtdb.child("slots").child(slotId).get().addOnSuccessListener { statusSnap ->
            val status = statusSnap.getValue(String::class.java) ?: "FREE"
            rtdb.child("slots_info").child(slotId).get().addOnSuccessListener { infoSnap ->
                val lastDeparture = infoSnap.child("last_departure_time").getValue(String::class.java) ?: "N/A"

                if (status == "OCCUPIED" && !onlyFuture) return@addOnSuccessListener

                firestore.collection("schedules").get().addOnSuccessListener { result ->
                    val now = Calendar.getInstance()
                    val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                    var bestMatchBus: Schedule? = null
                    var earliestTime = Long.MAX_VALUE
                    val threshold = if (onlyFuture) 5000L else -14400000L 

                    for (doc in result) {
                        val schedule = doc.toObject(Schedule::class.java)
                        val matchesSlot = schedule.route.contains(slotName, ignoreCase = true) || 
                                          schedule.slotName.contains(slotName, ignoreCase = true) ||
                                          schedule.route.contains(slotId, ignoreCase = true) ||
                                          schedule.slotName.contains(slotId, ignoreCase = true)

                        if (matchesSlot) {
                            if (lastDeparture != "N/A" && schedule.arrivalTime.trim().uppercase() == lastDeparture.trim().uppercase()) continue

                            try {
                                val arrivalTime = sdf.parse(schedule.arrivalTime.trim().uppercase()) ?: continue
                                val arrivalCal = Calendar.getInstance().apply {
                                    time = arrivalTime
                                    set(Calendar.YEAR, now.get(Calendar.YEAR))
                                    set(Calendar.MONTH, now.get(Calendar.MONTH))
                                    set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
                                }
                                val schedMillis = arrivalCal.timeInMillis
                                val diff = schedMillis - now.timeInMillis
                                
                                if (lastDeparture != "N/A") {
                                    val lastDepDate = sdf.parse(lastDeparture.trim().uppercase())
                                    if (lastDepDate != null && arrivalTime.before(lastDepDate)) continue
                                }

                                if (diff > threshold && schedMillis < earliestTime) {
                                    earliestTime = schedMillis
                                    bestMatchBus = schedule
                                }
                            } catch (e: Exception) { continue }
                        }
                    }

                    bestMatchBus?.let { bus ->
                        val currentBus = infoSnap.child("bus_number").getValue(String::class.java) ?: ""
                        if (currentBus != bus.busNumber) {
                            rtdb.child("slots_info").child(slotId).updateChildren(hashMapOf<String, Any>(
                                "route" to bus.route,
                                "bus_number" to bus.busNumber,
                                "expected_arrival" to bus.arrivalTime,
                                "scheduled_arrival" to bus.arrivalTime,
                                "delay" to 0L,
                                "reason" to "On Schedule"
                            ))
                        }
                    } ?: run {
                        if (status == "FREE") {
                            rtdb.child("slots_info").child(slotId).updateChildren(hashMapOf<String, Any>(
                                "route" to "No More Buses Today",
                                "bus_number" to "N/A",
                                "expected_arrival" to "N/A",
                                "scheduled_arrival" to "N/A"
                            ))
                        }
                    }
                }
            }
        }
    }

    private fun parseSlot(statusSnap: DataSnapshot, infoSnap: DataSnapshot, id: String, defaultName: String): SlotData {
        val status = statusSnap.getValue(String::class.java) ?: "FREE"
        val route = infoSnap.child("route").getValue(String::class.java) ?: "Not Assigned"
        
        val delayValue = infoSnap.child("delay").value
        val delay = when (delayValue) {
            is Long -> delayValue
            is Int -> delayValue.toLong()
            is Double -> delayValue.toLong()
            else -> 0L
        }
        
        val lastUpdated = infoSnap.child("last_updated").getValue(String::class.java) ?: "N/A"
        val expectedArrival = infoSnap.child("expected_arrival").getValue(String::class.java) ?: "N/A"
        val busNumber = infoSnap.child("bus_number").getValue(String::class.java) ?: ""
        val reason = infoSnap.child("reason").getValue(String::class.java) ?: "On Time"
        
        return SlotData(id, defaultName, status, route, delay, reason, lastUpdated, expectedArrival, busNumber)
    }

    fun updateSlotDelay(slotId: String, delayMinutes: Long, reason: String) {
        rtdb.child("slots_info").child(slotId).get().addOnSuccessListener { snapshot ->
            val scheduledArrival = snapshot.child("scheduled_arrival").getValue(String::class.java)
            
            Log.d("DelayUpdate", "Updating $slotId. Base Time: $scheduledArrival, Minutes: $delayMinutes")

            if (scheduledArrival == null || scheduledArrival == "N/A" || scheduledArrival == "Finding Next Bus...") {
                _toastMessage.value = "Error: No bus currently assigned to this slot."
                return@addOnSuccessListener
            }

            try {
                // FIXED: Normalizing string (trim, upper) and forcing Locale.US for AM/PM consistency
                val cleanTime = scheduledArrival.trim().uppercase().replace(".", "") // Handles AM vs A.M.
                val sdf = SimpleDateFormat("hh:mm a", Locale.US)
                val baseDate = sdf.parse(cleanTime)
                
                if (baseDate == null) {
                    Log.e("DelayUpdate", "Parse failed for: $cleanTime")
                    _toastMessage.value = "Error: Invalid time format in database ($cleanTime)."
                    return@addOnSuccessListener
                }
                
                val calendar = Calendar.getInstance().apply {
                    time = baseDate
                    add(Calendar.MINUTE, delayMinutes.toInt())
                }
                
                val newExpectedArrival = sdf.format(calendar.time)
                
                val updates = hashMapOf<String, Any>(
                    "delay" to delayMinutes,
                    "reason" to reason,
                    "expected_arrival" to newExpectedArrival
                )
                
                rtdb.child("slots_info").child(slotId).updateChildren(updates).addOnSuccessListener {
                    _toastMessage.value = "Update Successful: Expected at $newExpectedArrival"
                    Log.d("DelayUpdate", "Successfully pushed $newExpectedArrival to Firebase")
                }.addOnFailureListener {
                    _toastMessage.value = "Failed to sync with cloud."
                    Log.e("DelayUpdate", "Database push error: ${it.message}")
                }
            } catch (e: Exception) {
                Log.e("DelayUpdate", "Math Error: ${e.message}")
                _toastMessage.value = "Calculation error. Please try again."
            }
        }
    }

    fun markBusAsDelayed(slotId: String, reason: String) {
        rtdb.child("slots_info").child(slotId).get().addOnSuccessListener { snapshot ->
            val scheduledArrival = snapshot.child("scheduled_arrival").getValue(String::class.java) ?: "N/A"
            rtdb.child("slots_info").child(slotId).updateChildren(hashMapOf<String, Any>(
                "delay" to -1L, 
                "reason" to reason, 
                "expected_arrival" to scheduledArrival
            ))
        }
    }

    override fun onCleared() {
        super.onCleared()
        rtdbListener?.let { rtdb.removeEventListener(it) }
        firestoreListener?.remove()
    }
}
