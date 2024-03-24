package com.pandaapps.abmsstudies.books.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityPdfViewBinding


class PdfViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewBinding


    private companion object {
        private const val TAG = "PDF_VIEW_TAG"
    }

    private var bookId = ""
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityPdfViewBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        loadBookDetails()


    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Get pdf URL from db")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book url
                    val pdfUrl = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: PDF_URL: $pdfUrl")




                    loadBookFromUrl("$pdfUrl")


                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }

    private fun loadBookFromUrl(pdfUrl: String) {
        Log.d(TAG, "loadBookFromUrl: GET PDF from firebase Storage using url")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Utils.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "loadBookFromUrl: pdf got from url $pdfUrl")


                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        //set current and total pages toolbar subtitle
                        val currentPage = page + 1 // page starts from 0 so we add 1
                        binding.toolbarSubtitleTv.text =
                            "$currentPage/$pageCount" //e.g 3/332
                        Log.d(TAG, "loadBookFromUrl: $currentPage/$pageCount")
                    }
                    .onError { t ->
                        Log.e(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Utils.toast(this, "Error at page: $page")
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }

                    .onError { error ->
                        error.message?.let {
                            Utils.toast(this, it)
                        }

                    }
                    .load()

                binding.progressBar.visibility = View.GONE


                //load pdf


            }
            .addOnFailureListener { e ->
                Log.e(TAG, "loadBookFromUrl: Failed to get url due to ${e.message}")
                binding.progressBar.visibility = View.GONE
            }

    }


}


