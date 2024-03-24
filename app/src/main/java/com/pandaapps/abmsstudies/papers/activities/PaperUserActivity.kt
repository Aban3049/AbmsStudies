package com.pandaapps.abmsstudies.papers.activities


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
import com.pandaapps.abmsstudies.databinding.ActivityPaperAdminBinding
import com.pandaapps.abmsstudies.papers.adapter.AdapterPaperCategory
import com.pandaapps.abmsstudies.papers.model.ModelPaperCategory

class PaperUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaperAdminBinding

    private lateinit var categoryArrayList: ArrayList<ModelPaperCategory>

    private lateinit var adapterBooksCategoryAdmin: AdapterPaperCategory

    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityPaperAdminBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.toolbarBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.searchEt.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                try {
                    adapterBooksCategoryAdmin.filter.filter(s)
                } catch (e: Exception) {
                    Utils.toast(this@PaperUserActivity, "Failed to search due to ${e.message}")
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


        loadCategories()


    }


    private fun loadCategories() {

        // init arrayList

        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("CategoriesPapers")


        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                // clearing the list before adding data
                categoryArrayList.clear()

                for (ds in snapshot.children) {
                    //get data as model

                    val model = ds.getValue(ModelPaperCategory::class.java)

                    //add to array list

                    categoryArrayList.add(model!!)

                }

                //set adapter
                adapterBooksCategoryAdmin =
                    AdapterPaperCategory(this@PaperUserActivity, categoryArrayList)

                // set adapter to recyclerView
                binding.categoriesRv.adapter = adapterBooksCategoryAdmin


            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }

}

