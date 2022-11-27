package com.example.homeinventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homeinventory.databinding.ResultsRowBinding

class ResultsAdapter(private val list: List<RoomDB.Item>) :
    RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {
    inner class ViewHolder(val rowBinding: ResultsRowBinding) : RecyclerView.ViewHolder(rowBinding.root) {
        init {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowBinding = ResultsRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(rowBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.rowBinding
        binding.name.text = list[position].name
        binding.container.text = list[position].container_id.toString()
        binding.quantity.text = "1"
    }

    override fun getItemCount(): Int {
        return list.size
    }
}