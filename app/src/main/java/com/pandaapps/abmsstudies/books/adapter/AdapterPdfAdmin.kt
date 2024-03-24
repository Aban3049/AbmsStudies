package com.pandaapps.abmsstudies.books.adapter

import android.app.AlertDialog
import com.pandaapps.abmsstudies.books.MyApplication
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.pandaapps.abmsstudies.books.activities.PdfDetailActivity
import com.pandaapps.abmsstudies.books.filter.FilterPdfAdmin
import com.pandaapps.abmsstudies.books.activities.PdfEditActivity
import com.pandaapps.abmsstudies.databinding.RowPdfAdminBinding
import com.pandaapps.abmsstudies.books.model.ModelBookPdf

class AdapterPdfAdmin : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable {

    private lateinit var binding: RowPdfAdminBinding

    private var context: Context

    //array list to hold pdfs
  var pdfArrayList: ArrayList<ModelBookPdf>

    private val filterList: ArrayList<ModelBookPdf>

    //filter Object
    private var filter: FilterPdfAdmin? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelBookPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {

        //bind inflate layout row_admin
        binding = RowPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfAdmin(binding.root)

    }


    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {
        //Get data and Set Data, Handle Clicks

        //get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp
        val imageUrl = model.imageUrl

        //covet time stamp to dd/MM/yyyy format

        val formattedDate = MyApplication.formatTimestampDate(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        //load further detail lik category pdf from url ,pdf size

        //category Id
        MyApplication.loadCategory(categoryId, holder.categoryTv)



        MyApplication.loadBookImage(imageUrl,holder.progressBar,context,holder.booksIv)

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        // handle clicks show options Edit Book, Delete Books
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }


        //we don't need page number here, pass as null
//        MyApplication.loadPdfFromUrlSinglePage(
//            pdfUrl,
//            title,
//            holder.pdfView,
//            holder.progressBar,
//            null
//        )



        // handel item click show pdf Detail Activity
        holder.itemView.setOnClickListener {
            //intent with book id
            val intent = Intent(context,PdfDetailActivity::class.java)
            intent.putExtra("bookId",pdfId)
            context.startActivity(intent)
        }


    }

    private fun moreOptionsDialog(model: ModelBookPdf, holder: HolderPdfAdmin) {

        //get id,url ,title of the book
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title
        val imageUrl = model.imageUrl

        // options to show in Dialog

        val options = arrayOf("Edit", "Delete")

        // alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options) { _, position ->

                if (position ==0){

                    //Edit is Clicked
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId",bookId) // passed bookId used to edit book
                    context.startActivity(intent)

                }else if (position==1){
                    //delete is  is clicked
                    //show confirmation dialog
                    MyApplication.deleteBook(context,bookId,bookUrl, bookTitle, imageUrl)
                }

            }
            .show()

    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }


    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfAdmin(filterList, this)

        }

        return filter as FilterPdfAdmin

    }

    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //UI Views of row_pdf_admin.xml

//        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
        val booksIv = binding.booksIv

    }

}