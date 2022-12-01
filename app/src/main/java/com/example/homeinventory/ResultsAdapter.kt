package com.example.homeinventory

import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homeinventory.databinding.ResultsRowBinding

class ResultsAdapter(private val itemDao: RoomDB.ItemDao,
                     private val resultsClick: (item: RoomDB.Item, position: Int) -> Unit) :
    RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {
    private var list = mutableListOf<RoomDB.Item>()
    inner class ViewHolder(val rowBinding: ResultsRowBinding) : RecyclerView.ViewHolder(rowBinding.root)

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
        binding.container.text = "In " + itemDao.up(item).name
        binding.quantity.text = "Quantity: " + item.quantity.toString()
        if(item.image != null) {
            binding.image.setImageURI(Uri.parse(item.image))
        }
        binding.root.setOnClickListener {
            resultsClick(list[position], position)
        }
    }

    fun submitList(newList: List<RoomDB.Item>) {
        list = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun delete(index: Int): RoomDB.Item {
        val removedObject = list.removeAt(index)
        notifyItemRemoved(index)
        notifyItemRangeChanged(index, itemCount)
        return removedObject
    }

    fun dontDelete(index: Int) {
        notifyItemChanged(index)
    }

    fun updateName(newName: String, position: Int) {
        list[position] = list[position].copy(name = newName)
        notifyItemChanged(position)
    }

    fun updateQuantity(newQuantity: Int, position: Int) {
        list[position] = list[position].copy(quantity = newQuantity)
        notifyItemChanged(position)
    }

    fun updateImage(newImage: String, position: Int) {
        list[position] = list[position].copy(image = newImage)
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}