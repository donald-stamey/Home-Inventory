package com.example.homeinventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homeinventory.databinding.InvRowBinding
import com.example.homeinventory.ui.dashboard.DashboardFragment

class InvListAdapter(private val list: List<RoomDB.Floor>) :
    RecyclerView.Adapter<InvListAdapter.ViewHolder>() {
    inner class ViewHolder(val rowBinding: InvRowBinding) : RecyclerView.ViewHolder(rowBinding.root) {
        init {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowBinding = InvRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(rowBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.rowBinding
        binding.name.text = list[position].name
        binding.id.text = list[position].id.toString()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}