package com.example.selifeproject.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selifeproject.R
import com.example.selifeproject.adapters.AdapterPhotos
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.internal.ContextUtils.getActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.view.*
import java.io.File

class MainActivity : AppCompatActivity() {

    var mPhotoList = ArrayList<Bitmap>()
    val REQUEST_CAMERA_CODE = 1
    val REQUEST_GALLERY_CODE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        var bottomsheetDialog = BottomSheetDialog(this)

        var view = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)

        bottomsheetDialog.setContentView(view)
        Log.d("abc", "bottomsheet created")

        button_main_bottom_sheet.setOnClickListener {
            bottomsheetDialog.show()
            Log.d("abc", "bottomsheet opened")
        }

        view.text_view_bottomsheet_camera.setOnClickListener {
            requestMultiplePermission()
            Log.d("abc", "camera button selected")
        }

        view.text_view_bottomsheet_gallery.setOnClickListener {
            requestSinglePermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            Log.d("abc", "gallery button selected")
        }

        getData()

    }

    private fun requestMultiplePermission() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                openCamera()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }

        }).check()
    }

    private fun requestSinglePermission(inputPermission: String) {

        Dexter.withContext(this).withPermission(inputPermission).withListener(object :
            PermissionListener {
            override fun onPermissionGranted(permission: PermissionGrantedResponse?) {
                when (inputPermission) {
                    Manifest.permission.READ_EXTERNAL_STORAGE -> openGalleryandChoose()
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) {
                TODO("Not yet implemented")
            }

            override fun onPermissionDenied(permission: PermissionDeniedResponse?) {
                Toast.makeText(applicationContext, "Permission has been denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }).check()
    }


    private fun openGalleryandChoose() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        startActivityForResult(intent, REQUEST_GALLERY_CODE)

    }

    @SuppressLint("RestrictedApi")
    private fun openCamera() {

        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        var file = File(getActivity(this)?.externalCacheDir, "${System.currentTimeMillis()}.jpg")
        var fileUri = Uri.fromFile(file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        startActivityForResult(intent, REQUEST_CAMERA_CODE)
        Log.d("abc", "camera application open")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CAMERA_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    var newImage = data.extras?.get("data") as Bitmap
                    mPhotoList.add(newImage)
                }
            }
            REQUEST_GALLERY_CODE -> {
                if (data != null) {
                    var selectImage = contentResolver.openInputStream(data.data!!)
                    var bitmap = BitmapFactory.decodeStream(selectImage) as Bitmap
                    selectImage?.close()
                    mPhotoList.add(bitmap)
                } else {
                    Toast.makeText(applicationContext, "Image has not selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getData(){
        var adapterPhotos = AdapterPhotos(this)
        adapterPhotos.setData(mPhotoList)
        recycler_view.adapter = adapterPhotos
        recycler_view.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        getData()
    }
}
