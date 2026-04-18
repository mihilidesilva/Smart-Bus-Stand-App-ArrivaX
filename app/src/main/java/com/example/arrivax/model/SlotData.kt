package com.example.arrivax.model

data class SlotData(
    val id: String, // e.g., "slot1"
    val displayName: String, // e.g., "Slot A-01"
    var status: String = "FREE", // From RTDB (Hardware)
    var route: String = "Not Assigned", // From Firestore (App Logic)
    var delay: Long = 0, // From Firestore
    var reason: String = "N/A", // From Firestore
    var lastUpdated: String = "N/A", // From Firestore
    var expectedArrival: String = "N/A", // NEW: Expected arrival time
    var busNumber: String = "" // NEW: Track the specific bus number for permission checks
)