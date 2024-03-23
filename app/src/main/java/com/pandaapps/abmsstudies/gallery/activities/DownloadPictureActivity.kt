package com.pandaapps.abmsstudies.gallery.activities

import android.Manifest
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
import com.pandaapps.abmsstudies.databinding.ActivityDownloadPictureBinding
import java.io.FileOutputStream

class DownloadPictureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadPictureBinding

    private lateinit var progressDialog: ProgressDialog

    private var imageUrl = ""

    private companion object {
        private const val TAG = "DOWNLOADING_PICTURES"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityDownloadPictureBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        imageUrl = intent.getStringExtra("imageUrl")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Downloading Image")
        progressDialog.show()


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
            downloadPicture()
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
                Toast.makeText(this@DownloadPictureActivity, "Failed Permission Denied", Toast.LENGTH_SHORT).show()

            }
        }

    private fun downloadPicture() {
        Log.d(TAG, "downloadLectures: Downloading Assignment")
        //progress bar
        progressDialog.setMessage("Downloading Picture")
        progressDialog.show()

        //lets download book from firebase storage storage url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        storageReference.getBytes(1000000)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadAssignment: Assignment Downloaded...")
                saveToDownloadsFolder(bytes)
            }
            .addOnFailureListener { e ->

                progressDialog.dismiss()
                Log.d(TAG, "Assignment: Failed to download Assignment due to ${e.message}")
                Toast.makeText(this@DownloadPictureActivity, "Failed to download due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun saveToDownloadsFolder(bytes: ByteArray?) {
        Log.d(TAG, "saveToDownloadsFolder: saving downloaded book")

        val nameWithExtension = "${"GalleyPictures" + System.currentTimeMillis()}.jpg"

        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs() // create folder if not exist
            val filepath = downloadsFolder.path + "/" + nameWithExtension

            val out = FileOutputStream(filepath)
            out.write(bytes)
            out.close()

            Toast.makeText(this@DownloadPictureActivity, "Downloaded Successfully", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "saveToDownloadsFolder: Save to Download Folder")
            progressDialog.dismiss()


        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.e(TAG, "saveToDownloadsFolder: failed to save due to ${e.message}")
            Toast.makeText(this@DownloadPictureActivity, "Failed to download due to ${e.message}", Toast.LENGTH_SHORT).show()

        }
    }


}