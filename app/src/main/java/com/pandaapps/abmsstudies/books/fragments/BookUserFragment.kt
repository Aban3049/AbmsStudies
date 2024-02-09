package com.pandaapps.abmsstudies.books.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.books.adapter.AdapterPdfUser
import com.pandaapps.abmsstudies.books.model.ModelBookPdf
import com.pandaapps.abmsstudies.databinding.FragmentBookUserBinding


class BookUserFragment : Fragment {

    private lateinit var binding: FragmentBookUserBinding

    private lateinit var mContext: Context

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<ModelBookPdf>
    private lateinit var adapterPdfUser: AdapterPdfUser

    constructor()


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        private const val TAG = "BOOK_USER_FRAGMENT_TAG"

        //receive data from Activity to Load

        fun newInstance(
            categoryId: String,
            category: String,
            uid: String
        ): BookUserFragment {


            val fragment = BookUserFragment()
            //put data to bundle intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)

            fragment.arguments = args

            return fragment

        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //get arguments that we passed in newInstance method

        val arguments = arguments

        if (arguments != null) {
            categoryId = arguments.getString("categoryId")!!
            category = arguments.getString("category")!!
            uid = arguments.getString("uid")!!

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookUserBinding.inflate(LayoutInflater.from(mContext), container, false)

        //load pdf category to this fragment category which have new instance to load each pdf category
        Log.d(TAG, "onCreateView: Category: $category")

        if (category == "All") {
            //load all books
            loadAllBooks()
        } else if (category == "Most Viewed") {
            //load most viewed books
            loadMostViewedDownloadedBooks("viewCount")
        } else if (category == "Most Download") {
            //load most downloaded books
            loadMostViewedDownloadedBooks("downloadsCount")
        } else {
            //load selected category
            loadCategorizedBooks()
        }

        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                try {

                    adapterPdfUser.filter.filter(s)

                } catch (e: Exception) {
                    Log.d(TAG, "onTextChanged: Search Exception: ${e.message}")
                }

            }

            override fun afterTextChanged(s: Editable?) {

            }

        })


        return binding.root
    }

    private fun loadAllBooks() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")

        ref.addValueEventListener(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()

                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelBookPdf::class.java)

                    //add to list
                    pdfArrayList.add(model!!)
                }

                adapterPdfUser  = AdapterPdfUser(mContext,pdfArrayList)

                //set adapter to recyclerView

                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")

        ref.orderByChild(orderBy)
            .limitToLast(10) // load most viewed or most downloaded books . order by ="
            .addValueEventListener(object :ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()

                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(ModelBookPdf::class.java)

                    //add to list
                    pdfArrayList.add(model!!)
                }

                adapterPdfUser  = AdapterPdfUser(mContext,pdfArrayList)

                //set adapter to recyclerView

                binding.booksRv.adapter = adapterPdfUser
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    private fun loadCategorizedBooks() {
        //init list
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")

        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object :ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()

                    for (ds in snapshot.children){
                        //get data
                        val model = ds.getValue(ModelBookPdf::class.java)

                        //add to list
                        pdfArrayList.add(model!!)
                    }

                    adapterPdfUser  = AdapterPdfUser(mContext,pdfArrayList)

                    //set adapter to recyclerView

                    binding.booksRv.adapter = adapterPdfUser
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


}