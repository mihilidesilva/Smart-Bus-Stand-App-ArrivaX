package com.example.arrivax.model

data class Schedule(
    val id: String = "",
    val route: String = "",
    val slotName: String = "",
    val departureTime: String = "",
    val arrivalTime: String = "",
    val busNumber: String = "",
    val days: String = "Daily"
)
