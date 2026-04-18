package com.example.arrivax.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arrivax.databinding.ItemConductorBinding
import com.example.arrivax.model.Conductor

class ConductorAdapter(
    private var conductors: MutableList<Conductor>,
    private val onEditClicked: (Conductor) -> Unit,
    private val onDeleteClicked: (Conductor) -> Unit,
    private val onStatusChanged: (conductor: Conductor, isActive: Boolean) -> Unit
) : RecyclerView.Adapter<ConductorAdapter.ConductorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConductorViewHolder {
        val binding = ItemConductorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ConductorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConductorViewHolder, position: Int) {
        val conductor = conductors[position]
        holder.bind(conductor)
    }

    override fun getItemCount(): Int = conductors.size

    fun updateConductors(newConductors: List<Conductor>) {
        conductors.clear()
        conductors.addAll(newConductors)
        notifyDataSetChanged()
    }

    inner class ConductorViewHolder(private val binding: ItemConductorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(conductor: Conductor) {
            // 1. Clear listener FIRST to avoid triggering it during binding
            binding.statusSwitch.setOnCheckedChangeListener(null)
            
            // 2. Set the data
            binding.conductorNameTextView.text = conductor.name
            binding.busNumberTextView.text = "Bus Number: ${conductor.busNumber}"

            val isActive = conductor.status.equals("ACTIVE", ignoreCase = true)
            binding.statusSwitch.isChecked = isActive
            
            // 3. Set new listener
            binding.statusSwitch.setOnCheckedChangeListener { _, isChecked ->
                onStatusChanged(conductor, isChecked)
            }

            // Click listeners for actions
            binding.editButton.setOnClickListener { onEditClicked(conductor) }
            binding.deleteButton.setOnClickListener { onDeleteClicked(conductor) }
        }
    }
}
