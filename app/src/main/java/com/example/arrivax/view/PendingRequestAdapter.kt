package com.example.arrivax.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.arrivax.databinding.ItemPendingRequestBinding
import com.example.arrivax.model.PendingRequest

class PendingRequestAdapter(
    private var requests: MutableList<PendingRequest>,
    private val onApproveClicked: (PendingRequest) -> Unit,
    private val onRejectClicked: (PendingRequest) -> Unit
) : RecyclerView.Adapter<PendingRequestAdapter.PendingRequestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingRequestViewHolder {
        val binding = ItemPendingRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingRequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PendingRequestViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request)
    }

    override fun getItemCount(): Int = requests.size

    fun updateRequests(newRequests: List<PendingRequest>) {
        requests.clear()
        requests.addAll(newRequests)
        notifyDataSetChanged()
    }

    inner class PendingRequestViewHolder(private val binding: ItemPendingRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: PendingRequest) {
            binding.nameTextView.text = request.name
            binding.emailTextView.text = request.email
            binding.busNumberTextView.text = "Bus Number: ${request.busNumber}"

            binding.approveButton.setOnClickListener {
                // Pass the request object back to the activity to handle the logic
                onApproveClicked(request)
            }

            binding.rejectButton.setOnClickListener {
                onRejectClicked(request)
            }
        }
    }
}