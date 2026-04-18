package com.example.arrivax.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arrivax.R
import com.example.arrivax.model.Schedule
import com.google.android.material.button.MaterialButton

class ScheduleAdapter(
    private var schedules: List<Schedule>,
    private val isAdmin: Boolean = false,
    private val onEditClick: (Schedule) -> Unit = {},
    private val onDeleteClick: (Schedule) -> Unit = {}
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val routeTv: TextView = view.findViewById(R.id.routeTv)
        val departureTimeTv: TextView = view.findViewById(R.id.departureTimeTv)
        val arrivalTimeTv: TextView = view.findViewById(R.id.arrivalTimeTv)
        val daysTv: TextView = view.findViewById(R.id.daysTv)
        val conductorTv: TextView = view.findViewById(R.id.conductorTv)
        val editBtn: MaterialButton = view.findViewById(R.id.editBtn)
        val deleteBtn: MaterialButton = view.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.routeTv.text = "🚌 ${schedule.route}"
        holder.departureTimeTv.text = schedule.departureTime
        holder.arrivalTimeTv.text = schedule.arrivalTime
        holder.daysTv.text = "📅 ${schedule.days}"
        holder.conductorTv.text = "👤 Bus: ${schedule.busNumber}"

        if (isAdmin) {
            holder.editBtn.visibility = View.VISIBLE
            holder.deleteBtn.visibility = View.VISIBLE
            
            holder.editBtn.setOnClickListener { onEditClick(schedule) }
            holder.deleteBtn.setOnClickListener { onDeleteClick(schedule) }
        } else {
            holder.editBtn.visibility = View.GONE
            holder.deleteBtn.visibility = View.GONE
        }
    }

    override fun getItemCount() = schedules.size

    fun updateData(newSchedules: List<Schedule>) {
        this.schedules = newSchedules
        notifyDataSetChanged()
    }
}
