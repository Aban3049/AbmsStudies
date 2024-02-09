package com.pandaapps.abmsstudies.books.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.EntityIterator
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.datastore.preferences.protobuf.Value
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.books.MyApplication
import com.pandaapps.abmsstudies.databinding.ActivityPdfDetailBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.FileOutputStream
import java.math.RoundingMode

class PdfDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfDetailBinding

    private companion object {
        private const val TAG = "PDF_DETAIL_TAG"
    }

    private var bookId = ""
    private var bookTitle = ""
    private var bookUrl = ""
    private var bookImageUrl = ""

    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityPdfDetailBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        //init progress Bar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait...")
        progressDialog.setCanceledOnTouchOutside(false)


        //increment books count,whenever this page starts
        MyApplication.incrementBooksViewCount(bookId)

        loadBookDetail()


        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //handle read button click
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this@PdfDetailActivity, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        binding.downloadBookBtn.setOnClickListener {
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
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
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

        val nameWithExtension = "${bookTitle + System.currentTimeMillis()}.pdf"

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
            Log.d(TAG, "saveToDownloadsFolder: Save to Donwlaod Folder")
            progressDialog.dismiss()
            incrementDownloadCount()

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

        Log.d(TAG, "loadBookDetail: ")

        //Books >bookId >Details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = snapshot.child("timestamp").value as Long
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    bookImageUrl = "${snapshot.child("imageUrl").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    val formattedDate = MyApplication.formatTimestampDate(timestamp)

                    //   loadPdfCategory
                    MyApplication.loadCategory(categoryId, binding.categoryTv)
                    // load Pdf ThumbNail
//                    MyApplication.loadPdfFromUrlSinglePage(
//                        "$bookUrl",
//                        "$title",
//                        binding.pdfView,
//                        binding.progressBar,
//                        binding.pagesTv
//                    )

                    //load boo Image
                    MyApplication.loadBookImage(bookImageUrl,binding.progressBar,baseContext,binding.booksImageIv)

                    //load pdf size
                    MyApplication.loadPdfSize("$bookUrl", "$title", binding.sizeTv)

                    //set data
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadsCount
                    binding.dateTv.text = formattedDate
                    Log.d(TAG, "onDataChange: Successfully")

                }

                override fun onCancelled(error: DatabaseError) {

                    Log.e(TAG, "onCancelled: failed due to $error")

                }

            })

    }

    private fun incrementDownloadCount() {
        //increment download count
        Log.d(TAG, "incrementDownloadCount: ")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    //get download count

                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG, "onDataChange: onDataChange: Current Downloads Count $downloadsCount")

                    if (downloadsCount == "" || downloadsCount == "null"){
                        downloadsCount = "0"
                    }

                    //covert to Long and increment
                    val newDownloadCount:Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: New Download Count $newDownloadCount")

                    val hashMap:HashMap<String,Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadCount

                    //Update Increment downloads Count
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Download Count increment")
                        }.addOnFailureListener {e ->
                            Log.e(TAG, "onDataChange: Failed to increment due to ${e.message}", )
                        }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }


}