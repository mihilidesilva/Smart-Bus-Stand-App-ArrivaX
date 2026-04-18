package com.example.arrivax

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.arrivax.databinding.ItemHomeContentBinding

data class HomeItem(val title: String, val description: String, val type: String = "TEXT")

class HomeContentAdapter(private var items: List<HomeItem>) : RecyclerView.Adapter<HomeContentAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemHomeContentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.titleTextView.text = item.title
        holder.binding.descriptionTextView.text = item.description

        when (item.type) {
            "WHATSAPP" -> {
                holder.binding.endIcon.visibility = View.VISIBLE
                holder.binding.endIcon.setImageResource(R.drawable.ic_profile) // Fallback as ic_whatsapp is missing
            }
            "PHONE" -> {
                holder.binding.endIcon.visibility = View.VISIBLE
                holder.binding.endIcon.setImageResource(R.drawable.ic_profile) // Fallback as ic_phone is missing
            }
            else -> {
                holder.binding.endIcon.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            when (item.type) {
                "PHONE" -> {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:${item.description.filter { it.isDigit() || it == '+' }}")
                    context.startActivity(intent)
                }
                "WHATSAPP" -> {
                    val number = item.description.filter { it.isDigit() || it == '+' }
                    val url = "https://api.whatsapp.com/send?phone=$number"
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<HomeItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}