package com.pandaapps.abmsstudies.papers.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.pandaapps.abmsstudies.books.MyApplication


import com.pandaapps.abmsstudies.databinding.RowPdfPaperBinding
import com.pandaapps.abmsstudies.papers.activities.PaperDetailActivity
import com.pandaapps.abmsstudies.papers.filter.FilterPaperPdfUser
import com.pandaapps.abmsstudies.papers.model.ModelPaperPdf

class AdapterPaperPdf : RecyclerView.Adapter<AdapterPaperPdf.HolderPdf>, Filterable {

    private lateinit var binding: RowPdfPaperBinding

    private var context: Context

    //array list to hold pdfs
    var pdfArrayList: ArrayList<ModelPaperPdf>

    private var filter: FilterPaperPdfUser? = null

    private var filterList: ArrayList<ModelPaperPdf>

    // private val filterList: ArrayList<ModelBookPdf>

    //filter Object
    // private var filter: FilterPdfAdmin? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelPaperPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdf {

        binding = RowPdfPaperBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdf(binding.root)

    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPdf, position: Int) {

        //Get data and Set Data, Handle Clicks

        //get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val pdfUrl = model.url
        val timestamp = model.timestamp
        val imageUrl = model.imageUrl

        //covet time stamp to dd/MM/yyyy format

        val formattedDate = MyApplication.formatTimestampDate(timestamp)

        //set data
        holder.titleTv.text = title
        holder.dateTv.text = formattedDate

        //load further detail lik category pdf from url ,pdf size

        //category Id
        MyApplication.loadCategoryPapers(categoryId, holder.categoryTv)



        MyApplication.loadBookImage(imageUrl, holder.progressBar, context, holder.booksIv)

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        holder.itemView.setOnClickListener {
            //intent with book id
            val intent = Intent(context, PaperDetailActivity::class.java)
            intent.putExtra("paperId", pdfId)
            context.startActivity(intent)
        }

        // handle clicks show options Edit Book, Delete Books
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }


    }

    private fun moreOptionsDialog(model: ModelPaperPdf, holder: HolderPdf) {

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

                if (position == 0) {

                } else if (position == 1) {
                    //delete is  is clicked
                    //show confirmation dialog
                    MyApplication.deletePaper(context, bookId, bookUrl, bookTitle, imageUrl)
                }

            }
            .show()

    }


    inner class HolderPdf(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //UI Views of row_pdf_admin.xml

        //        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn
        val booksIv = binding.paperImageIv

    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPaperPdfUser(filterList, this)
        }

        return filter as FilterPaperPdfUser


    }


}