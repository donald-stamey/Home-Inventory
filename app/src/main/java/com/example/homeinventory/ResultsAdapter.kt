package com.example.homeinventory

import android.net.Uri
import android.provider.MediaStore
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
        val item = list[position]
        binding.name.text = item.name
        binding.container.text = item.container_id.toString()
        binding.quantity.text = "Quantity: " + item.quantity.toString()
        if(item.image != null) {
            binding.image.setImageURI(Uri.parse(item.image))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}