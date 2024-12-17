package com.example.shoplist

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox

class GroceryAdapter(
    private val items: MutableList<MainViewModel.GroceryItem>,
    private val onEditClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<GroceryAdapter.ViewHolder>() {

    private var filteredItems: MutableList<MainViewModel.GroceryItem> = items.toMutableList()
    private val handler = Handler(Looper.getMainLooper())

    fun filter(query: String) {
        filteredItems = if (query.isEmpty()) {
            items.toMutableList()
        } else {
            items.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_grocery, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredItems[position]
        holder.name.text = item.name
        holder.quantity.text = item.quantity.toString()
        holder.type.text = item.type
        holder.checkBox.isChecked = item.purchased

        holder.checkBox.setOnCheckedChangeListener(null)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val adapterPosition = holder.adapterPosition
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    holder.checkBox.isEnabled = false
                    handler.postDelayed({
                        onDeleteClick(adapterPosition)
                    }, 175)
                }
            } else {
                item.purchased = false
            }
        }

        holder.itemView.setOnClickListener {
            val adapterPosition = holder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION) {
                onEditClick(adapterPosition)
            }
        }
    }

    override fun getItemCount() = filteredItems.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: MaterialCheckBox = itemView.findViewById(R.id.checkbox_purchased)
        val name: TextView = itemView.findViewById(R.id.tv_item_name)
        val quantity: TextView = itemView.findViewById(R.id.tv_item_quantity)
        val type: TextView = itemView.findViewById(R.id.tv_item_type)
    }
}
