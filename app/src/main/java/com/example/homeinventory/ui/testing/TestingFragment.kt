package com.example.homeinventory.ui.testing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.example.homeinventory.Inventory
import com.example.homeinventory.databinding.FragmentTestingBinding

class TestingFragment : Fragment() {

    private var _binding: FragmentTestingBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.add.setOnClickListener {
            addTest()
        }
    }

    fun addTest() {
        val db = Room.databaseBuilder(
            requireContext(), Inventory.InventoryDatabase::class.java, "inventory")
            .allowMainThreadQueries().fallbackToDestructiveMigration().build()
        db.clearAllTables()
        val floorDao = db.floorDao()
        floorDao.insertFloor(Inventory.Floor(1,"floor1"))
        floorDao.insertFloor(Inventory.Floor(2,"floor2"))
        val roomDao = db.roomDao()
        roomDao.insertRoom(Inventory.Room(1, "room1", 1))
        roomDao.insertRoom(Inventory.Room(2, "room2", 1))
        roomDao.insertRoom(Inventory.Room(3, "room3", 2))
        roomDao.insertRoom(Inventory.Room(4, "room4", 2))
        val surfaceDao = db.surfaceDao()
        surfaceDao.insertSurface(Inventory.Surface(1, "surface1", 1, 1))
        surfaceDao.insertSurface(Inventory.Surface(2, "surface2", 1, 1))
        surfaceDao.insertSurface(Inventory.Surface(3, "surface3", 1, 2))
        surfaceDao.insertSurface(Inventory.Surface(4, "surface4", 1, 2))
        surfaceDao.insertSurface(Inventory.Surface(5, "surface5", 2, 3))
        surfaceDao.insertSurface(Inventory.Surface(6, "surface6", 2, 3))
        surfaceDao.insertSurface(Inventory.Surface(7, "surface7", 2, 4))
        surfaceDao.insertSurface(Inventory.Surface(8, "surface8", 2, 4))
        val containerDao = db.containerDao()
        containerDao.insertContainer(Inventory.Container(1, "container1", 1, 1, 1))
        containerDao.insertContainer(Inventory.Container(2, "container2", 1, 1, 1))
        containerDao.insertContainer(Inventory.Container(3, "container3", 1, 1, 2))
        containerDao.insertContainer(Inventory.Container(4, "container4", 1, 1, 2))
        containerDao.insertContainer(Inventory.Container(5, "container5", 1, 2, 3))
        containerDao.insertContainer(Inventory.Container(6, "container6", 1, 2, 3))
        containerDao.insertContainer(Inventory.Container(7, "container7", 1, 2, 4))
        containerDao.insertContainer(Inventory.Container(8, "container8", 1, 2, 4))
        containerDao.insertContainer(Inventory.Container(9, "container9", 2, 3, 5))
        containerDao.insertContainer(Inventory.Container(10, "container10", 2, 3, 5))
        containerDao.insertContainer(Inventory.Container(11, "container11", 2, 3, 6))
        containerDao.insertContainer(Inventory.Container(12, "container12", 2, 3, 6))
        containerDao.insertContainer(Inventory.Container(13, "container13", 2, 4, 7))
        containerDao.insertContainer(Inventory.Container(14, "container14", 2, 4, 7))
        containerDao.insertContainer(Inventory.Container(15, "container15", 2, 4, 8))
        containerDao.insertContainer(Inventory.Container(16, "container16", 2, 4, 8))
        val itemDao = db.itemDao()
        itemDao.insertItem(Inventory.Item(1, "item1", 1, 1, 1, 1, null))
        itemDao.insertItem(Inventory.Item(2, "item2", 1, 1, 1, 1, null))
        itemDao.insertItem(Inventory.Item(3, "item3", 1, 1, 1, 2, null))
        itemDao.insertItem(Inventory.Item(4, "item4", 1, 1, 1, 2, null))
        itemDao.insertItem(Inventory.Item(5, "item5", 1, 1, 2, 3, null))
        itemDao.insertItem(Inventory.Item(6, "item6", 1, 1, 2, 3, null))
        itemDao.insertItem(Inventory.Item(7, "item7", 1, 1, 2, 4, null))
        itemDao.insertItem(Inventory.Item(8, "item8", 1, 1, 2, 4, null))
        itemDao.insertItem(Inventory.Item(9, "item9", 1, 2, 3, 5, null))
        itemDao.insertItem(Inventory.Item(10, "item10", 1, 2, 3, 5,null))
        itemDao.insertItem(Inventory.Item(11, "item11", 1, 2, 3, 6, null))
        itemDao.insertItem(Inventory.Item(12, "item12", 1, 2, 3, 6, null))
        itemDao.insertItem(Inventory.Item(13, "item13", 1, 2, 4, 7, null))
        itemDao.insertItem(Inventory.Item(14, "item14", 1, 2, 4, 7, null))
        itemDao.insertItem(Inventory.Item(15, "item15", 1, 2, 4, 8, null))
        itemDao.insertItem(Inventory.Item(16, "item16", 1, 2, 4, 8, null))
        itemDao.insertItem(Inventory.Item(17, "item17", 2, 3, 5, 9, null))
        itemDao.insertItem(Inventory.Item(18, "item18", 2, 3, 5, 9, null))
        itemDao.insertItem(Inventory.Item(19, "item19", 2, 3, 5, 10, null))
        itemDao.insertItem(Inventory.Item(20, "item20", 2, 3, 5, 10, null))
        itemDao.insertItem(Inventory.Item(21, "item21", 2, 3, 6, 11, null))
        itemDao.insertItem(Inventory.Item(22, "item22", 2, 3, 6, 11, null))
        itemDao.insertItem(Inventory.Item(23, "item23", 2, 3, 6, 12, null))
        itemDao.insertItem(Inventory.Item(24, "item24", 2, 3, 6, 12, null))
        itemDao.insertItem(Inventory.Item(25, "item25", 2, 4, 7, 13, null))
        itemDao.insertItem(Inventory.Item(26, "item26", 2, 4, 7, 13, null))
        itemDao.insertItem(Inventory.Item(27, "item27", 2, 4, 7, 14, null))
        itemDao.insertItem(Inventory.Item(28, "item28", 2, 4, 7, 14, null))
        itemDao.insertItem(Inventory.Item(29, "item29", 2, 4, 8, 15, null))
        itemDao.insertItem(Inventory.Item(30, "item30", 2, 4, 8, 15, null))
        itemDao.insertItem(Inventory.Item(31, "item31", 2, 4, 8, 16, null))
        itemDao.insertItem(Inventory.Item(32, "item32", 2, 4, 8, 16, null))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}