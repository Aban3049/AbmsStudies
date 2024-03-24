package com.pandaapps.abmsstudies.books.activities

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.books.MyApplication
import com.pandaapps.abmsstudies.books.adapter.AdapterComment
import com.pandaapps.abmsstudies.books.model.ModelComments
import com.pandaapps.abmsstudies.databinding.ActivityPdfDetailBinding
import com.pandaapps.abmsstudies.databinding.DialogCommentAddBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.FileOutputStream

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
    private lateinit var firebaseAuth: FirebaseAuth

    //arraylist to hold comments
    private lateinit var commentArrayList: ArrayList<ModelComments>

    //adapter set to recyclerView
    private lateinit var adapterComment:AdapterComment
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityPdfDetailBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()



        bookId = intent.getStringExtra("bookId")!!

        //init progress Bar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait...")
        progressDialog.setCanceledOnTouchOutside(false)


        //increment books count,whenever this page starts
        MyApplication.incrementBooksViewCount(bookId)

        loadBookDetail()
        showComments()


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



        //handle click show comment dialog
        binding.addCommentBtn.setOnClickListener {
            //user must be logged in to comment
            if (firebaseAuth.currentUser == null){
                //user not logged in,comment not allowed
                Utils.toast(this,"Only Logged in Use can Comment")
            }
            else{
                //user loggged in comment allowed
                addCommentDialog()
            }
        }

    }

    private fun showComments() {
        //init arrayList
        commentArrayList = ArrayList()

        //db path to loadComments
        val ref =FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object:ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list
                    commentArrayList.clear()

                    for (ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelComments::class.java)

                        //add to list
                        commentArrayList.add(model!!)

                    }

                    //setup adapter
                    adapterComment = AdapterComment(this@PdfDetailActivity,commentArrayList)
                    binding.commentsRv.adapter = adapterComment

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private var comment = ""

    private fun addCommentDialog(){
        //inflate view for dialog_comment_add.xml
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))

        //setup alert dialog
        val builder = AlertDialog.Builder(this,R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        //create and show alert dialog
        val alertDialog = builder.create()
        alertDialog.show()

        //handle click , dismiss dialog
        commentAddBinding.backBtn.setOnClickListener {alertDialog.dismiss()}

        // handle click add comment
        commentAddBinding.submitBtn.setOnClickListener {
            //GET data
            comment = commentAddBinding.commentEt.text.toString().trim()
            // validate data
            if (comment.isEmpty()){
                Utils.toast(this,"Enter Comment....")
            }else{
                addComment()
            }
        }

    }

    private fun addComment(){
        //show progress
        progressDialog.setMessage("Adding Comment")
        progressDialog.show()

        // timestamp for current id comment timestamp e.t.c
        val timestamp = Utils.getTimestamp()

        //set data to HashMap

        val hashMap = HashMap<String,Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        //Db path to add data into it

        //Books >bookId >> Comments >> Comment Id >> Comment DATA

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child("$timestamp")
            .setValue(hashMap).addOnSuccessListener {
                progressDialog.dismiss()
                Utils.toast(this,"Comment Added Successfully")
            }.addOnFailureListener{e ->
                progressDialog.dismiss()
                Utils.toast(this,"Failed to Comment due to ${e.message} ")
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
                    MyApplication.loadBookImage(
                        bookImageUrl,
                        binding.progressBar,
                        baseContext,
                        binding.booksImageIv
                    )

                    //load pdf size
                    MyApplication.loadPdfSize(bookUrl, "$title", binding.sizeTv)

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
                    Log.d(
                        TAG,
                        "onDataChange: onDataChange: Current Downloads Count $downloadsCount"
                    )

                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }

                    //covert to Long and increment
                    val newDownloadCount: Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "onDataChange: New Download Count $newDownloadCount")

                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadCount

                    //Update Increment downloads Count
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "onDataChange: Download Count increment")
                        }.addOnFailureListener { e ->
                            Log.e(TAG, "onDataChange: Failed to increment due to ${e.message}")
                        }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }



}