package com.example.homeinventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.homeinventory.databinding.InvRowBinding

class InvListAdapter(private val daoList: List<RoomDB.InvDao>) :
    RecyclerView.Adapter<InvListAdapter.ViewHolder>() {
    private var daoIndex = 0
    private var list = daoList[daoIndex].getAll()
    private var curInvObject = list[0]
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
                curInvObject = list[position]
                list = daoList[daoIndex].downList(curInvObject.id)
                daoIndex++
                notifyDataSetChanged()
            } else {
                //open item page
            }
        }
    }

    fun up() {
        if(daoIndex > 1) {
            daoIndex--
            curInvObject = daoList[daoIndex].up(curInvObject)
            list = daoList[daoIndex - 1].downList(curInvObject.id)
        } else {
            daoIndex = 0
            list = daoList[daoIndex].getAll()
            curInvObject = list[0]
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return list.size
    }
}