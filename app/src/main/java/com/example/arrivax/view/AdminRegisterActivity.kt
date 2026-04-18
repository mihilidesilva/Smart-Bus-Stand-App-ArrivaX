package com.example.arrivax.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arrivax.databinding.ActivityAdminRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class AdminRegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val ADMIN_SECRET_CODE = "ADMIN_SECRET_CODE_123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.loginLinkContainer.setOnClickListener { 
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.registerButton.setOnClickListener {
            val name = binding.nameInputLayout.editText?.text.toString()
            val email = binding.emailInputLayout.editText?.text.toString()
            val securityCode = binding.securityCodeInputLayout.editText?.text.toString()
            val pass = binding.passwordInputLayout.editText?.text.toString()
            val confirmPass = binding.confirmPasswordInputLayout.editText?.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && securityCode.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    if (securityCode == ADMIN_SECRET_CODE) {
                        firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
                                val userId = firebaseUser?.uid
                                if (userId != null) {
                                    val user = hashMapOf(
                                        "name" to name,
                                        "email" to email,
                                        "role" to "ADMIN"
                                    )

                                    firestore.collection("users").document(userId)
                                        .set(user)
                                        .addOnSuccessListener {
                                            firebaseUser.sendEmailVerification().addOnCompleteListener { verificationTask ->
                                                if (verificationTask.isSuccessful) {
                                                    Toast.makeText(this, "Admin registration successful! Please check your email for verification.", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                                                }
                                                firebaseAuth.signOut()
                                                val intent = Intent(this, LoginActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Failed to save user details: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            } else {
                                Toast.makeText(this, task.exception?.message ?: "Registration Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "Invalid Admin Security Code.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}