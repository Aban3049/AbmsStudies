package com.pandaapps.abmsstudies.gallery.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import com.google.android.exoplayer2.MediaItem
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityAddVideoBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle


class AddVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddVideoBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    private var videoUri: Uri? = null

    private companion object {
        private const val TAG = "ADD_VIDE_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityAddVideoBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Please Wait...")

        binding.playerView.visibility = View.GONE

        binding.closeBtn.setOnClickListener {
            // Clear the video view
            binding.playerView.player = null
            videoUri = null
        }

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.VideoPickBtn.setOnClickListener {
            showVideoPickOptions()
            binding.playerView.visibility = View.VISIBLE
        }

        binding.publishBtn.setOnClickListener {
            validateData()
        }


    }

    private var titleVideo = ""

    private var uploadVideoUrl = ""
    private fun validateData() {

        titleVideo = binding.titleEt.text.toString()
        if (titleVideo.isEmpty()) {
            binding.titleEt.requestFocus()
            binding.titleEt.error = "Field Required"
        } else if (videoUri == null) {
            // No video selected
            MotionToast.createColorToast(
                this@AddVideoActivity,
                "Pick Again",
                "Failed to Get Video",
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
            )
        } else {
            uploadVideoToStorage()
        }

    }

    private fun uploadVideoToStorage() {
        progressDialog.setMessage("Uploading Video...")
        progressDialog.show()

        val timestamp = Utils.getTimestamp()
        val filePathAndName = "GalleryVideo/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)

        // Upload video to Firebase Storage
        storageReference.putFile(videoUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                uriTask.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        uploadVideoUrl = task.result.toString()
                        uploadInfoToDb(timestamp)
                    } else {
                        progressDialog.dismiss()
                        // Error getting download URL
                        MotionToast.createColorToast(
                            this@AddVideoActivity,
                            "Pick Again",
                            "Failed to Get Video URL",
                            MotionToastStyle.ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                        )
                    }
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                // Upload failed
                MotionToast.createColorToast(
                    this@AddVideoActivity,
                    "Pick Again",
                    "Failed to Upload Video due to ${e.message}",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )
            }
            .addOnProgressListener { snapshot ->
                val progress = (100 * snapshot.bytesTransferred / snapshot.totalByteCount)
                progressDialog.setMessage("Uploading $progress%")
            }
    }




    private fun uploadInfoToDb(timestamp: Long) {
        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = uid!!
        hashMap["id"] = "$timestamp"
        hashMap["title"] = titleVideo
        hashMap["timestamp"] = timestamp
        hashMap["videoUrl"] = uploadVideoUrl

        val ref = FirebaseDatabase.getInstance().getReference("GalleryVideos")
        ref.child(timestamp.toString())
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@AddVideoActivity,
                    "Success",
                    "Successfully Uploaded",
                    MotionToastStyle.SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )
                videoUri = null
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@AddVideoActivity,
                    "Pick Again",
                    "Failed to Upload Video due to ${e.message}",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )
            }
    }

    private fun showVideoPickOptions() {
        val popupMenu = PopupMenu(this, binding.VideoPickBtn)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Record Video")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Select from Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val itemId = item.itemId
            if (itemId == 1) {
                // Launch video recorder
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
                // Select from gallery

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickVideoGallery()
                } else {
                    // Device version is TIRAMISU,We need Storage permission to launch Gallery
                    val storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    requestStoragePermission.launch(storagePermission)
                }


            }
            true
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
            pickVideoCamera()
        } else {
            //Camera or Storage or Both permission are denied, Can't launch camera to capture image
            Utils.toast(this, "Camera or Storage or both permission denied...")
        }

    }

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "requestStoragePermission: isGranted: $isGranted")

        if (isGranted) {
            // Let's check if permission is granted or not
            pickVideoGallery()
        } else {
            //Storage Permission denied, we can't launch gallery to pick images
            Utils.toast(this, "Storage Permission denied.... ")
        }
    }

    private fun pickVideoGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "video/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickVideoCamera() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        videoUri = createVideoFileUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun createVideoFileUri(): Uri {
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Video.Media.TITLE, "TEMP_VIDEO_TITLE")
        contentValues.put(MediaStore.Video.Media.DESCRIPTION, "TEMP_VIDEO_DESCRIPTION")
        return contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)!!
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val videoUri = result.data?.data
            videoUri?.let {
                this.videoUri = it
                // Load the video
                loadVideo()
            }
        } else {
            // Video capture cancelled
            Utils.toast(this, "Video capture cancelled...")
        }
    }


    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            videoUri = data?.data

            //load video
            loadVideo()
        } else {
            Utils.toast(this, "Video capture cancelled...")
        }
    }

    private fun loadVideo() {
        // Initialize the player
        val player = SimpleExoPlayer.Builder(this).build()
        binding.playerView.player = player

        // Set the media item
        player.setMediaItem(MediaItem.fromUri(videoUri!!))

        // Prepare the player
        player.prepare()
    }



}