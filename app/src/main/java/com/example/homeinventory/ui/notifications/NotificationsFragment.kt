package com.example.homeinventory.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Database
import androidx.room.Room
import com.example.homeinventory.InvListAdapter
import com.example.homeinventory.ResultsAdapter
import com.example.homeinventory.RoomDB
import com.example.homeinventory.databinding.FragmentNotificationsBinding
import kotlin.math.floor

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var db: RoomDB.Inventory
    private var container: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        db = Room.databaseBuilder(
            requireContext(), RoomDB.Inventory::class.java, "inventory")
            .allowMainThreadQueries().build()
        listFloors()
        binding.searchButton.setOnClickListener {
            val itemDao = db.itemDao()
            val items = itemDao.getItemsInContainer(container, binding.search.text.toString())
            binding.results.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val adapter = ResultsAdapter(items)
            binding.results.adapter = adapter
        }
    }

    fun listFloors() {
        val floorDao = db.floorDao()
        val floors = floorDao.getFloors()
        val floorSpinAdapt = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, floors)
        floorSpinAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.floorSpin.adapter = floorSpinAdapt
        val floorSpinSelect = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                listRooms(floors[p2].id)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        binding.floorSpin.onItemSelectedListener = floorSpinSelect
    }

    fun listRooms(floorId: Int) {
        val roomDao = db.roomDao()
        val rooms = roomDao.getRoomsOnFloor(floorId)
        val roomSpinAdapt = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, rooms)
        roomSpinAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.roomSpin.adapter = roomSpinAdapt
        val roomSpinSelect = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                listSurfaces(rooms[p2].id)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        binding.roomSpin.onItemSelectedListener = roomSpinSelect
        binding.containerSpin.adapter = null
        binding.surfaceSpin.adapter = null
    }

    fun listSurfaces(roomId: Int) {
        val surfaceDao = db.surfaceDao()
        val surfaces = surfaceDao.getSurfacesInRoom(roomId)
        val surfaceSpinAdapt = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, surfaces)
        surfaceSpinAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.surfaceSpin.adapter = surfaceSpinAdapt
        val surfaceSpinSelect = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                listContainers(surfaces[p2].id)
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        binding.surfaceSpin.onItemSelectedListener = surfaceSpinSelect
        binding.containerSpin.adapter = null
    }

    fun listContainers(surfaceId: Int) {
        val containerDao = db.containerDao()
        val containers = containerDao.getContainersOnSurface(surfaceId)
        val containerSpinAdapt = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, containers)
        containerSpinAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.containerSpin.adapter = containerSpinAdapt
        val containerSpinSelect = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                container = containers[p2].id
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
        binding.containerSpin.onItemSelectedListener = containerSpinSelect
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}