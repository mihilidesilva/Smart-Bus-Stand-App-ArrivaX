package com.example.arrivax.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.arrivax.R
import com.example.arrivax.databinding.FragmentConductorProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConductorProfileFragment : Fragment() {

    private var _binding: FragmentConductorProfileBinding? = null
    private val binding get() = _binding!!
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConductorProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        fetchConductorData()
        setupListeners()
    }

    private fun setupListeners() {
        // Dropdown Menu for Settings with Branded Dark Red Theme
        binding.settingsButton.setOnClickListener { view ->
            // Use ContextThemeWrapper to apply the custom dark_red popup theme
            val contextWrapper: Context = ContextThemeWrapper(requireContext(), R.style.PopupMenuTheme)
            val popup = PopupMenu(contextWrapper, view)
            popup.menuInflater.inflate(R.menu.conductor_profile_menu, popup.menu)
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_edit_profile -> {
                        openEditProfile()
                        true
                    }
                    R.id.menu_logout -> {
                        performLogout()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    private fun openEditProfile() {
        // Show Edit Profile as a Popup Dialog instead of a fragment transaction
        val editFragment = EditConductorProfileFragment()
        editFragment.show(childFragmentManager, "EditProfile")
    }

    private fun performLogout() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun fetchConductorData() {
        val userId = auth.currentUser?.uid ?: return
        
        firestore.collection("users").document(userId)
            .addSnapshotListener { document, error ->
                if (!isAdded) return@addSnapshotListener
                
                if (error != null) {
                    Toast.makeText(context, "Error updating profile data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "Conductor"
                    val email = document.getString("email") ?: auth.currentUser?.email ?: "N/A"
                    val busNumber = document.getString("busNumber") ?: "Not Assigned"
                    val cId = document.getString("conductorId") ?: "CND-${userId.take(6).uppercase()}"
                    val status = document.getString("status") ?: "ACTIVE"

                    binding.conductorNameTextView.text = name
                    binding.conductorEmailTextView.text = email.lowercase()
                    binding.busNumberValue.text = "Bus Number: $busNumber"
                    binding.conductorIdValue.text = "ID: $cId"
                    binding.statusTextView.text = status.uppercase()
                    
                    if (status.equals("ACTIVE", ignoreCase = true)) {
                        binding.statusTextView.setBackgroundResource(R.drawable.status_background_active)
                    } else {
                        binding.statusTextView.setBackgroundResource(R.drawable.status_background_inactive)
                    }
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
