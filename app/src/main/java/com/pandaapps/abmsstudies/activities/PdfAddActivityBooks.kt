package com.pandaapps.abmsstudies.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityPdfAddBooksBinding
import com.pandaapps.abmsstudies.models.ModelBooksCategoryAdmin
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class PdfAddActivityBooks : AppCompatActivity() {

    private lateinit var binding: ActivityPdfAddBooksBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

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




    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData(){

        Log.d(TAG, "validateData: validating data")

        title=binding.titleEt.text.toString().trim()
        description=binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        if (title.isEmpty()){
            binding.titleEt.requestFocus()
            binding.titleEt.error="Field Cannot be empty"
        }
        else if (description.isEmpty()){
            binding.descriptionEt.requestFocus()
            binding.descriptionEt.error = "Field cannot be empty"
        }
        else if (category.isEmpty()){
            binding.titleEt.requestFocus()
            binding.categoryTv.error="Pick Category"
        }

        else if (pdfUri == null){
            MotionToast.createColorToast(
                this@PdfAddActivityBooks,
                "Warning",
                "Pick PDF",
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
            )
        }

        else{
            //data validate begin upload
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

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->

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

    )

    private fun uploadPdfToStorage(){

        Log.d(TAG, "uploadPdfToStorage: uploading to storage....")

        //show progress dialog
        progressDialog.setMessage("Uploading PDF...")
        progressDialog.show()

        //timestamp in
        val timestamp = Utils.getTimestamp()

        //path f pdf in firebase storage

        val filePathAndName ="Books/$timestamp"

        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot ->

                Log.d(TAG, "uploadPdfToStorage: pdf uploaded now getting url ...")

                //get url of uploaded PDF
                val uriTask:Task<Uri> =taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                    val uploadPdfUrl = "${uriTask.result}"
                    uploadPdfInfoToDb(uploadPdfUrl,timestamp)




            }
            .addOnFailureListener {e ->

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
            .addOnProgressListener {it ->

                val dataTransferred =(100 * it.bytesTransferred/it.totalByteCount)
                progressDialog.setMessage("Uploading $dataTransferred%")
            }

    }

    private fun uploadPdfInfoToDb(uploadPdfUrl: String, timestamp: Long) {
// upload pdf to firebase db
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading pdf info....")

        //uid of  current user
        val uid = firebaseAuth.uid

        val hashMap:HashMap<String,Any> = HashMap()


        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"
        hashMap["url"] = "$uploadPdfUrl"
        hashMap["timestamp"] = timestamp
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
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold))

                pdfUri= null

            }

            .addOnFailureListener {e ->
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
            .setItems(categoriesArray) { dialog, which ->

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





}