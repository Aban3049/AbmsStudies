package com.pandaapps.abmsstudies.papers.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityAdminPaperBinding
import com.pandaapps.abmsstudies.papers.adapter.AdapterPaperCategoryAdmin
import com.pandaapps.abmsstudies.papers.model.ModelPaperCategory

class AdminPaperActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminPaperBinding

    private lateinit var categoryArrayList: ArrayList<ModelPaperCategory>

    private lateinit var adapterBooksCategoryAdmin: AdapterPaperCategoryAdmin

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAdminPaperBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }

        firebaseAuth = FirebaseAuth.getInstance()

        loadCategories()

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this@AdminPaperActivity, AddCategoryPapersActivity::class.java))
        }
        binding.addPdfFab.setOnClickListener {
            startActivity(Intent(this@AdminPaperActivity, AddPapersActivity::class.java))
        }

        binding.searchEt.addTextChangedListener(object :TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                try {
                    adapterBooksCategoryAdmin.filter.filter(s)
                }catch (e:Exception){
                    Utils.toast(this@AdminPaperActivity,"Failed to search due to ${e.message}")
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


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
                   AdapterPaperCategoryAdmin(this@AdminPaperActivity, categoryArrayList)

                // set adapter to recyclerView
                binding.categoriesRv.adapter = adapterBooksCategoryAdmin


            }

            override fun onCancelled(error: DatabaseError) {

            }

        })


    }


}