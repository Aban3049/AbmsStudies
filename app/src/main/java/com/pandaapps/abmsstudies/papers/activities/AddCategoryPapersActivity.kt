package com.pandaapps.abmsstudies.papers.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityAddCategoryPapersBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class AddCategoryPapersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCategoryPapersBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityAddCategoryPapersBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setMessage("Adding Category")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private var category = ""

    private fun validateData() {

        category = binding.categoryEt.text.toString().trim()

        //validate data

        if (category.isEmpty()) {

            MotionToast.createColorToast(
                this@AddCategoryPapersActivity,
                "Error",
                "Field cannot be Empty",
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(
                    this@AddCategoryPapersActivity,
                    www.sanju.motiontoast.R.font.helvetica_regular
                )
            )
        } else {
            addCategoryFirebase()
        }


    }

    private fun addCategoryFirebase() {

        progressDialog.show()

        val timestamp = Utils.getTimestamp()

        //Adding data in firebase Database

        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        val ref = FirebaseDatabase.getInstance().getReference("CategoriesPapers")
        ref.child("$timestamp")
            .setValue(hashMap)

            .addOnSuccessListener {

                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@AddCategoryPapersActivity,
                    "Successfully",
                    "Added Category successfully",
                    MotionToastStyle.SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(
                        this@AddCategoryPapersActivity,
                        www.sanju.motiontoast.R.font.helvetica_regular
                    )
                )

            }
            .addOnFailureListener { e ->

                progressDialog.dismiss()

                MotionToast.createColorToast(
                    this@AddCategoryPapersActivity,
                    "Failed",
                    "Failed to add due to ${e.message}",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(
                        this@AddCategoryPapersActivity,
                        www.sanju.motiontoast.R.font.helvetica_regular
                    )
                )

            }


    }
}