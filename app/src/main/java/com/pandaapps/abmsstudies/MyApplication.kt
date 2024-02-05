package com.pandaapps.abmsstudies

import android.app.Application
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {


        fun formatTimestampDate(timestamp: Long): String {
            val calendar = Calendar.getInstance(Locale.ENGLISH)
            calendar.timeInMillis = timestamp

            return DateFormat.format("dd/MM/yyyy", calendar).toString()

        }

        //Function to get PDF size

        fun loadPdfSize(pdfUrl: String, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"

            //using url we can get file and its
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener { storageMetaData ->

                    Log.d(TAG, "loadPdfSize: got metaData")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    //convert bytes to KB/MB
                    val kb = bytes / 1024
                    val mb = kb / 1024

                    if (mb > 1) {
                        sizeTv.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb > 1) {
                        sizeTv.text = "${String.format("%.2f", kb)} KB"
                    } else {
                        sizeTv.text = "${String.format("%.2f", bytes)} bytes"
                    }

                }
                .addOnFailureListener { e ->
                    //failed to get metaData
                    Log.e(TAG, "loadPdfSize: Failed to get meta data due to ${e.message} ")
                }

        }

        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            pagesTv: TextView?
        ) {

            val TAG = "PDF_THUMBNAIL_TAG"
            // using url we can get its file and meta data

            //using url we can get file and its
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Utils.MAX_BYTES_PDF)
                .addOnSuccessListener { bytes ->

                    Log.d(TAG, "loadPdfSize: Size Bytes $bytes")

                    //SET TO PDFVIEW
                    pdfView.fromBytes(bytes)
                        .pages(0) // show first Page
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t ->

                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")

                        }
                        .onPageError { page, t ->

                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")

                        }
                        .onLoad { nbPages ->
                            Log.d(TAG, "loadPdfFromUrlSinglePage: Pages: $nbPages")
                            //pdf loaded , we can set page count pdf Thumbnail
                            progressBar.visibility = View.INVISIBLE

                            //if pagesTv param is not null set page numbers

                            if (pagesTv != null ) {
                                pagesTv.text = "$nbPages"
                            }

                        }
                        .load()


                }
                .addOnFailureListener { e ->
                    //failed to get metaData
                    Log.e(TAG, "loadPdfSize: Failed to get meta data due to ${e.message} ")
                }

        }


        fun loadCategory(categoryId: String, categoryTv: TextView) {

            //load category using category id from firebase

            val ref = FirebaseDatabase.getInstance().getReference("CategoriesBooks")
            ref.child(categoryId)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category

                        val category: String = "${snapshot.child("category").value}"

                        //set Category
                        categoryTv.text = category

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

        }

    }


}