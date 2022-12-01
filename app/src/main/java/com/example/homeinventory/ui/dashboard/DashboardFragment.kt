package com.example.homeinventory.ui.dashboard

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.*
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentResolverCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.viewbinding.ViewBinding
import com.example.homeinventory.*
import com.example.homeinventory.InvUtils.makePopup
import com.example.homeinventory.databinding.AddObjectBinding
import com.example.homeinventory.databinding.CameraBinding
import com.example.homeinventory.databinding.FragmentDashboardBinding
import com.example.homeinventory.databinding.DeleteConfirmBinding
import com.example.homeinventory.databinding.ItemBinding
import com.example.homeinventory.databinding.MustHaveContainerBinding
import java.util.*
import java.util.concurrent.Executors

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val daoList = InvUtils.daoList
    private var daoIndex = 0
    private lateinit var curInvObject: RoomDB.InvObject
    private val adapter = InvListAdapter{invObject: RoomDB.InvObject, position: Int ->
        down(invObject, position)
    }
    private lateinit var labelBinding: EditText
    var imageCapture: ImageCapture? = null
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

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
        }
        binding.edit.setOnClickListener {
            if(labelBinding.isEnabled) {
                disableEdit()
                labelBinding.text.toString().let{newName -> curInvObject = when (daoIndex) {
                    1 -> (curInvObject as RoomDB.Floor).copy(name = newName)
                    2 -> (curInvObject as RoomDB.Room).copy(name = newName)
                    3 -> (curInvObject as RoomDB.Surface).copy(name = newName)
                    else -> (curInvObject as RoomDB.Container).copy(name = newName)
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
                0 -> RoomDB.Floor(0, name)
                1 -> RoomDB.Room(0, name, (curInvObject as RoomDB.Floor).id)
                2 -> {(curInvObject as RoomDB.Room).let {
                    RoomDB.Surface(0, name, it.floor_id, it.id)
                }}
                3 -> {(curInvObject as RoomDB.Surface).let {
                    RoomDB.Container(0, name, it.floor_id, it.room_id, it.id)
                }}
                else -> {(curInvObject as RoomDB.Container).let {
                    RoomDB.Item(0, name, it.floor_id, it.room_id, it.surface_id, it.id, null)
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

    private fun cascadeDelete(invObject: RoomDB.InvObject, index: Int) {
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

    private fun down(invObject: RoomDB.InvObject, position: Int) {
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
                invObject as RoomDB.Item, position, layoutInflater, requireContext(), viewLifecycleOwner,
                {name, pos -> adapter.updateName(name, pos)},
                {quantity, pos -> adapter.updateQuantity(quantity, pos)},
                {image, pos -> adapter.updateImage(image, pos)},
                {pos -> adapter.delete(pos) as RoomDB.Item}
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