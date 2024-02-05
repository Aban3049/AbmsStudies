package com.pandaapps.abmsstudies.adapters

import android.app.AlertDialog
import android.app.Application
import com.pandaapps.abmsstudies.MyApplication
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.pandaapps.abmsstudies.FilterPdfAdmin
import com.pandaapps.abmsstudies.databinding.RowPdfAdminBinding
import com.pandaapps.abmsstudies.models.ModelBookPdf

class AdapterPdfAdmin : RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin>, Filterable {

    private lateinit var binding: RowPdfAdminBinding

    private var context: Context

    //array list to hold pdfs
    public var pdfArrayList: ArrayList<ModelBookPdf>

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

        //covet time stamp to dd/MM/yyyy format

        val formattedDate = MyApplication.formatTimestampDate(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = formattedDate

        //load further detail lik category pdf from url ,pdf size

        //category Id
        MyApplication.loadCategory(categoryId, holder.categoryTv)

        //we don't need page number here, pass as null
        MyApplication.loadPdfFromUrlSinglePage(
            pdfUrl,
            title,
            holder.pdfView,
            holder.progressBar,
            null
        )

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.sizeTv)

        // handle clicks show options Edit Book, Delte Books
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }


    }

    private fun moreOptionsDialog(model: ModelBookPdf, holder: AdapterPdfAdmin.HolderPdfAdmin) {

        //get id,url ,title of the book
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        // options to show in Dialog

        val options = arrayOf("Edit", "Delete")

        // alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options) { dialog, position ->

                if (position ==0){

                    //Edit is Clicked

                }else if (position==1){
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

        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val titleTv = binding.titleTv
        val descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        val sizeTv = binding.sizeTv
        val dateTv = binding.dateTv
        val moreBtn = binding.moreBtn

    }

}