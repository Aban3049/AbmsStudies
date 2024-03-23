package com.pandaapps.abmsstudies.books.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.books.adapter.AdapterBooksCategoryAdmin
import com.pandaapps.abmsstudies.databinding.ActivityBooksAdminDashboardBinding
import com.pandaapps.abmsstudies.books.model.ModelBooksCategoryAdmin

class BooksAdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBooksAdminDashboardBinding

    private lateinit var categoryArrayList: ArrayList<ModelBooksCategoryAdmin>

    private lateinit var adapterBooksCategoryAdmin: AdapterBooksCategoryAdmin

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityBooksAdminDashboardBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadCategories()

        binding.searchEt.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                // call the filter when user start writing

                try {

                    adapterBooksCategoryAdmin.filter.filter(s)

                } catch (e: Exception) {
                    Utils.toast(this@BooksAdminDashboardActivity,"Failed to search due to ${e.message}")
                }


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.addCategoryBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@BooksAdminDashboardActivity,
                    AddCategoryBooksActivity::class.java
                )
            )
        }

        binding.addPdfFab.setOnClickListener {

            startActivity(
                Intent(
                    this@BooksAdminDashboardActivity,
                    PdfAddActivityBooks::class.java
                )
            )
        }

        binding.logOutBtn.setOnClickListener {
            firebaseAuth.signOut()
        }
        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun loadCategories() {

        // init arrayList

        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("CategoriesBooks")


        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                // clearing the list before adding data
                categoryArrayList.clear()

                for (ds in snapshot.children) {
                    //get data as model

                    val model = ds.getValue(ModelBooksCategoryAdmin::class.java)

                    //add to array list

                    categoryArrayList.add(model!!)

                }

                //set adapter
                adapterBooksCategoryAdmin =
                    AdapterBooksCategoryAdmin(this@BooksAdminDashboardActivity, categoryArrayList)

                // set adapter to recyclerView
                binding.categoriesRv.adapter = adapterBooksCategoryAdmin


            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

}