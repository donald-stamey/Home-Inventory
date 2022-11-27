package com.example.homeinventory.ui.dashboard

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.homeinventory.InvListAdapter
import com.example.homeinventory.R
import com.example.homeinventory.RoomDB
import com.example.homeinventory.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var db: RoomDB.Inventory
    private lateinit var daoList: List<RoomDB.InvDao>
    private var daoIndex = 0
    private lateinit var curInvObject: RoomDB.InvObject
    private val adapter = InvListAdapter{down(it)}
    private lateinit var labelBinding: EditText

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
        setup()
        binding.back.setOnClickListener {
            up()
            labelBinding.isEnabled = false
            binding.edit.background = getDrawable(requireContext(), R.drawable.ic_edit_black_24dp)
        }
        binding.edit.setOnClickListener {
            if(labelBinding.isEnabled) {
                labelBinding.isEnabled = false
                curInvObject.name = labelBinding.text.toString()
                daoList[daoIndex - 1].changeName(curInvObject)
                it.background = getDrawable(requireContext(), R.drawable.ic_edit_black_24dp)
            } else {
                labelBinding.isEnabled = true
                it.background = getDrawable(requireContext(), R.drawable.ic_check_black_24dp)
            }
        }
    }

    private fun setup() {
        db = Room.databaseBuilder(
            requireContext(), RoomDB.Inventory::class.java, "inventory")
            .allowMainThreadQueries().fallbackToDestructiveMigration().build()
        daoList = listOf(db.floorDao(), db.roomDao(), db.surfaceDao(), db.containerDao(), db.itemDao())
        binding.rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rv.adapter = adapter
        adapter.submitList(daoList[daoIndex].getAll())
        labelBinding = binding.label
        labelBinding.setHintTextColor(Color.BLACK)
        labelBinding.isEnabled = false
        binding.back.visibility = View.INVISIBLE
        binding.edit.visibility = View.INVISIBLE
    }

    private fun down(invObject: RoomDB.InvObject) {
        if(daoIndex < daoList.size - 1) {
            binding.back.visibility = View.VISIBLE
            binding.edit.visibility = View.VISIBLE
            curInvObject = invObject
            labelBinding.setText(curInvObject.name)
            adapter.submitList(daoList[daoIndex].downList(curInvObject.id))
            daoIndex++
        } else {
            //open item page
        }
    }

    private fun up() {
        if(daoIndex > 1) {
            daoIndex--
            curInvObject = daoList[daoIndex].up(curInvObject)
            labelBinding.setText(curInvObject.name)
            adapter.submitList(daoList[daoIndex - 1].downList(curInvObject.id))
        } else {
            daoIndex = 0
            adapter.submitList(daoList[daoIndex].getAll())
            labelBinding.setText("Floors")
            binding.back.visibility = View.INVISIBLE
            binding.edit.visibility = View.INVISIBLE
        }
    }

    fun addTest() {
        //Thread(Runnable { db.clearAllTables() }).start()
        val floorDao = db.floorDao()
        floorDao.insertFloor(RoomDB.Floor(1,"floor1"))
        floorDao.insertFloor(RoomDB.Floor(2,"floor2"))
        val roomDao = db.roomDao()
        roomDao.insertRoom(RoomDB.Room(1, "room1", 1))
        roomDao.insertRoom(RoomDB.Room(2, "room2", 1))
        roomDao.insertRoom(RoomDB.Room(3, "room3", 2))
        roomDao.insertRoom(RoomDB.Room(4, "room4", 2))
        val surfaceDao = db.surfaceDao()
        surfaceDao.insertSurface(RoomDB.Surface(1, "surface1", 1, 1))
        surfaceDao.insertSurface(RoomDB.Surface(2, "surface2", 1, 1))
        surfaceDao.insertSurface(RoomDB.Surface(3, "surface3", 1, 2))
        surfaceDao.insertSurface(RoomDB.Surface(4, "surface4", 1, 2))
        surfaceDao.insertSurface(RoomDB.Surface(5, "surface5", 2, 3))
        surfaceDao.insertSurface(RoomDB.Surface(6, "surface6", 2, 3))
        surfaceDao.insertSurface(RoomDB.Surface(7, "surface7", 2, 4))
        surfaceDao.insertSurface(RoomDB.Surface(8, "surface8", 2, 4))
        val containerDao = db.containerDao()
        containerDao.insertContainer(RoomDB.Container(1, "container1", 1, 1, 1))
        containerDao.insertContainer(RoomDB.Container(2, "container2", 1, 1, 1))
        containerDao.insertContainer(RoomDB.Container(3, "container3", 1, 1, 2))
        containerDao.insertContainer(RoomDB.Container(4, "container4", 1, 1, 2))
        containerDao.insertContainer(RoomDB.Container(5, "container5", 1, 2, 3))
        containerDao.insertContainer(RoomDB.Container(6, "container6", 1, 2, 3))
        containerDao.insertContainer(RoomDB.Container(7, "container7", 1, 2, 4))
        containerDao.insertContainer(RoomDB.Container(8, "container8", 1, 2, 4))
        containerDao.insertContainer(RoomDB.Container(9, "container9", 2, 3, 5))
        containerDao.insertContainer(RoomDB.Container(10, "container10", 2, 3, 5))
        containerDao.insertContainer(RoomDB.Container(11, "container11", 2, 3, 6))
        containerDao.insertContainer(RoomDB.Container(12, "container12", 2, 3, 6))
        containerDao.insertContainer(RoomDB.Container(13, "container13", 2, 4, 7))
        containerDao.insertContainer(RoomDB.Container(14, "container14", 2, 4, 7))
        containerDao.insertContainer(RoomDB.Container(15, "container15", 2, 4, 8))
        containerDao.insertContainer(RoomDB.Container(16, "container16", 2, 4, 8))
        val itemDao = db.itemDao()
        itemDao.insertItem(RoomDB.Item(1, "item1", 1, 1, 1, 1, null, null))
        itemDao.insertItem(RoomDB.Item(2, "item2", 1, 1, 1, 1, null, null))
        itemDao.insertItem(RoomDB.Item(3, "item3", 1, 1, 1, 2, null, null))
        itemDao.insertItem(RoomDB.Item(4, "item4", 1, 1, 1, 2, null, null))
        itemDao.insertItem(RoomDB.Item(5, "item5", 1, 1, 2, 3, null, null))
        itemDao.insertItem(RoomDB.Item(6, "item6", 1, 1, 2, 3, null, null))
        itemDao.insertItem(RoomDB.Item(7, "item7", 1, 1, 2, 4, null, null))
        itemDao.insertItem(RoomDB.Item(8, "item8", 1, 1, 2, 4, null, null))
        itemDao.insertItem(RoomDB.Item(9, "item9", 1, 2, 3, 5, null, null))
        itemDao.insertItem(RoomDB.Item(10, "item10", 1, 2, 3, 5, null, null))
        itemDao.insertItem(RoomDB.Item(11, "item11", 1, 2, 3, 6, null, null))
        itemDao.insertItem(RoomDB.Item(12, "item12", 1, 2, 3, 6, null, null))
        itemDao.insertItem(RoomDB.Item(13, "item13", 1, 2, 4, 7, null, null))
        itemDao.insertItem(RoomDB.Item(14, "item14", 1, 2, 4, 7, null, null))
        itemDao.insertItem(RoomDB.Item(15, "item15", 1, 2, 4, 8, null, null))
        itemDao.insertItem(RoomDB.Item(16, "item16", 1, 2, 4, 8, null, null))
        itemDao.insertItem(RoomDB.Item(17, "item17", 2, 3, 5, 9, null, null))
        itemDao.insertItem(RoomDB.Item(18, "item18", 2, 3, 5, 9, null, null))
        itemDao.insertItem(RoomDB.Item(19, "item19", 2, 3, 5, 10, null, null))
        itemDao.insertItem(RoomDB.Item(20, "item20", 2, 3, 5, 10, null, null))
        itemDao.insertItem(RoomDB.Item(21, "item21", 2, 3, 6, 11, null, null))
        itemDao.insertItem(RoomDB.Item(22, "item22", 2, 3, 6, 11, null, null))
        itemDao.insertItem(RoomDB.Item(23, "item23", 2, 3, 6, 12, null, null))
        itemDao.insertItem(RoomDB.Item(24, "item24", 2, 3, 6, 12, null, null))
        itemDao.insertItem(RoomDB.Item(25, "item25", 2, 4, 7, 13, null, null))
        itemDao.insertItem(RoomDB.Item(26, "item26", 2, 4, 7, 13, null, null))
        itemDao.insertItem(RoomDB.Item(27, "item27", 2, 4, 7, 14, null, null))
        itemDao.insertItem(RoomDB.Item(28, "item28", 2, 4, 7, 14, null, null))
        itemDao.insertItem(RoomDB.Item(29, "item29", 2, 4, 8, 15, null, null))
        itemDao.insertItem(RoomDB.Item(30, "item30", 2, 4, 8, 15, null, null))
        itemDao.insertItem(RoomDB.Item(31, "item31", 2, 4, 8, 16, null, null))
        itemDao.insertItem(RoomDB.Item(32, "item32", 2, 4, 8, 16, null, null))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}