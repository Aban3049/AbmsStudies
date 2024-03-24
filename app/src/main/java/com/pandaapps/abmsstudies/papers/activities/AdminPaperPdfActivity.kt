package com.pandaapps.abmsstudies.papers.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityAdminPaperPdfBinding
import com.pandaapps.abmsstudies.papers.adapter.adapterAdminPaperPdf
import com.pandaapps.abmsstudies.papers.model.ModelPaperPdf

class AdminPaperPdfActivity : AppCompatActivity() {

    private lateinit var binding:ActivityAdminPaperPdfBinding


    private var categoryId: String = ""
    private var category = ""

    //arrayList to hold Books
    private lateinit var pdfArrayList: ArrayList<ModelPaperPdf>

    private lateinit var adapterPdfAdmin:adapterAdminPaperPdf

    private companion object {
        private const val TAG = "PAPER_PDF_ADMIN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding=ActivityAdminPaperPdfBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



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
                    adapterPdfAdmin.filter.filter(s)
                }catch (e:Exception){
                    Utils.toast(this@AdminPaperPdfActivity,"Failed to Search due to ${e.message}")
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
                        Log.d(TAG,
                            "onDataChange: ${model.title} ${model.categoryId} "
                        )
                    }

                    //setup adapter
                   adapterPdfAdmin = adapterAdminPaperPdf(this@AdminPaperPdfActivity, pdfArrayList)
                    binding.booksRv.adapter = adapterPdfAdmin




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