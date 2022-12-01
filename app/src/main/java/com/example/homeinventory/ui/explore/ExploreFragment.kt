package com.example.homeinventory.ui.explore

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.homeinventory.*
import com.example.homeinventory.InvUtils.makePopup
import com.example.homeinventory.databinding.AddObjectBinding
import com.example.homeinventory.databinding.FragmentExploreBinding
import com.example.homeinventory.databinding.DeleteConfirmBinding

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val daoList = InvUtils.daoList
    private var daoIndex = 0
    private lateinit var curInvObject: Inventory.InvObject
    private val adapter = InvListAdapter{ invObject: Inventory.InvObject, position: Int ->
        down(invObject, position)
    }
    private lateinit var labelBinding: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setup()
        binding.back.setOnClickListener {
            up()
        }
        binding.edit.setOnClickListener {
            if(labelBinding.isEnabled) {
                disableEdit()
                labelBinding.text.toString().let{newName -> curInvObject = when (daoIndex) {
                    1 -> (curInvObject as Inventory.Floor).copy(name = newName)
                    2 -> (curInvObject as Inventory.Room).copy(name = newName)
                    3 -> (curInvObject as Inventory.Surface).copy(name = newName)
                    else -> (curInvObject as Inventory.Container).copy(name = newName)
                }}
                daoList[daoIndex - 1].update(curInvObject)
            } else {
                labelBinding.isEnabled = true
                it.background = getDrawable(requireContext(), R.drawable.ic_check_black_24dp)
            }
        }
        binding.add.setOnClickListener {
            addObject()
        }
    }

    private fun addObject() {
        val addBinding = AddObjectBinding.inflate(layoutInflater)
        val popup = makePopup(addBinding.root)
        addBinding.okay.setOnClickListener {
            var name = addBinding.name.text.toString()
            if(name.isEmpty()) {
                name = "Unnamed"
            }
            daoList[daoIndex].insert(when(daoIndex) {
                0 -> Inventory.Floor(0, name)
                1 -> Inventory.Room(0, name, (curInvObject as Inventory.Floor).id)
                2 -> {(curInvObject as Inventory.Room).let {
                    Inventory.Surface(0, name, it.floor_id, it.id)
                }}
                3 -> {(curInvObject as Inventory.Surface).let {
                    Inventory.Container(0, name, it.floor_id, it.room_id, it.id)
                }}
                else -> {(curInvObject as Inventory.Container).let {
                    Inventory.Item(0, name, it.floor_id, it.room_id, it.surface_id, it.id, null)
                }}
            })
            if(daoIndex > 0) {
                adapter.submitList(daoList[daoIndex - 1].downList(curInvObject.id))
            } else {
                adapter.submitList(daoList[daoIndex].getAll())
            }
            popup.dismiss()
        }
        addBinding.cancel.setOnClickListener {
            popup.dismiss()
        }
    }

    private fun setup() {
        setupRV()
        adapter.submitList(daoList[daoIndex].getAll())
        labelBinding = binding.label
        labelBinding.isEnabled = false
        labelBinding.setTextColor(Color.BLACK)
        labelBinding.setText("Floors")
        binding.back.visibility = View.INVISIBLE
        binding.edit.visibility = View.INVISIBLE
    }

    private fun setupRV() {
        binding.rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rv.adapter = adapter
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder): Boolean = true
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deleteBinding = DeleteConfirmBinding.inflate(layoutInflater)
                val popup = makePopup(deleteBinding.root)
                deleteBinding.cancel.setOnClickListener {
                    adapter.dontDelete(viewHolder.adapterPosition)
                    popup.dismiss()
                }
                deleteBinding.yes.setOnClickListener {
                    cascadeDelete(adapter.delete(viewHolder.adapterPosition), daoIndex)
                    popup.dismiss()
                }
            }
        }).attachToRecyclerView(binding.rv)
    }

    private fun cascadeDelete(invObject: Inventory.InvObject, index: Int) {
        if(index < daoList.size - 1) {
            daoList[index].downList(invObject.id).forEach {
                cascadeDelete(it, index + 1)
            }
        }
        daoList[index].delete(invObject)
    }

    private fun disableEdit() {
        labelBinding.isEnabled = false
        binding.edit.background = getDrawable(requireContext(), R.drawable.ic_edit_black_24dp)
    }

    private fun down(invObject: Inventory.InvObject, position: Int) {
        disableEdit()
        if(daoIndex < daoList.size - 1) {
            binding.back.visibility = View.VISIBLE
            binding.edit.visibility = View.VISIBLE
            curInvObject = invObject
            labelBinding.setText(curInvObject.name)
            adapter.submitList(daoList[daoIndex].downList(curInvObject.id))
            daoIndex++
        } else {
            InvUtils.itemPopup(
                invObject as Inventory.Item, position, layoutInflater, requireContext(), viewLifecycleOwner,
                {name, pos -> adapter.updateName(name, pos)},
                {quantity, pos -> adapter.updateQuantity(quantity, pos)},
                {image, pos -> adapter.updateImage(image, pos)},
                {pos -> adapter.delete(pos) as Inventory.Item}
            )
        }
    }

    private fun up() {
        if(daoIndex > 1) {
            daoIndex--
            curInvObject = daoList[daoIndex].up(curInvObject)
            labelBinding.setText(curInvObject.name)
            adapter.submitList(daoList[daoIndex - 1].downList(curInvObject.id))
            disableEdit()
        } else {
            daoIndex = 0
            adapter.submitList(daoList[daoIndex].getAll())
            labelBinding.setText("Floors")
            binding.back.visibility = View.INVISIBLE
            binding.edit.visibility = View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}