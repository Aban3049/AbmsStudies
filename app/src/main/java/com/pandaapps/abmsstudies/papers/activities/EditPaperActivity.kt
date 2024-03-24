package com.pandaapps.abmsstudies.papers.activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.books.activities.PdfEditActivity
import com.pandaapps.abmsstudies.databinding.ActivityEditPaperBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class EditPaperActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditPaperBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var categoryTitleArrayList: ArrayList<String>

    private lateinit var categoryIdArrayList: ArrayList<String>

    private companion object {
        private const val TAG = "PAPER_EDIT"
    }

    //book Id from intent started from AdapterPdfAdmin

    private var paperId = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityEditPaperBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        paperId = intent.getStringExtra("paperId")!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait....")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadPaperInfo()

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }


    private var title = ""


    private fun validateData() {

        //get data
        title = binding.titleEt.text.toString().trim()

        //validate data
        if (title.isEmpty()) {
            binding.titleEt.requestFocus()
            binding.titleEt.error = "Enter Title"
        }
         else if (selectedCategoryId.isEmpty()) {
            MotionToast.createColorToast(
                this,
                "Warning",
                "Select Category",
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
            )
        }
        else{
            updatePdf()
        }

    }

    private fun loadPaperInfo() {
        Log.d(TAG, "loadPaperInfo: Loading Paper Info")

        val ref = FirebaseDatabase.getInstance().getReference("Papers")
        ref.child(paperId)
            .addValueEventListener(object:ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book info
                    selectedCategoryId = snapshot.child("categoryId").value as String
                    val title = snapshot.child("title").value as String

                    //set to views
                    binding.titleEt.setText(title)

                    //load category info using category Id
                    Log.d(TAG, "onDataChange: Loading book category info")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("CategoriesPapers")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object:ValueEventListener{

                            override fun onDataChange(snapshot: DataSnapshot) {
                                // get category
                                val category = snapshot.child("category").value

                                //set to textView
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""
    private fun loadCategories() {
        Log.d(TAG, "loadCategories: loading categories:")

        categoryIdArrayList = ArrayList()
        categoryTitleArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("CategoriesPapers")
        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // clear list before adding data

                categoryTitleArrayList.clear()
                categoryIdArrayList.clear()

                for (ds in snapshot.children) {

                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: Category Id :$id")
                    Log.d(TAG, "onDataChange: Category: $category")

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

    private fun updatePdf(){
        Log.d(TAG, "updatePaper: Starting updating paper info....")

        // show progress
        progressDialog.setMessage("Updating papers info")
        progressDialog.show()

        val hashMap = HashMap<String,Any>()
        hashMap ["title"] = "$title"
        hashMap["categoryId"] = "$selectedCategoryId"

        // start updating
        val ref = FirebaseDatabase.getInstance().getReference("Papers")
        ref.child(paperId)
            .updateChildren(hashMap)

            .addOnSuccessListener {

                progressDialog.dismiss()
                Log.d(TAG, "updatePaper: Update Successfully...")
                MotionToast.createColorToast(
                    this,
                    "Success",
                    "Updated Successfully",
                    MotionToastStyle.SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )
            }

            .addOnFailureListener {e ->
                Log.d(TAG, "updatePdf:Failed to Update due to ${e.message} ")
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this,
                    "Error",
                    "Failed to Update due to ${e.message}",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )

            }

    }



    private fun categoryDialog() {

        val categoryArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoryArray[i] = categoryTitleArrayList[i]
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category")
            .setItems(categoryArray) { _, position ->

                //handle clicks and saved clicked category id and title
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                //set  textView

                binding.categoryTv.text = selectedCategoryTitle
            }
            .show() // show dialog

    }

}