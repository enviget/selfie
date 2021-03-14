package com.example.selifeproject.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.selifeproject.BuildConfig
import com.example.selifeproject.R
import com.example.selifeproject.adapters.AdapterPhotos
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
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
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var mPhotoList = ArrayList<Uri>()
    val REQUEST_CAMERA_CODE = 1
    val REQUEST_GALLERY_CODE = 2
    lateinit var selectedUri: Uri

    lateinit var mUri: Uri
    lateinit var auth: FirebaseAuth
    lateinit var userId: String
    lateinit var storage: FirebaseStorage
    lateinit var storageReference: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()
        userId = auth.uid.toString()
        Log.d("abc", "userId is = " + auth.uid.toString())
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        displayList()
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
        button_logout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
        }
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
                token: PermissionToken?
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
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        startActivityForResult(intent, REQUEST_GALLERY_CODE)
    }

    private fun openCamera() {
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        var url = System.currentTimeMillis().toString() + ".jpg"
        val capturedImage = File(externalCacheDir, url)
        if (capturedImage.exists()) {
            capturedImage.delete()
        }
        capturedImage.createNewFile()
        mUri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(
                Objects.requireNonNull(applicationContext),
                BuildConfig.APPLICATION_ID + ".provider", capturedImage
            );
        } else {
            Uri.fromFile(capturedImage)
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
            .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

        startActivityForResult(intent, REQUEST_CAMERA_CODE)

        Log.d("abc", "camera application open")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CAMERA_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Log.d("abc", "${data.data.toString()}")
                }
            }
            REQUEST_GALLERY_CODE -> {
                if (data?.data != null) {
                    selectedUri = data.data!!
                    uploadPicture()
                } else {
                    Toast.makeText(applicationContext, "Image has not selected", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    fun displayList() {
        mPhotoList.clear()
        var imageRef =
            FirebaseStorage.getInstance().getReferenceFromUrl("gs://apolis-b29.appspot.com")
        var pathReference = imageRef.child("$userId/image")

        pathReference.listAll().addOnSuccessListener(object : OnSuccessListener<ListResult> {
            override fun onSuccess(result: ListResult?) {
                var items = result?.items
                items?.forEach {
                    it.downloadUrl.addOnSuccessListener(object : OnSuccessListener<Uri> {
                        override fun onSuccess(uri: Uri) {
                            mPhotoList.add(uri)
                            Log.d("item", "$uri")
                        }
                    }).addOnCompleteListener {
                        var adapterPhotos = AdapterPhotos(this@MainActivity)
                        adapterPhotos.setData(mPhotoList)
                        recycler_view.adapter = adapterPhotos
                        recycler_view.layoutManager = LinearLayoutManager(this@MainActivity)
                    }
                }
            }
        })
    }

    private fun uploadPicture() {
        val randomKey = UUID.randomUUID().toString()
        var storageRef = storageReference.child("$userId/image/$randomKey")

        storageRef.putFile(selectedUri)
            .addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
                override fun onSuccess(uploadTask: UploadTask.TaskSnapshot?) {
                    Toast.makeText(applicationContext, "image uploaded", Toast.LENGTH_SHORT).show()
                    displayList()
                }
            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(exception: Exception) {
                    Toast.makeText(applicationContext, exception.message, Toast.LENGTH_SHORT).show()
                }
            })
    }


}
