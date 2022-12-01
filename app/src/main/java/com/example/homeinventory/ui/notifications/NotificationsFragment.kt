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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.homeinventory.InvUtils
import com.example.homeinventory.MainActivity
import com.example.homeinventory.ResultsAdapter
import com.example.homeinventory.RoomDB
import com.example.homeinventory.databinding.DeleteConfirmBinding
import com.example.homeinventory.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var adapter: ResultsAdapter
    private val itemDao = InvUtils.itemDao
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
        adapter = ResultsAdapter(itemDao){item, position -> openItem(item, position)}
        setupRV()
        val spinnerList = listOf(binding.floorSpin, binding.roomSpin, binding.surfaceSpin, binding.containerSpin)
        InvUtils.listOnSpinner(InvUtils.daoList[0].getAll(), 0, spinnerList, idList, requireContext(), false)
        binding.searchButton.setOnClickListener {
            search()
        }
    }

    private fun setupRV() {
        binding.results.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.results.addItemDecoration(DividerItemDecoration(requireContext(), 1))
        binding.results.adapter = adapter
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean = true
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deleteBinding = DeleteConfirmBinding.inflate(layoutInflater)
                val popup = InvUtils.makePopup(deleteBinding.root)
                deleteBinding.cancel.setOnClickListener {
                    adapter.dontDelete(viewHolder.adapterPosition)
                    popup.dismiss()
                }
                deleteBinding.yes.setOnClickListener {
                    itemDao.delete(adapter.delete(viewHolder.adapterPosition))
                    popup.dismiss()
                }
            }
        }).attachToRecyclerView(binding.results)
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
        adapter.submitList(items)
    }

    private fun openItem(item: RoomDB.Item, position: Int) {
        InvUtils.itemPopup(
            item, position, layoutInflater, requireContext(), viewLifecycleOwner,
            {name, pos -> adapter.updateName(name, pos)},
            {quantity, pos -> adapter.updateQuantity(quantity, pos)},
            {image, pos -> adapter.updateImage(image, pos)},
            {pos ->
                val deleted = adapter.delete(pos)
                search()
                deleted
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}