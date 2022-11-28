package com.example.homeinventory.ui.dashboard

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.homeinventory.InvListAdapter
import com.example.homeinventory.R
import com.example.homeinventory.RoomDB
import com.example.homeinventory.databinding.FragmentDashboardBinding
import com.example.homeinventory.databinding.DeleteConfirmBinding
import org.w3c.dom.Text

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
                labelBinding.text.toString().let{newName -> curInvObject = when (daoIndex) {
                    1 -> (curInvObject as RoomDB.Floor).copy(name = newName)
                    2 -> (curInvObject as RoomDB.Room).copy(name = newName)
                    3 -> (curInvObject as RoomDB.Surface).copy(name = newName)
                    else -> (curInvObject as RoomDB.Container).copy(name = newName)
                }}
                daoList[daoIndex - 1].update(curInvObject)
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
                val screenHeight = (requireContext() as Activity).windowManager
                    .currentWindowMetrics.windowInsets.getInsets(WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
                val screenHeight2 = screenHeight.bottom + screenHeight.top
                val bounds = (requireContext() as Activity).windowManager
                    .currentWindowMetrics.bounds.height()
                val popup = PopupWindow(deleteBinding.root, binding.root.measuredWidth, bounds - screenHeight2, true)
                popup.showAtLocation(binding.root, Gravity.TOP, 0, 0)
                deleteBinding.cancel.setOnClickListener {
                    adapter.dontDelete(viewHolder.adapterPosition)
                    popup.dismiss()
                }
                deleteBinding.yes.setOnClickListener {
                    daoList[daoIndex].delete(adapter.delete(viewHolder.adapterPosition))
                    popup.dismiss()
                }
                popup.setOnDismissListener {

                }
            }
        }).attachToRecyclerView(binding.rv)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}