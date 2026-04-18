package com.example.arrivax.model

import com.google.firebase.firestore.Exclude

data class Conductor(
    @get:Exclude var id: String = "", // Document ID from Firestore
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val busNumber: String = "",
    val status: String = "ACTIVE", // Default status is ACTIVE
    val role: String = "" // Added to resolve the build error
)
