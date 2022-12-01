package com.example.homeinventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homeinventory.databinding.InvRowBinding

class InvListAdapter(private val invClick: (invObject: Inventory.InvObject, position: Int) -> Unit) :
    RecyclerView.Adapter<InvListAdapter.ViewHolder>() {
    private var list = mutableListOf<Inventory.InvObject>()
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
            invClick(list[position], position)
        }
    }

    fun submitList(newList: List<Inventory.InvObject>) {
        list = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun delete(index: Int): Inventory.InvObject {
        val removedObject = list.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, itemCount)
        return removedObject
    }

    fun dontDelete(index: Int) {
        notifyItemChanged(index)
    }

    fun add(invObject: Inventory.InvObject) {
        list.add(invObject)
        notifyItemInserted(itemCount)
    }

    fun updateName(newName: String, position: Int) {
        list[position] = (list[position] as Inventory.Item).copy(name = newName)
        notifyItemChanged(position)
    }

    fun updateQuantity(newQuantity: Int, position: Int) {
        list[position] = (list[position] as Inventory.Item).copy(quantity = newQuantity)
        notifyItemChanged(position)
    }

    fun updateImage(newImage: String, position: Int) {
        list[position] = (list[position] as Inventory.Item).copy(image = newImage)
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}