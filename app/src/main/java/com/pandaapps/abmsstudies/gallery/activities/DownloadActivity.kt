package com.pandaapps.abmsstudies.gallery.activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.databinding.ActivityDownloadBinding
import java.io.FileOutputStream

class DownloadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadBinding

    private lateinit var progressDialog:ProgressDialog

    private var videoUrl = ""

    private companion object{
        private const val TAG = "DOWNLOADING_VIDEO"
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityDownloadBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

       videoUrl = intent.getStringExtra("videoUrl")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Downloading Video")
        progressDialog.show()

        Log.d(TAG, "onCreate: videoUrl: $videoUrl")

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        download()

    }

    private fun download(){
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
            downloadVideo(videoUrl)
        } else {
            Log.d(TAG, "onCreate: STORAGE PERMISSION not granted, LETS request it")
            requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            //lets check if granted or not
            if (isGranted) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION not granted")
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION not granted")
                Toast.makeText(this@DownloadActivity, "Failed Permission Denied", Toast.LENGTH_SHORT).show()

            }
        }

    private fun downloadVideo(videoUrl: String) {
        Log.d(TAG, "downloadVideo: Downloading Video")
        //progress bar
        progressDialog.setMessage("Downloading Video")
        progressDialog.show()

        // Download video from Firebase Storage URL
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(videoUrl)
        storageReference.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadVideo: Video Downloaded")
                saveToDownloadsFolder(bytes)
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.e(TAG, "downloadVideo: Failed to download video due to ${e.message}")
                Toast.makeText(this@DownloadActivity, "Failed to download due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDownloadsFolder(bytes: ByteArray) {
        Log.d(TAG, "saveToDownloadsFolder: Saving downloaded video")

        val nameWithExtension = "GalleryVideo_${System.currentTimeMillis()}.mp4"

        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs() // Create folder if it does not exist
            val filepath = "${downloadsFolder.path}/$nameWithExtension"

            FileOutputStream(filepath).use { out ->
                out.write(bytes)
            }

            Toast.makeText(this@DownloadActivity, "Downloaded Successfully", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "saveToDownloadsFolder: Saved to Download Folder")
        } catch (e: Exception) {
            Log.e(TAG, "saveToDownloadsFolder: Failed to save due to ${e.message}")
            Toast.makeText(this@DownloadActivity, "Failed to save due to ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            progressDialog.dismiss()
        }
    }


}