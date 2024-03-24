package com.pandaapps.abmsstudies.sell.activities

import android.app.Activity
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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityProfileEditBinding
import java.io.ByteArrayOutputStream

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileEditBinding

    private companion object {
        private const val TAG = "PROFILE_EDIT_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var myUserType = ""
    private var imageUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadMyInfo()

        binding.ToolbarBackBtn.setOnClickListener {
            onBackPressed()
        }
        binding.profileImagePickFab.setOnClickListener {
            imagePickDialog()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }

    }

    private var name = ""
    private var dob = ""
    private var email = ""
    private var phoneCode = ""
    private var phoneNumber = ""

    private fun validateData() {
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        dob = binding.dobEt.text.toString().trim()
        phoneCode = binding.countryCodePicker.selectedCountryCodeWithPlus
        phoneNumber = binding.phoneNumberET.text.toString().trim()

        if (imageUri == null) {
            updateProfileDb(null)
        } else {
            uploadProfileImageStorage()
        }
    }

    private fun uploadProfileImageStorage() {
        Log.d(TAG, "uploadProfileImageStorage: ")
        // show progress
        progressDialog.setMessage("Uploading user profile Image")

        val filePathAndName = "UserProfile/profile_${firebaseAuth.uid}"

        val ref = FirebaseStorage.getInstance().reference.child(filePathAndName)

        //compress Image
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
        val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()


        ref.putBytes(reducedImage)
            .addOnProgressListener { snapshot ->
                val progress = 100 * snapshot.bytesTransferred / snapshot.totalByteCount
                Log.d(TAG, "uploadProfileImageStorage: progress: $progress")
                progressDialog.setMessage("Uploading profile image. Progress: $progress%")
                progressDialog.show()
            }
            .addOnSuccessListener { taskSnapshot ->
                Log.d(TAG, "uploadProfileImageStorage: Image uploaded...")

                val uriTask = taskSnapshot.storage.downloadUrl

                while (!uriTask.isSuccessful);
                var uploadedImageUrl = uriTask.result.toString()
                if (uriTask.isSuccessful) {
                    updateProfileDb(uploadedImageUrl)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "uploadProfileImageStorage", e)
                progressDialog.dismiss()
                Utils.toast(this@ProfileEditActivity, "Failed to upload Image due to ${e.message}")
            }


    }

    private fun updateProfileDb(uploadedImageUrl: String?) {
        Log.d(TAG, "updateProfileDb: uploadedImageUrl: $uploadedImageUrl")

        progressDialog.setMessage("Updating user info")
        progressDialog.show()

        val hashMap = HashMap<String, Any>()
        hashMap["name"] = "$name"
        hashMap["dob"] = "$dob"


        if (uploadedImageUrl != null) {
            hashMap["profileImageURl"] = "$uploadedImageUrl"
        }
        if (myUserType.equals("Phone", true)) {
            hashMap["email"] = "$email"
        } else if (myUserType.equals("Email", true) || myUserType.equals("Google", true)) {
            hashMap["phoneCode"] = "$phoneCode"
            hashMap["phoneNumber"] = "$phoneNumber"
        }

        // dataBase reference to update user info

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateProfileDb: updated...")
                progressDialog.dismiss()
                Utils.toast(this@ProfileEditActivity, "Updated...")

                imageUri = null
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "updateProfileDb:", e)
                progressDialog.dismiss()
                Utils.toast(this@ProfileEditActivity, "Failed to update due to ${e.message}")
            }

    }

    private fun loadMyInfo() {
        Log.d(TAG, "loadMyInfo :")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dob = "${snapshot.child("dob").value}"
                    val name = "${snapshot.child("name").value}"
                    val email = "${snapshot.child("email").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val profileImageURl = "${snapshot.child("profileImageURl").value}"
                    myUserType = "${snapshot.child("userType").value}"


                    val phone = phoneCode + phoneNumber

                    if (myUserType.equals("Email", true) || myUserType.equals("Google", true)) {
                        binding.emailTil.isEnabled = false
                        binding.emailEt.isEnabled = false
                    } else {
                        binding.phoneNumberTil.isEnabled = true
                        binding.phoneNumberET.isEnabled = true
                        binding.countryCodePicker.isEnabled = true
                    }

                    binding.emailEt.setText(email)
                    binding.dobEt.setText(dob)
                    binding.nameEt.setText(name)
                    binding.phoneNumberET.setText(phoneNumber)
                    try {
                        val phoneCodeInt = phoneCode.replace("+", "").toInt() // e.g +92 --->92
                        binding.countryCodePicker.setCountryForPhoneCode(phoneCodeInt)
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChanged :", e)
                    }

                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImageURl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.profileIv)
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChanged :", e)
                    }


                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun imagePickDialog() {
        val popupMenu = PopupMenu(this@ProfileEditActivity, binding.profileImagePickFab)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->

            val itemId = item.itemId
            if (itemId == 1) {
                Log.d(
                    TAG,
                    "imagePickDialog: Camera Clicked, check if camera permission(s) granted or not"
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestCameraPermissions.launch(arrayOf(android.Manifest.permission.CAMERA))
                } else {
                    requestCameraPermissions.launch(
                        arrayOf(
                            android.Manifest.permission.CAMERA,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
            } else if (itemId == 2) {
                Log.d(
                    TAG,
                    "imagePickDialog: Gallery Picked, check if storage permission granted or not"
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickImageGallery()
                } else {
                    requestStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }


            return@setOnMenuItemClickListener true

        }

    }

    private val requestCameraPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            Log.d(TAG, "requestCameraPermission: result: $result")

            // chk if permission is granted or not

            var areAllGranted = true
            for (isGranted in result.values) {
                areAllGranted = areAllGranted && isGranted
            }
            if (areAllGranted) {
                Log.d(TAG, "requestCameraPermissions: All are Granted e.g. Camera, Storage")
                pickImageCamera()
            } else {
                Log.d(TAG, "requestCameraPermission: All or either one is denied...")
                Utils.toast(this@ProfileEditActivity, "Camera or Storage or both permission denied")
            }

        }

    private val requestStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            Log.d(TAG, "requestStoragePermission: isGranted $isGranted")

            if (isGranted) {
                pickImageGallery()
            } else {
                Utils.toast(this@ProfileEditActivity, "Storage permission denied...")
            }


        }

    private fun pickImageCamera() {
        Log.d(TAG, "pickImageCamera: ")
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_image_title")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_image_description")

        imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            // chk if Image is captured or Not
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "cameraActivityResultLauncher: Image captured: imageUri: $imageUri")
                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.profileIv)
                } catch (e: Exception) {
                    Log.e(TAG, "cameraActivityResultLauncher:", e)
                }
            } else {
                Utils.toast(this@ProfileEditActivity, "Cancelled!")
            }

        }

    private fun pickImageGallery() {
        Log.d(TAG, "pickImageGallery")
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data

                imageUri = data!!.data

                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.profileIv)
                } catch (e: java.lang.Exception) {
                    Log.e(TAG, "galleryActivityResultLauncher")
                }
            }
        }

}