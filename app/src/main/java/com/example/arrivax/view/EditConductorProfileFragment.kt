package com.example.arrivax.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.arrivax.databinding.FragmentEditConductorProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.firestore.FirebaseFirestore

class EditConductorProfileFragment : DialogFragment() {

    private var _binding: FragmentEditConductorProfileBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        _binding = FragmentEditConductorProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCurrentData()
        setupListeners()
    }

    private fun loadCurrentData() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    binding.editNameValue.setText(document.getString("name") ?: "")
                    binding.editEmailValue.setText(document.getString("email") ?: auth.currentUser?.email)
                }
            }
    }

    private fun setupListeners() {
        binding.saveProfileButton.setOnClickListener {
            saveChanges()
        }

        binding.cancelEdit.setOnClickListener {
            dismiss()
        }

        binding.editProfileImageView.setOnClickListener {
            Toast.makeText(requireContext(), "Image upload coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveChanges() {
        val userId = auth.currentUser?.uid ?: return
        val newName = binding.editNameValue.text.toString().trim()
        val newEmail = binding.editEmailValue.text.toString().trim()
        val newPassword = binding.editPasswordValue.text.toString().trim()

        if (newName.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Name and Email cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Update Firestore first
        val updates = mutableMapOf<String, Any>(
            "name" to newName,
            "email" to newEmail
        )

        binding.saveProfileButton.isEnabled = false
        binding.saveProfileButton.text = "Updating..."

        firestore.collection("users").document(userId).update(updates)
            .addOnSuccessListener {
                handleAuthUpdates(newEmail, newPassword)
            }
            .addOnFailureListener {
                binding.saveProfileButton.isEnabled = true
                binding.saveProfileButton.text = "Save Changes"
                Toast.makeText(requireContext(), "Failed to update profile info.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleAuthUpdates(newEmail: String, newPassword: String) {
        val user = auth.currentUser ?: return
        val emailChanged = newEmail != user.email
        val passwordChanged = newPassword.isNotEmpty()

        if (emailChanged) {
            user.updateEmail(newEmail).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (passwordChanged) {
                        updatePasswordOnly(newPassword)
                    } else {
                        finishSuccess("Profile Updated Successfully!")
                    }
                } else {
                    handleAuthError(task.exception)
                }
            }
        } else if (passwordChanged) {
            updatePasswordOnly(newPassword)
        } else {
            finishSuccess("Profile Updated Successfully!")
        }
    }

    private fun updatePasswordOnly(newPassword: String) {
        auth.currentUser?.updatePassword(newPassword)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                finishSuccess("Profile & Password Updated!")
            } else {
                handleAuthError(task.exception)
            }
        }
    }

    private fun handleAuthError(exception: Exception?) {
        binding.saveProfileButton.isEnabled = true
        binding.saveProfileButton.text = "Save Changes"
        
        if (exception is FirebaseAuthRecentLoginRequiredException) {
            Toast.makeText(requireContext(), "Security: Please log out and log back in to change password/email.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Error: ${exception?.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun finishSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}