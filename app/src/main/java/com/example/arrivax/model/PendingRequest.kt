package com.example.arrivax.model

/**
 * A data class representing a single pending conductor registration request.
 *
 * @property userId The unique ID of the user from Firebase Authentication.
 * @property name The name of the applicant.
 * @property email The email of the applicant.
 * @property busNumber The bus number provided during registration.
 */
data class PendingRequest(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val busNumber: String = ""
)