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
import com.pandaapps.abmsstudies.papers.activities.EditPaperActivity
import com.pandaapps.abmsstudies.papers.activities.PaperDetailActivity
import com.pandaapps.abmsstudies.papers.filter.FilterAdminPaperPdf
import com.pandaapps.abmsstudies.papers.model.ModelPaperPdf

class adapterAdminPaperPdf:RecyclerView.Adapter<adapterAdminPaperPdf.HolderAdminPdf>,Filterable {

    private lateinit var binding: RowPdfPaperBinding

    private var context: Context

    private var filterList:ArrayList<ModelPaperPdf>

    private var filter:FilterAdminPaperPdf?=null

    //array list to hold pdfs
    var pdfArrayList: ArrayList<ModelPaperPdf>

    constructor(context: Context, pdfArrayList: ArrayList<ModelPaperPdf>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAdminPdf {
        binding = RowPdfPaperBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderAdminPdf(binding.root)
    }


    override fun onBindViewHolder(holder: HolderAdminPdf, position: Int) {


        //get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val pdfUrl = model.url
        val timestamp = model.timestamp
        val imageUrl = model.imageUrl

      holder.moreBtn.visibility=View.VISIBLE

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

        // handle clicks show options Edit Book, Delete Books
        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }
        // handel item click show pdf Detail Activity
        holder.itemView.setOnClickListener {
            //intent with book id
            val intent = Intent(context, PaperDetailActivity::class.java)
            intent.putExtra("paperId",pdfId)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
      return  pdfArrayList.size
    }


    private fun moreOptionsDialog(model: ModelPaperPdf, holder: adapterAdminPaperPdf.HolderAdminPdf) {

        //get id,url ,title of the book
        val paperId = model.id
        val paperUrl = model.url
        val paperTitle = model.title
        val paperImageUrl = model.imageUrl

        // options to show in Dialog

        val options = arrayOf("Edit", "Delete")

        // alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options) { _, position ->

                if (position == 0) {

                    //Edit is Clicked
                  val intent = Intent(context, EditPaperActivity::class.java)
                    intent.putExtra("paperId",paperId) // passed bookId used to edit book
                    context.startActivity(intent)

                } else if (position == 1) {
                    //delete is  is clicked
                    //show confirmation dialog
                    MyApplication.deletePaper(context, paperId, paperUrl,paperTitle, paperImageUrl)
                }

            }
            .show()

    }
    inner class HolderAdminPdf (itemView: View) : RecyclerView.ViewHolder(itemView) {

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
            filter = FilterAdminPaperPdf(filterList, this)
        }

        return filter as FilterAdminPaperPdf
    }


}