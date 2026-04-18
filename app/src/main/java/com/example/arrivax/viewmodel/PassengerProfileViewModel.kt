package com.example.arrivax.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.arrivax.model.Passenger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PassengerProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _passenger = MutableLiveData<Passenger?>()
    val passenger: LiveData<Passenger?> = _passenger

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadPassengerProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).addSnapshotListener { document, exception ->
                if (exception != null) {
                    _error.value = "Failed to load profile: ${exception.message}"
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val passenger = document.toObject(Passenger::class.java)
                    _passenger.value = passenger
                } else {
                    _error.value = "User profile not found."
                }
            }
        } else {
            _error.value = "No authenticated user found."
        }
    }

    fun updateNotificationPreferences(type: String, enabled: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val field = if (type == "delay") "delayAlerts" else "slotAlerts"
        
        firestore.collection("users").document(userId).update(field, enabled)
            .addOnFailureListener {
                _error.value = "Failed to update preferences"
            }
    }
}