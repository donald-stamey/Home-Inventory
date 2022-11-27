package com.example.homeinventory.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.homeinventory.MainActivity
import com.example.homeinventory.ResultsAdapter
import com.example.homeinventory.RoomDB
import com.example.homeinventory.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var db: RoomDB.Inventory
    private lateinit var daoList: List<RoomDB.InvDao>
    private lateinit var itemDao: RoomDB.ItemDao
    private lateinit var spinnerList: List<Spinner>
    private val idList = mutableListOf(-1, -1, -1, -1)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.results.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.results.addItemDecoration(DividerItemDecoration(requireContext(), 1))
        spinnerList = listOf(binding.floorSpin, binding.roomSpin, binding.surfaceSpin, binding.containerSpin)
        db = Room.databaseBuilder(
            requireContext(), RoomDB.Inventory::class.java, "inventory")
            .allowMainThreadQueries().fallbackToDestructiveMigration().build()
        daoList = listOf(db.floorDao(), db.roomDao(), db.surfaceDao(), db.containerDao())
        itemDao = db.itemDao()
        listOnSpinner(daoList[0].getAll(), 0)
        binding.searchButton.setOnClickListener {
            search()
        }
    }

    private fun search() {
        val search = "%" + binding.search.text.toString() + "%"
        val items = if(idList[3] != -1) {
            itemDao.getItemsInContainer(idList[3], search)
        } else if(idList[2] != -1) {
            itemDao.getItemsOnSurface(idList[2], search)
        } else if(idList[1] != -1) {
            itemDao.getItemsInRoom(idList[1], search)
        } else if(idList[0] != -1) {
            itemDao.getItemsOnFloor(idList[0], search)
        } else {
            itemDao.getSearch(search)
        }
        binding.results.adapter = ResultsAdapter(items)
    }

    fun listOnSpinner(list: List<RoomDB.InvObject>, index: Int) {
        val spinner = spinnerList[index]
        //Floor represents not selected
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
            listOf<RoomDB.InvObject>(RoomDB.Floor(-1, "Select")) + list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                for(i in index + 1 until idList.size) {
                    spinnerList[i].adapter = null
                    idList[i] = -1
                }
                if(p2 == 0) {
                    idList[index] = -1
                } else {
                    idList[index] = list[p2 - 1].id
                    if(index < daoList.size - 1) {
                        listOnSpinner(daoList[index].downList(idList[index]), index + 1)
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}