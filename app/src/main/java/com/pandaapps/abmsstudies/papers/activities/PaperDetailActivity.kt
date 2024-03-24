package com.pandaapps.abmsstudies.papers.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.books.MyApplication
import com.pandaapps.abmsstudies.databinding.ActivityPaperDetailBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.FileOutputStream

class PaperDetailActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPaperDetailBinding

    private companion object {
        private const val TAG = "PDF_DETAIL_PAPER_TAG"
    }

    private var paperId = ""
    private var paperTitle = ""
    private var paperUrl = ""
    private var paperImageUrl = ""


    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        binding=ActivityPaperDetailBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

      paperId = intent.getStringExtra("paperId")!!

        //init progress Bar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //handle read button click
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this@PaperDetailActivity, PaperPdfViewActivity::class.java)
            intent.putExtra("paperId", paperId)
            startActivity(intent)
        }

        binding.downloadBookBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
                downloadPaper()
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION not granted, LETS request it")
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        loadBookDetail()

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

    private fun downloadPaper() {
        Log.d(TAG, "downloadBook: Downloading Paper")
        //progress bar
        progressDialog.setMessage("Download Book")
        progressDialog.show()

        //lets download book from firebase storage storage url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(paperUrl)
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

        val nameWithExtension = "${paperTitle + System.currentTimeMillis()}.pdf"

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
            Log.d(TAG, "saveToDownloadsFolder: Save to Download Folder")
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

    private fun loadBookDetail() {

        Log.d(TAG, "loadPaperDetail: ")

        //Books >bookId >Details
        val ref = FirebaseDatabase.getInstance().getReference("Papers")
        ref.child(paperId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val timestamp = snapshot.child("timestamp").value as Long
                    paperTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    paperUrl= "${snapshot.child("url").value}"
                    paperImageUrl = "${snapshot.child("imageUrl").value}"


                    val formattedDate = MyApplication.formatTimestampDate(timestamp)

                    //   loadPdfCategory
                    MyApplication.loadCategoryPapers(categoryId, binding.categoryTv)


                    //load boo Image
                    MyApplication.loadBookImage(
                        paperImageUrl,
                        binding.progressBar,
                        baseContext,
                        binding.booksImageIv
                    )

                    //load pdf size
                    MyApplication.loadPdfSize(paperUrl, "$title", binding.sizeTv)

                    //set data
                    binding.titleTv.text = paperTitle
                    binding.dateTv.text = formattedDate
                    Log.d(TAG, "onDataChange: Successfully")

                }

                override fun onCancelled(error: DatabaseError) {

                    Log.e(TAG, "onCancelled: failed due to $error")

                }

            })

    }

}