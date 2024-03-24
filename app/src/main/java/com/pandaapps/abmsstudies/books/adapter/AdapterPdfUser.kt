package com.pandaapps.abmsstudies.books.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.pandaapps.abmsstudies.books.MyApplication
import com.pandaapps.abmsstudies.books.activities.PdfDetailActivity
import com.pandaapps.abmsstudies.books.filter.FilterPdfUser
import com.pandaapps.abmsstudies.books.model.ModelBookPdf
import com.pandaapps.abmsstudies.databinding.RowPdfUserBinding

class AdapterPdfUser : RecyclerView.Adapter<AdapterPdfUser.HolderPdfUser>, Filterable {

    private var context: Context

    var pdfArrayList: ArrayList<ModelBookPdf>

    //array list to hold filtered data
    private var filteredList: ArrayList<ModelBookPdf>

    private lateinit var binding: RowPdfUserBinding

    private var filter: FilterPdfUser? = null

    constructor(context: Context, pdfArrayList: ArrayList<ModelBookPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filteredList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {

        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfUser(binding.root)

    }

    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {

        //get data and set Data

        //get data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp
        val imageUrl = model.imageUrl

        //convert time

        val date = MyApplication.formatTimestampDate(timestamp)

        //set data

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date

//        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)

        MyApplication.loadBookImage(imageUrl,binding.progressBar,context,binding.booksImageIv)

        MyApplication.loadCategory(categoryId, holder.categoryTv)

        MyApplication.loadPdfSize(url, title, holder.sizeTv)

        //handle click,open pdf detail

        holder.itemView.setOnClickListener {
            //pass book id in intent
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return pdfArrayList.size // return list size number of records
    }


    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterPdfUser(filteredList, this)
        }
        return filter as FilterPdfUser
    }

    inner class HolderPdfUser(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var sizeTv = binding.sizeTv
        var dateTv = binding.dateTv

    }


}