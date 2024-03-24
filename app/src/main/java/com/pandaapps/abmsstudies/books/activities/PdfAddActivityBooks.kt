package com.pandaapps.abmsstudies.books.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityPdfAddBooksBinding
import com.pandaapps.abmsstudies.books.model.ModelBooksCategoryAdmin
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.ByteArrayOutputStream

class PdfAddActivityBooks : AppCompatActivity() {

    private lateinit var binding: ActivityPdfAddBooksBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var imageUri: Uri? = null

    // uri of picked Pdf

    private var pdfUri: Uri? = null


    private companion object {
        private const val TAG = "PDF_ADD_TAG"
    }

    private lateinit var categoryArrayList: ArrayList<ModelBooksCategoryAdmin>

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityPdfAddBooksBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadCategories()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.categoryTv.setOnClickListener {
            categoryPickDialog()
        }

        binding.pdfUploadBtn.setOnClickListener {
            pdfPickIntent()
        }

        // handle click upload pdf

        binding.submitBtn.setOnClickListener {

            validateData()
        }

        binding.ImagePickBtn.setOnClickListener {
            showImagePickOptions()
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

    }

    private var title = ""
    private var description = ""
    private var category = ""
    private var uploadImageUrl = ""

    private fun validateData() {

        Log.d(TAG, "validateData: validating data")

        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        if (title.isEmpty()) {
            binding.titleEt.requestFocus()
            binding.titleEt.error = "Field Cannot be empty"
        } else if (description.isEmpty()) {
            binding.descriptionEt.requestFocus()
            binding.descriptionEt.error = "Field cannot be empty"
        } else if (category.isEmpty()) {
            binding.titleEt.requestFocus()
            binding.categoryTv.error = "Pick Category"
        } else if (pdfUri == null) {
            MotionToast.createColorToast(
                this@PdfAddActivityBooks,
                "Warning",
                "Pick PDF",
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
            )
        } else if (imageUri == null) {
            MotionToast.createColorToast(
                this@PdfAddActivityBooks,
                "Warning",
                "Pick Image",
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
            )
        } else {
            //data validate begin upload
            uploadBookImageToStorage()
            uploadPdfToStorage()
        }

    }

    private fun pdfPickIntent() {

        Log.d(TAG, "pdfPickIntent: starting pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)

    }

    private val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()


    ) { result ->

        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "PDF Picked: ")

            pdfUri = result.data!!.data
        } else {
            Log.d(TAG, "PDF picked Cancelled: ")
            MotionToast.createColorToast(
                this@PdfAddActivityBooks,
                "Failed",
                "Failed to Get Pdf",
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
            )
        }

    }

    private fun uploadPdfToStorage() {

        Log.d(TAG, "uploadPdfToStorage: uploading to storage....")

        //show progress dialog
        progressDialog.setMessage("Uploading PDF...")
        progressDialog.show()

        //timestamp in
        val timestamp = Utils.getTimestamp()

        //path f pdf in firebase storage

        val filePathAndName = "Books/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot ->

                Log.d(TAG, "uploadPdfToStorage: pdf uploaded now getting url ...")

                //get url of uploaded PDF
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);

                val uploadPdfUrl = "${uriTask.result}"
                uploadPdfInfoToDb(uploadPdfUrl, timestamp)


            }
            .addOnFailureListener { e ->

                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@PdfAddActivityBooks,
                    "Pick Again",
                    "Failed to Get Pdf due to ${e.message}",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )


            }
            .addOnProgressListener {

                val dataTransferred = (100 * it.bytesTransferred / it.totalByteCount)
                progressDialog.setMessage("Uploading $dataTransferred%")
            }

    }

    private fun uploadBookImageToStorage() {
        Log.d(TAG, "uploadPdfToStorage: uploading to storage....")

        //show progress dialog
        progressDialog.setMessage("Uploading Image...")
        progressDialog.show()

        //timestamp in
        val timestamp = Utils.getTimestamp()

        //path f pdf in firebase storage

        val filePathAndName = "BooksImage/$timestamp"


        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        // compressing image
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
        val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()

        storageReference.putBytes(reducedImage)
            .addOnSuccessListener { taskSnapshot ->

                Log.d(TAG, "uploadPdfToStorage: Image uploaded now getting url ...")

                //get url of uploaded Image
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);

                uploadImageUrl = "${uriTask.result}"
            }
            .addOnFailureListener { e ->

                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@PdfAddActivityBooks,
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

    private fun uploadPdfInfoToDb(uploadPdfUrl: String, timestamp: Long) {
// upload pdf to firebase db
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading pdf info....")

        //uid of  current user
        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()


        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["imageUrl"] = "$uploadImageUrl"
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)

            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: uploaded to db")
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@PdfAddActivityBooks,
                    "Success",
                    "Successfully Uploaded",
                    MotionToastStyle.SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )

                pdfUri = null

            }

            .addOnFailureListener { e ->
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@PdfAddActivityBooks,
                    "Pick Again",
                    "Failed to Get Pdf due to ${e.message}",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )
            }

    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: Loading pdf categories")

        //init array list
        categoryArrayList = ArrayList()

        //db reference to get categories

        val ref = FirebaseDatabase.getInstance().getReference("CategoriesBooks")

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                categoryArrayList.clear()

                for (ds in snapshot.children) {

                    val model = ds.getValue(ModelBooksCategoryAdmin::class.java)

                    //add to array list

                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")

                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing pdf category pick dialog")

        // get string array of categories from arraylist

        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)

        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        // alert dialog

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoriesArray) { _, which ->

                //handle item click get item click

                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                //set category to textView

                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Selected category Id: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Selected Category Title: $selectedCategoryTitle")

            }
            .show()

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