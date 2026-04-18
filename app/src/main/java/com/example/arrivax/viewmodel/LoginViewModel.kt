package com.example.arrivax.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Sealed class to represent the different states of the login process
sealed class LoginState {
    object Loading : LoginState()
    data class Success(val role: String) : LoginState()
    // ApprovalPending state is removed as it's no longer part of the workflow
    data class Error(val message: String) : LoginState()
    object EmailNotVerified : LoginState()
}

class LoginViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun loginUser(email: String, pass: String) {
        _loginState.value = LoginState.Loading

        firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    if (firebaseUser.isEmailVerified) {
                        // Email is verified, now check the role in Firestore
                        checkUserRole(firebaseUser.uid)
                    } else {
                        // Email is not verified
                        firebaseAuth.signOut() // Sign out to prevent access
                        _loginState.value = LoginState.EmailNotVerified
                    }
                } else {
                    _loginState.value = LoginState.Error("User not found after login.")
                }
            } else {
                _loginState.value = LoginState.Error(task.exception?.message ?: "Login Failed")
            }
        }
    }

    private fun checkUserRole(userId: String) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val role = document.getString("role")
                    if (role != null) {
                        // Login is successful for any user with a valid role.
                        _loginState.value = LoginState.Success(role)
                    } else {
                        // This case handles if the 'role' field is missing for some reason.
                        firebaseAuth.signOut()
                        _loginState.value = LoginState.Error("User role not found.")
                    }
                } else {
                    firebaseAuth.signOut()
                    _loginState.value = LoginState.Error("User data not found in database.")
                }
            }
            .addOnFailureListener { exception ->
                firebaseAuth.signOut()
                _loginState.value = LoginState.Error("Error fetching user data: ${exception.message}")
            }
    }
}