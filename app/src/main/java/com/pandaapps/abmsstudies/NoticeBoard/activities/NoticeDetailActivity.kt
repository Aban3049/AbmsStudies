package com.pandaapps.abmsstudies.NoticeBoard.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityNoticeDetailBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.FileOutputStream

class NoticeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoticeDetailBinding

    private lateinit var progressDialog: ProgressDialog

    private var noticeId = ""
    private var noticeImageUrl = ""
    private var noticeTitle = ""
    private var noticeDescription = ""

    private companion object {
        private const val TAG = "NOTICE_DETAIL_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityNoticeDetailBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Please wait...")


        noticeId = intent.getStringExtra("noticeId")!!

        binding.toolbarBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.downloadNoticeBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
                downloadBook()
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION not granted, LETS request it")
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        loadNoticeDetails()


    }


    private fun loadNoticeDetails() {

        Log.d(TAG, "loadBookDetail: ")

        //Books >bookId >Details
        val ref = FirebaseDatabase.getInstance().getReference("Notices")
        ref.child(noticeId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data

                    val uid = "${snapshot.child("uid").value}"
                    noticeImageUrl = "${snapshot.child("imageUrl").value}"
                    noticeTitle = "${snapshot.child("title").value}"
                    noticeDescription = "${snapshot.child("description").value}"




                    loadBookImage(
                        noticeImageUrl,
                        binding.progressBar,
                        baseContext,
                        binding.noticeIv
                    )

                    Log.d(TAG, "onDataChange: Successfully")

                }

                override fun onCancelled(error: DatabaseError) {

                    Log.e(TAG, "onCancelled: failed due to $error")

                }

            })

    }

    private fun loadBookImage(
        imageUrl: String,
        progressBar: ProgressBar,
        context: Context,
        imageView: ImageView
    ) {

        val TAG = "BOOK_IMAGE_TAG"

        //using url we can get image
        try {

            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_gray)
                .into(imageView)
            imageView.visibility = View.GONE

            android.os.Handler().postDelayed({
                progressBar.visibility = View.GONE
                imageView.visibility = View.VISIBLE
            }, 2000)


        } catch (e: Exception) {
            Log.e(TAG, "onDataChanged", e)
        }
    }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            //lets check if granted or not
            if (isGranted) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION not granted")
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION not granted")
                MotionToast.createColorToast(
                    this,
                    "Failed",
                    "Permission Denied",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )

            }
        }
    private fun downloadBook() {
        Log.d(TAG, "downloadBook: Downloading Books")
        //progress bar
        progressDialog.setMessage("Download Book")
        progressDialog.show()

        //lets download book from firebase storage storage url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(noticeImageUrl)
        storageReference.getBytes(Utils.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadBook: Book downloaded...")
                saveToDownloadsFolder(bytes)
            }
            .addOnFailureListener { e ->

                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: Failed to download book due to ${e.message}")
                MotionToast.createColorToast(
                    this,
                    "Failed",
                    "Failed to Download due to ${e.message}",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )
            }

    }

    private fun saveToDownloadsFolder(bytes: ByteArray?) {
        Log.d(TAG, "saveToDownloadsFolder: saving downloaded book")

        val nameWithExtension = "${noticeTitle + System.currentTimeMillis()}.jpg"

        try {
            val downloadsFolder =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs() // create folder if not exist
            val filepath = downloadsFolder.path + "/" + nameWithExtension

            val out = FileOutputStream(filepath)
            out.write(bytes)
            out.close()

            MotionToast.createColorToast(
                this,
                "Successfully",
                "Download Successfully",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
            )
            Log.d(TAG, "saveToDownloadsFolder: Save to Download Folder $downloadsFolder")
            progressDialog.dismiss()

        } catch (e: Exception) {
            progressDialog.dismiss()
            Log.e(TAG, "saveToDownloadsFolder: failed to save due to ${e.message}")
            MotionToast.createColorToast(
                this,
                "Failed",
                "Failed to Download due to ${e.message}",
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
            )

        }
    }


}