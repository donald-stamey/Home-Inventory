package com.example.homeinventory.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.homeinventory.InvListAdapter
import com.example.homeinventory.RoomDB
import com.example.homeinventory.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val db = Room.databaseBuilder(
            requireContext(), RoomDB.Inventory::class.java, "inventory")
            .allowMainThreadQueries().build()
        val floorDao = db.floorDao()
        Thread(Runnable {
            db.clearAllTables()
        })
        //.insertFloor(RoomDB.Floor(0,"test1"))
        //floorDao.insertFloor(RoomDB.Floor(0,"test2"))
        //floorDao.insertFloor(RoomDB.Floor(0,"test3"))
        //floorDao.insertFloor(RoomDB.Floor(0,"test4"))
        //binding.rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        //val adapter = InvListAdapter(floorDao.getFloors())
        //binding.rv.adapter = adapter
        //binding.label.text = adapter.itemCount.toString()
    }

    fun Test(){}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}