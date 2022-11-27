package com.example.homeinventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homeinventory.databinding.InvRowBinding

class InvListAdapter(private val invClick: (invObject: RoomDB.InvObject) -> Unit) :
    RecyclerView.Adapter<InvListAdapter.ViewHolder>() {
    private var list = mutableListOf<RoomDB.InvObject>()
    inner class ViewHolder(val rowBinding: InvRowBinding) : RecyclerView.ViewHolder(rowBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowBinding = InvRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(rowBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.rowBinding
        binding.name.text = list[position].name
        binding.root.setOnClickListener {
            invClick(list[position])
        }
    }

    fun submitList(newList: List<RoomDB.InvObject>) {
        list = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun delete(index: Int): RoomDB.InvObject {
        val removedObject = list.removeAt(index)
        notifyItemRemoved(index)
        return removedObject
    }

    override fun getItemCount(): Int {
        return list.size
    }
}