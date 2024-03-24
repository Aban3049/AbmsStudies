package com.pandaapps.abmsstudies.papers.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityPaperPdfBinding
import com.pandaapps.abmsstudies.papers.adapter.AdapterPaperPdf
import com.pandaapps.abmsstudies.papers.model.ModelPaperPdf

class PaperPdfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaperPdfBinding

    private var categoryId: String = ""
    private var category = ""

    //arrayList to hold Books
    private lateinit var pdfArrayList: ArrayList<ModelPaperPdf>

    private lateinit var adapterPdfUser: AdapterPaperPdf

    private companion object {
        private const val TAG = "PAPER_PDF"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityPaperPdfBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //get from intent, that we passed from adapter

        val intent = intent

        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        binding.subTitleTv.text = category



        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.searchEt.addTextChangedListener(object:TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                try {
                    adapterPdfUser.filter.filter(s)
                }catch (e:Exception){
                    Utils.toast(this@PaperPdfActivity,"Failed to search due to ${e.message}")
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


        //load pdfPapers/books
        loadPdfList()

    }

    private fun loadPdfList() {

        //init arrayList

        Log.d(TAG, "loadPdfList: ")

        pdfArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Papers")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    //clear list before setting data to it
                    pdfArrayList.clear()

                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(ModelPaperPdf::class.java)
                        //add to list
                        pdfArrayList.add(model!!)
                        Log.d(
                            TAG,
                            "onDataChange: ${model.title} ${model.categoryId} "
                        )
                    }

                    //setup adapter
                    adapterPdfUser = AdapterPaperPdf(this@PaperPdfActivity, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfUser




                    if (binding.booksRv.adapter == null) {
                        Log.e(TAG, "onDataChange: null layout")
                    } else {
                        Log.d(TAG, "onDataChange: Both are okay")
                    }


                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: DatabaseError: $error")
                }

            })


    }


}