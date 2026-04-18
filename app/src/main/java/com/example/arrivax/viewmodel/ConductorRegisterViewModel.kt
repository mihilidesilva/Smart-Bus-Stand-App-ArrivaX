package com.example.arrivax.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseOptions
import java.util.UUID

// Sealed class to represent the different states of the registration process
sealed class RegistrationState {
    object Loading : RegistrationState()
    data class Success(val message: String) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

// Use AndroidViewModel to get the application context
class ConductorRegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val mainAuth = FirebaseAuth.getInstance() // The admin's auth instance
    private val firestore = FirebaseFirestore.getInstance()

    private val _registrationState = MutableLiveData<RegistrationState>()
    val registrationState: LiveData<RegistrationState> = _registrationState

    fun registerConductor(name: String, email: String, busNumber: String, pass: String) {
        _registrationState.value = RegistrationState.Loading

        if (mainAuth.currentUser == null) {
            _registrationState.value = RegistrationState.Error("Operation failed: Administrator not logged in.")
            return
        }

        val firebaseOptions = FirebaseApp.getInstance().options
        val tempAppName = "tempAuth_" + UUID.randomUUID().toString()
        val tempApp = FirebaseApp.initializeApp(getApplication(), firebaseOptions, tempAppName)
        val tempAuth = FirebaseAuth.getInstance(tempApp)

        tempAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newUser = task.result?.user
                if (newUser != null) {
                    // FINAL FIX: Send the verification email
                    newUser.sendEmailVerification().addOnCompleteListener { emailTask ->
                        if (emailTask.isSuccessful) {
                            val conductorData = hashMapOf(
                                "name" to name,
                                "email" to email,
                                "busNumber" to busNumber,
                                "status" to "ACTIVE",
                                "role" to "CONDUCTOR"
                            )

                            firestore.collection("users").document(newUser.uid)
                                .set(conductorData)
                                .addOnSuccessListener {
                                    // FINAL FIX: Update the success message to be more user-friendly
                                    _registrationState.postValue(RegistrationState.Success("Conductor registered. A verification email has been sent."))
                                    tempApp.delete()
                                }
                                .addOnFailureListener { e ->
                                    _registrationState.postValue(RegistrationState.Error("Failed to save conductor details: ${e.message}"))
                                    tempApp.delete()
                                }
                        } else {
                            _registrationState.postValue(RegistrationState.Error("Failed to send verification email: ${emailTask.exception?.message}"))
                            tempApp.delete()
                        }
                    }
                } else {
                    _registrationState.postValue(RegistrationState.Error("Could not retrieve new user ID."))
                    tempApp.delete()
                }
            } else {
                _registrationState.postValue(RegistrationState.Error(task.exception?.message ?: "Registration failed."))
                tempApp.delete()
            }
        }
    }
}
