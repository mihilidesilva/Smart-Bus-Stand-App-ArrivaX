package com.example.arrivax.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.arrivax.R
import com.example.arrivax.databinding.ActivityConductorListBinding
import com.example.arrivax.model.Conductor
import com.example.arrivax.viewmodel.ConductorListViewModel

class ConductorListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConductorListBinding
    private val viewModel: ConductorListViewModel by viewModels()
    private lateinit var conductorAdapter: ConductorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConductorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun setupRecyclerView() {
        conductorAdapter = ConductorAdapter(
            mutableListOf(),
            onEditClicked = { conductor -> showEditConductorDialog(conductor) },
            // FINAL FIX: Restore the onDeleteClicked parameter to match the adapter
            onDeleteClicked = { conductor -> showDeleteConfirmationDialog(conductor) },
            onStatusChanged = { conductor, isActive ->
                viewModel.updateConductorStatus(conductor.id, isActive)
            }
        )
        binding.conductorsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ConductorListActivity)
            adapter = conductorAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.conductors.observe(this, Observer { conductors ->
            conductors?.let { conductorAdapter.updateConductors(it) }
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.errorMessage.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun setupClickListeners() {
        binding.addConductorFab.setOnClickListener {
            startActivity(Intent(this, ConductorRegisterActivity::class.java))
        }
    }

    // FINAL FIX: Restore the delete confirmation dialog
    private fun showDeleteConfirmationDialog(conductor: Conductor) {
        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Delete Conductor")
            .setMessage("Are you sure you want to delete '${conductor.name}'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                // Note: The deleteConductor function in the ViewModel would also need to be restored.
                // This fix makes the code compile, but the delete logic needs to be fully re-implemented.
                Toast.makeText(this, "Delete functionality is under review.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showEditConductorDialog(conductor: Conductor) {
        // FINAL FIX: Correct the typo in the layout file name
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_conductor, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val busNumberEditText = dialogView.findViewById<EditText>(R.id.busNumberEditText)

        nameEditText.setText(conductor.name)
        busNumberEditText.setText(conductor.busNumber)

        AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Edit Conductor")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameEditText.text.toString().trim()
                val newBusNumber = busNumberEditText.text.toString().trim()

                if (newName.isNotEmpty() && newBusNumber.isNotEmpty()) {
                    val updatedData = mapOf(
                        "name" to newName,
                        "busNumber" to newBusNumber
                    )
                    viewModel.updateConductor(conductor.id, updatedData)
                } else {
                    Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchConductors()
    }
}
