package com.pandaapps.abmsstudies.gallery.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityAddPicturesBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.ByteArrayOutputStream

class AddPicturesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPicturesBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null

    private lateinit var progressDialog: ProgressDialog

    private companion object {
        private const val TAG = "ADD_PICTURES_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityAddPicturesBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Please Wait...")

        firebaseAuth = FirebaseAuth.getInstance()

        binding.imageIv.visibility = View.GONE

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.closeBtn.setOnClickListener {
            try {

                Log.d(TAG, "onBindViewHolder: imageUri: $imageUri")

                Glide.with(this)
                    .clear(binding.imageIv)
            } catch (e: Exception) {
                Log.e(TAG, "onBindViewHolder:", e)
            }
        }

        binding.submitBtn.setOnClickListener {

            validateData()
        }

        binding.ImagePickBtn.setOnClickListener {
            showImagePickOptions()
            binding.imageIv.visibility = View.VISIBLE
        }


    }

    private var titlePic = ""
    private var uploadImageUrl = ""

    private fun validateData() {

        titlePic = binding.titleEt.text.toString()

        if (titlePic.isEmpty()) {
            binding.titleEt.requestFocus()
            binding.titleEt.error = "Field Required"
        } else if (imageUri == null) {
            MotionToast.createColorToast(
                this@AddPicturesActivity,
                "Pick Again",
                "Failed to Get Image",
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
            )
        } else {
            uploadImageToStorage()
        }

    }

    private fun uploadImageToStorage() {
        Log.d(TAG, "uploadPdfToStorage: uploading to storage....")

        //show progress dialog
        progressDialog.setMessage("Uploading Image...")
        progressDialog.show()

        //timestamp in
        val timestamp = Utils.getTimestamp()

        //path f pdf in firebase storage

        val filePathAndName = "GalleryImage/$timestamp"


        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        // compressing image
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
        val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()

        storageReference.putBytes(reducedImage)
            .addOnSuccessListener { taskSnapshot ->

                Log.d(TAG, "uploadImageToStorage: Image uploaded now getting url ...")

                //get url of uploaded Image
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);

                uploadImageUrl = "${uriTask.result}"

                uploadInfoToDb(timestamp)

            }
            .addOnFailureListener { e ->

                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@AddPicturesActivity,
                    "Pick Again",
                    "Failed to Get Image due to ${e.message}",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )


                Log.i("xxx", "Failed uploading image to server")

            }
            .addOnProgressListener {

                val dataTransferred = (100 * it.bytesTransferred / it.totalByteCount)
                progressDialog.setMessage("Uploading $dataTransferred%")
            }

    }

    private fun uploadInfoToDb(timestamp: Long) {
// upload pdf to firebase db
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading image info....")

        //uid of  current user
        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()


        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$titlePic"
        hashMap["timestamp"] = timestamp
        hashMap["imageUrl"] = "$uploadImageUrl"

        val ref = FirebaseDatabase.getInstance().getReference("GalleryPictures")
        ref.child("$timestamp")
            .setValue(hashMap)

            .addOnSuccessListener {
                Log.d(TAG, "uploadInfoToDb: uploaded to db")
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@AddPicturesActivity,
                    "Success",
                    "Successfully Uploaded",
                    MotionToastStyle.SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )
                imageUri = null

            }

            .addOnFailureListener { e ->
                Log.d(
                    TAG,
                    "uploadToStorage: failed to upload due to ${e.message}"
                )
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@AddPicturesActivity,
                    "Pick Again",
                    "Failed to Get Image due to ${e.message}",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )
            }

    }

    private fun loadImages() {
        Log.d(TAG, "loadImages: ")
        try {

            Log.d(TAG, "onBindViewHolder: imageUri: $imageUri")

            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.ic_image_gray)
                .into(binding.imageIv)
        } catch (e: Exception) {
            Log.e(TAG, "onBindViewHolder:", e)
        }

    }

    private fun showImagePickOptions() {
        Log.d(TAG, "showImagePickOptions: ")

        //s how popUp on Button
        val popupMenu = PopupMenu(this, binding.ImagePickBtn)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
// get the id of the item clicked in popup menu
            val itemId = item.itemId
            // check which item is clicked from popUp menu, 1=Camera. 2=Gallery as we defined
            if (itemId == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    val cameraPermission = arrayOf(android.Manifest.permission.CAMERA)
                    requestCameraPermission.launch(cameraPermission)
                } else {
                    // Device version is TIRAMISU,We need Storage permission to launch Camera
                    val cameraPermissions = arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    requestCameraPermission.launch(cameraPermissions)
                }
            } else if (itemId == 2) {
                // Gallery is clicked we need to check if we have permission of Storage before launching Gallery to Pick image
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickImageGallery()
                } else {
                    // Device version is TIRAMISU,We need Storage permission to launch Gallery
                    val storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    requestStoragePermission.launch(storagePermission)
                }
            }

            true
        }
    }

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "requestStoragePermission: isGranted: $isGranted")

        if (isGranted) {
            // Let's check if permission is granted or not
            pickImageGallery()
        } else {
            //Storage Permission denied, we can't launch gallery to pick images
            Utils.toast(this, "Storage Permission denied.... ")
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d(TAG, "requestCameraPermission: result: $result")
        //let's check if permissions are granted or not
        var areAllGranted = true
        for (isGranted in result.values) {
            areAllGranted = areAllGranted && isGranted
        }
        if (areAllGranted) {
            //All Permission Camera,Storage are granted,we can now launch camera to capture image
            pickImageCamera()
        } else {
            //Camera or Storage or Both permission are denied, Can't launch camera to capture image
            Utils.toast(this, "Camera or Storage or both permission denied...")
        }

    }

    private fun pickImageGallery() {
        Log.d(TAG, "pickImageGallery:")
        val intent = Intent(Intent.ACTION_PICK)
        // we only want to pick Images
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera() {
        Log.d(TAG, "pickImageCamera:")

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_IMAGE_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_IMAGE_DESCRIPTION")
        // Uri of image to be captured from camera
        imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        //Intent to launch Camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "galleryActivityResultLauncher :")
        // Check if image is picked or not
        if (result.resultCode == Activity.RESULT_OK) {

            //get data from result param
            val data = result.data
            //get uri of image picked
            imageUri = data!!.data
            Log.d(TAG, "galleryActivityResultLauncher: imageUri: $imageUri")

            //timestamp will be used as id of image picked


            //setup model for image,Param 1 is id,Param 2 is imageUri, Param 3 is imageUrl , from internet
            loadImages()
        } else {
            // Cancelled
            Utils.toast(this, "Cancelled...!")
        }
// Check if image is picked or not
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "cameraActivityResultLauncher:")
        //Check if image is picked or not
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "cameraActivityResultLauncher: imageUri $imageUri")


            loadImages()
        } else {
            Utils.toast(this, "Cancelled...!")
        }
    }

}