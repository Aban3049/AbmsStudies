package com.pandaapps.abmsstudies.books.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.books.adapter.AdapterPdfAdmin
import com.pandaapps.abmsstudies.databinding.ActivityBookPdfAdminBinding
import com.pandaapps.abmsstudies.books.model.ModelBookPdf


class BookPdfAdminActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "BOOKS_PDF_ADMIN"
    }

    //category id, title

    private var categoryId: String = ""
    private var category = ""

    //arrayList to hold Books
    private lateinit var pdfArrayList: ArrayList<ModelBookPdf>

    //adapter

    private lateinit var adapterPdfAdmin: AdapterPdfAdmin

    private lateinit var binding: ActivityBookPdfAdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityBookPdfAdminBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //get from intent, that we passed from adapter

        val intent = intent

        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!


        //set pdf category

        binding.subTitleTv.text = category



        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        //load pdf/books
        loadPdfList()

        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                //filter data

                try {

                    adapterPdfAdmin.filter.filter(s)
                } catch (e: Exception) {
                    Log.e(TAG, "onTextChanged: ${e.message}")
                }

            }

            override fun afterTextChanged(s: Editable?) {
                //filter data
            }


        })


    }

    private fun loadPdfList() {

        //init arrayList

        Log.d(TAG, "loadPdfList: ")

        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    //clear list before setting data to it
                    pdfArrayList.clear()

                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(ModelBookPdf::class.java)
                        //add to list
                        pdfArrayList.add(model!!)
                        Log.d(TAG, "onDataChange: ${model.title} ${model.categoryId} ")
                    }

                    //setup adapter
                    adapterPdfAdmin = AdapterPdfAdmin(this@BookPdfAdminActivity, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfAdmin


                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: DatabaseError: $error")
                }

            })


    }


}