package com.pandaapps.abmsstudies.books

import android.app.Activity
import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
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


                        }
                        .onPageChange { page, pageCount ->
                            if (pagesTv != null) {
                                pagesTv.text = "$pageCount"
                                Log.d(TAG, "loadPdfFromUrlSinglePage: Total pages are: $pageCount")
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


        fun loadCategoryPapers(categoryId: String, categoryTv: TextView) {

            //load category using category id from firebase

            val ref = FirebaseDatabase.getInstance().getReference("CategoriesPapers")
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

        fun deleteBook(
            context: Context,
            bookId: String,
            bookUrl: String,
            bookTitle: String,
            bookImageUrl: String
        ) {

            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: deleting...")

            //progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please Wait...")
            progressDialog.setMessage("Deleting $bookTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Deleting from storage...")

            val refForBookImg = FirebaseStorage.getInstance().getReferenceFromUrl(bookImageUrl)
            refForBookImg.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBookImage: Deleted from Storage")

                }
                .addOnFailureListener { e ->
                    Log.d(
                        TAG,
                        "deleteBookImage: Failed to delete due from db due to ${e.message} "
                    )
                }

            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()

                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Deleted from Storage")
                    Log.d(TAG, "deleteBook: Deleting from db now")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()

                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            MotionToast.createColorToast(
                                context as Activity,
                                "Success",
                                "Successfully deleted.... ",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(
                                    context,
                                    www.sanju.motiontoast.R.font.helveticabold
                                )
                            )
                            Log.d(TAG, "deleteBook: Deleted from db")
                        }


                        .addOnFailureListener { e ->
                            Log.d(
                                TAG,
                                "deleteBook: Failed to delete due from db due to ${e.message} "
                            )
                            progressDialog.dismiss()
                            MotionToast.createColorToast(
                                context as Activity,
                                "Failed",
                                "Failed to delete rom db due to ${e.message}",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(
                                    context as Activity,
                                    www.sanju.motiontoast.R.font.helveticabold
                                )
                            )
                        }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "deleteBook: Failed to delete due from storage due to ${e.message} ")
                    progressDialog.dismiss()
                    MotionToast.createColorToast(
                        context as Activity,
                        "Failed",
                        "Failed to delete from Storage due to ${e.message}",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        ResourcesCompat.getFont(
                            context as Activity,
                            www.sanju.motiontoast.R.font.helveticabold
                        )
                    )
                }


        }

        fun deletePaper(
            context: Context,
            paperId: String,
            paperUrl: String,
            paperTitle: String,
            paperImageUrl: String
        ) {


            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: deleting...")

            //progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please Wait...")
            progressDialog.setMessage("Deleting $paperTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Deleting from storage...")

            val refForBookImg = FirebaseStorage.getInstance().getReferenceFromUrl(paperImageUrl)
            refForBookImg.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBookImage: Deleted from Storage")

                }
                .addOnFailureListener { e ->
                    Log.d(
                        TAG,
                        "deleteBookImage: Failed to delete due from db due to ${e.message} "
                    )
                }

            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(paperUrl)
            storageReference.delete()

                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Deleted from Storage")
                    Log.d(TAG, "deleteBook: Deleting from db now")

                    val ref = FirebaseDatabase.getInstance().getReference("Papers")
                    ref.child(paperId)
                        .removeValue()

                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            MotionToast.createColorToast(
                                context as Activity,
                                "Success",
                                "Successfully deleted.... ",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(
                                    context as Activity,
                                    www.sanju.motiontoast.R.font.helveticabold
                                )
                            )
                            Log.d(TAG, "deleteBook: Deleted from db")
                        }


                        .addOnFailureListener { e ->
                            Log.d(
                                TAG,
                                "deleteBook: Failed to delete due from db due to ${e.message} "
                            )
                            progressDialog.dismiss()
                            MotionToast.createColorToast(
                                context as Activity,
                                "Failed",
                                "Failed to delete rom db due to ${e.message}",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.SHORT_DURATION,
                                ResourcesCompat.getFont(
                                    context as Activity,
                                    www.sanju.motiontoast.R.font.helveticabold
                                )
                            )
                        }
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "deleteBook: Failed to delete due from storage due to ${e.message} ")
                    progressDialog.dismiss()
                    MotionToast.createColorToast(
                        context as Activity,
                        "Failed",
                        "Failed to delete from Storage due to ${e.message}",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        ResourcesCompat.getFont(
                            context as Activity,
                            www.sanju.motiontoast.R.font.helveticabold
                        )
                    )
                }


        }

        fun incrementBooksViewCount(bookId: String) {
            //   1):  Get current books viewCount
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount == "" || viewsCount == "null") {
                            viewsCount = "0";
                        }

                        // 2):Increment ViewsCount

                        val newViewsCount = viewsCount.toLong() + 1

                        //setup data to update in db

                        val hashMap = HashMap<String, Any>()

                        hashMap["viewsCount"] = newViewsCount

                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)


                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }

        fun loadBookImage(
            imageUrl: String,
            progressBar: ProgressBar,
            context: Context,
            imageView: ShapeableImageView
        ) {

            val TAG = "BOOK_IMAGE_TAG"

            //using url we can get image
            try {

                Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_gray)
                    .into(imageView)
                imageView.visibility = View.GONE

                android.os.Handler().postDelayed({
                    progressBar.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                }, 2000)


            } catch (e: Exception) {
                Log.e(TAG, "onDataChanged", e)
            }

        }


    }


}