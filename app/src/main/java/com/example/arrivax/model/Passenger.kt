package com.example.arrivax.model

import com.google.firebase.firestore.PropertyName

/**
 * Data class representing a user profile (Passenger or Conductor) in the system.
 */
data class Passenger(
    @JvmField
    @PropertyName("name")
    val name: String = "",

    @JvmField
    @PropertyName("email")
    val email: String = "",

    @JvmField
    @PropertyName("mobileNumber")
    val mobileNumber: String = "",

    @JvmField
    @PropertyName("busNumber")
    val busNumber: String = "",

    @JvmField
    @PropertyName("role")
    val role: String = "PASSENGER",

    @JvmField
    @PropertyName("status")
    val status: String = "ACTIVE",

    @JvmField
    @PropertyName("delayAlerts")
    val delayAlerts: Boolean = true,

    @JvmField
    @PropertyName("slotAlerts")
    val slotAlerts: Boolean = true
)
