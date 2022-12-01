package com.example.homeinventory

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.PopupWindow
import android.widget.Spinner
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.homeinventory.databinding.CameraBinding
import com.example.homeinventory.databinding.DeleteConfirmBinding
import com.example.homeinventory.databinding.ItemBinding
import com.example.homeinventory.databinding.MustHaveContainerBinding
import java.util.*

object InvUtils {
    private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private var imageCapture: ImageCapture? = null
    private var screenHeight = 0
    private var screenWidth = 0
    lateinit var daoList: List<RoomDB.InvDao>
    lateinit var itemDao: RoomDB.ItemDao

    fun setup(height: Int, width: Int, list: List<RoomDB.InvDao>) {
        screenHeight = height
        screenWidth = width
        daoList = list
        itemDao = list.last() as RoomDB.ItemDao
    }

    fun makePopup(popupBinding: ConstraintLayout): PopupWindow {
        val popup = PopupWindow(popupBinding, screenWidth, screenHeight, true)
        popup.showAtLocation(popupBinding, Gravity.TOP, 0, 0)
        return popup
    }

    fun itemPopup(item: RoomDB.Item, position: Int, layoutInflater: LayoutInflater,
                  context: Context, lifecycleOwner: LifecycleOwner,
                  updateName: (name: String, position: Int) -> Unit,
                  updateQuantity: (quantity: Int, position: Int) -> Unit,
                  updateImage: (image: String, position: Int) -> Unit,
                  delete: (position: Int) -> RoomDB.Item) {
        val itemBinding = ItemBinding.inflate(layoutInflater)
        val popup = makePopup(itemBinding.root)
        val idList = mutableListOf(item.floor_id, item.room_id, item.surface_id, item.container_id)
        setupSpinners(itemBinding, idList, context)
        itemBinding.back.setOnClickListener {
            if (itemBinding.containerSpin.adapter != null && idList.last() != -1) {
                itemBinding.quantityNum.text.toString().let {
                    if (it.isNotEmpty() && it.toInt() != item.quantity) {
                        itemDao.update(item.copy(quantity = it.toInt()))
                        updateQuantity(it.toInt(), position)
                    }
                }
                if (idList.last() != item.container_id) {
                    itemDao.update(item.copy(floor_id = idList[0], room_id = idList[1], surface_id = idList[2], container_id = idList[3]))
                    delete(position)
                }
                popup.dismiss()
            } else {
                mustHaveContainerPopup(layoutInflater)
            }
        }
        itemBinding.delete.setOnClickListener {
            val deleteBinding = DeleteConfirmBinding.inflate(layoutInflater)
            val deletePopup = makePopup(deleteBinding.root)
            deleteBinding.cancel.setOnClickListener {
                deletePopup.dismiss()
            }
            deleteBinding.yes.setOnClickListener {
                itemDao.delete(delete(position))
                deletePopup.dismiss()
                popup.dismiss()
            }
        }
        itemBinding.camera.setOnClickListener {
            val cameraBinding = CameraBinding.inflate(layoutInflater)
            val cameraPopup = makePopup(cameraBinding.root)
            startCamera(context, lifecycleOwner, cameraBinding.preview.surfaceProvider)
            cameraBinding.takePicture.setOnClickListener {
                takePhoto(context) {
                    cameraPopup.dismiss()
                    itemDao.update(item.copy(image = it))
                    itemBinding.image.setImageURI(Uri.parse(it))
                    updateImage(it, position)
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
                itemDao.update(item.copy(name = itemLabel.text.toString()))
                updateName(itemLabel.text.toString(), position)
                it.background = ContextCompat.getDrawable(context, R.drawable.ic_edit_black_24dp)
            } else {
                itemLabel.isEnabled = true
                it.background = ContextCompat.getDrawable(context, R.drawable.ic_check_black_24dp)
            }
        }
        itemBinding.quantityNum.setText(item.quantity.toString())
        if(item.image != null) {
            itemBinding.image.setImageURI(Uri.parse(item.image))
        }
    }

    //https://developer.android.com/codelabs/camerax-getting-started
    private fun takePhoto(context: Context, savePhoto: (uri: String) -> Unit) {
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
            .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context),
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

    private fun startCamera(context: Context, lifecycleOwner: LifecycleOwner, surfaceProvider: Preview.SurfaceProvider) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
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
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e("XXX", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun setupSpinners(binding: ItemBinding, idList: MutableList<Int>, context: Context) {
        val spinnerList = listOf(binding.floorSpin, binding.roomSpin, binding.surfaceSpin, binding.containerSpin)
        listOnSpinner(daoList[0].getAll(), 0, spinnerList, idList, context, true)
    }

    fun listOnSpinner(list: List<RoomDB.InvObject>, index: Int, spinnerList: List<Spinner>,
                              idList: MutableList<Int>, context: Context, isItem: Boolean) {
        val spinner = spinnerList[index]
        //Floor represents not selected
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item,
            listOf<RoomDB.InvObject>(RoomDB.Floor(-1, "Select")) + list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        if(isItem && idList[index] != -1) {
            spinner.setSelection(list.indexOf(daoList[index].getById(idList[index])) + 1)
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                for(i in index + 1 until idList.size) {
                    spinnerList[i].adapter = null
                    if(!isItem) {
                        idList[i] = -1
                    }
                }
                if(p2 == 0) {
                    idList[index] = -1
                } else {
                    idList[index] = list[p2 - 1].id
                    if(index < daoList.size - 2) {
                        listOnSpinner(daoList[index].downList(idList[index]), index + 1,
                            spinnerList, idList, context, isItem)
                    }
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun mustHaveContainerPopup(layoutInflater: LayoutInflater) {
        val mustBinding = MustHaveContainerBinding.inflate(layoutInflater)
        val popup = makePopup(mustBinding.root)
        mustBinding.okay.setOnClickListener {
            popup.dismiss()
        }
    }
}