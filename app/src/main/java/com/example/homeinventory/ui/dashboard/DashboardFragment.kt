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
import com.example.homeinventory.InvListAdapter
import com.example.homeinventory.R
import com.example.homeinventory.RoomDB
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
    private lateinit var db: RoomDB.Inventory
    private lateinit var daoList: List<RoomDB.InvDao>
    private var daoIndex = 0
    private lateinit var curInvObject: RoomDB.InvObject
    private val adapter = InvListAdapter{invObject: RoomDB.InvObject, position: Int ->
        down(invObject, position)
    }
    private lateinit var labelBinding: EditText
    private var screenHeight = 0
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
        val metrics = (requireContext() as Activity).windowManager.currentWindowMetrics
        val insets = metrics.windowInsets.getInsets(WindowInsets.Type.navigationBars() or WindowInsets.Type.displayCutout())
        screenHeight = metrics.bounds.height() - (insets.bottom + insets.top)
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
                    daoList[daoIndex].delete(adapter.delete(viewHolder.adapterPosition))
                    popup.dismiss()
                }
            }
        }).attachToRecyclerView(binding.rv)
    }

    private fun down(invObject: RoomDB.InvObject, position: Int) {
        if(daoIndex < daoList.size - 1) {
            binding.back.visibility = View.VISIBLE
            binding.edit.visibility = View.VISIBLE
            curInvObject = invObject
            labelBinding.setText(curInvObject.name)
            adapter.submitList(daoList[daoIndex].downList(curInvObject.id))
            daoIndex++
        } else {
            itemPopup(invObject, position)
        }
    }

    private fun itemPopup(invObject: RoomDB.InvObject, position: Int) {
        val itemBinding = ItemBinding.inflate(layoutInflater)
        val popup = makePopup(itemBinding.root)
        val item = invObject as RoomDB.Item
        val idList = mutableListOf(item.floor_id, item.room_id, item.surface_id, item.container_id)
        setupSpinners(item, itemBinding, idList)
        itemBinding.back.setOnClickListener {
            if (itemBinding.containerSpin.adapter != null && idList.last() != -1) {
                itemBinding.quantityNum.text.toString().let {
                    if (it.isNotEmpty() && it.toInt() != item.quantity) {
                        daoList.last().update(item.copy(quantity = it.toInt()))
                        adapter.updateQuantity(it.toInt(), position)
                    }
                }
                if (idList.last() != item.container_id) {
                    daoList.last().update(
                        item.copy(
                            floor_id = idList[0],
                            room_id = idList[1],
                            surface_id = idList[2],
                            container_id = idList[3]
                        )
                    )
                    adapter.delete(position)
                }
                popup.dismiss()
            } else {
                mustHaveContainerPopup()
            }
        }
        itemBinding.delete.setOnClickListener {
            val deleteBinding = DeleteConfirmBinding.inflate(layoutInflater)
            val deletePopup = makePopup(deleteBinding.root)
            deleteBinding.cancel.setOnClickListener {
                deletePopup.dismiss()
            }
            deleteBinding.yes.setOnClickListener {
                daoList[daoIndex].delete(adapter.delete(position))
                deletePopup.dismiss()
                popup.dismiss()
            }
        }
        itemBinding.camera.setOnClickListener {
            val cameraBinding = CameraBinding.inflate(layoutInflater)
            val popup = makePopup(cameraBinding.root)
            startCamera(cameraBinding.preview.surfaceProvider)
            cameraBinding.takePicture.setOnClickListener {
                takePhoto {
                    daoList.last().update(item.copy(image = it))
                    popup.dismiss()
                    itemBinding.image.setImageURI(Uri.parse(it))
                }
            }
        }
        val itemLabel = itemBinding.label
        itemLabel.isEnabled = false
        itemLabel.setTextColor(Color.BLACK)
        itemLabel.setText(item.name)
        itemBinding.edit.setOnClickListener {
            if (itemLabel.isEnabled) {
                itemLabel.isEnabled = false
                daoList.last().update(item.copy(name = itemLabel.text.toString()))
                adapter.updateName(itemLabel.text.toString(), position)
                it.background = getDrawable(requireContext(), R.drawable.ic_edit_black_24dp)
            } else {
                itemLabel.isEnabled = true
                it.background = getDrawable(requireContext(), R.drawable.ic_check_black_24dp)
            }
        }
        itemBinding.quantityNum.setText(item.quantity.toString())
        if(item.image != null) {
            itemBinding.image.setImageURI(Uri.parse(item.image))
        }
    }

    //https://developer.android.com/codelabs/camerax-getting-started
    private fun takePhoto(savePhoto: (uri: String) -> Unit) {
        Log.d("XXX", "takePhoto() called")
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return
        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Inventory-Image")
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(requireContext().contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        Log.d("XXX", "actually gonna take the picture")
        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("XXX", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){
                    Log.d("XXX", "Photo capture succeeded: ${output.savedUri}")
                    savePhoto(output.savedUri.toString())
                }
            }
        )
    }

    private fun startCamera(surfaceProvider: SurfaceProvider) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
            val preview = Preview.Builder().build().also{it.setSurfaceProvider(surfaceProvider)}
            imageCapture = ImageCapture.Builder().build()
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e("XXX", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun mustHaveContainerPopup() {
        val mustBinding = MustHaveContainerBinding.inflate(layoutInflater)
        val popup = makePopup(mustBinding.root)
        mustBinding.okay.setOnClickListener {
            popup.dismiss()
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

    private fun setupSpinners(item: RoomDB.Item, binding: ItemBinding, idList: MutableList<Int>) {
        val spinnerList = listOf(binding.floorSpin, binding.roomSpin, binding.surfaceSpin, binding.containerSpin)
        listOnSpinner(daoList[0].getAll(), 0, spinnerList, idList)
    }

    private fun listOnSpinner(list: List<RoomDB.InvObject>, index: Int, spinnerList: List<Spinner>, idList: MutableList<Int>) {
        val spinner = spinnerList[index]
        //Floor represents not selected
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
            listOf<RoomDB.InvObject>(RoomDB.Floor(-1, "Select")) + list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        if(idList[index] != -1) {
            spinner.setSelection(list.indexOf(daoList[index].getById(idList[index])) + 1)
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                for(i in index + 1 until idList.size) {
                    spinnerList[i].adapter = null
                    //idList[i] = -1
                }
                if(p2 == 0) {
                    idList[index] = -1
                } else {
                    idList[index] = list[p2 - 1].id
                    if(index < daoList.size - 2) {
                        listOnSpinner(daoList[index].downList(idList[index]), index + 1, spinnerList, idList)
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun makePopup(popupBinding: ConstraintLayout): PopupWindow {
        val popup = PopupWindow(popupBinding, binding.root.measuredWidth, screenHeight, true)
        popup.showAtLocation(binding.root, Gravity.TOP, 0, 0)
        return popup
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}