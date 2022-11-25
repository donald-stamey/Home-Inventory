package com.example.homeinventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homeinventory.databinding.InvRowBinding

class InvListAdapter(private val daoList: List<RoomDB.InvDao>) :
    RecyclerView.Adapter<InvListAdapter.ViewHolder>() {
    private var daoIndex = 0
    private var list = daoList[daoIndex].getAll()
    private var id = -1
    inner class ViewHolder(val rowBinding: InvRowBinding) : RecyclerView.ViewHolder(rowBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowBinding = InvRowBinding.inflate(LayoutInflater.from(parent.context),
            parent, false)
        return ViewHolder(rowBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.rowBinding
        binding.name.text = list[position].name
        binding.id.text = list[position].id.toString()
        binding.root.setOnClickListener {
            if(daoIndex < daoList.size - 1) {
                daoIndex++
                list = daoList[daoIndex].getAll()
                id = list[position].id
                notifyDataSetChanged()
            }
        }
    }

    fun up() {
        if(daoIndex > 0) {
            id = daoList[daoIndex].up(id).id
            daoIndex--
            list = daoList[daoIndex].downList(id)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}