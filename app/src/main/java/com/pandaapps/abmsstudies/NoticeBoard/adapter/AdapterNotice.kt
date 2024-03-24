package com.pandaapps.abmsstudies.NoticeBoard.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.pandaapps.abmsstudies.NoticeBoard.FilterNoticeBoard
import com.pandaapps.abmsstudies.NoticeBoard.activities.NoticeDetailActivity
import com.pandaapps.abmsstudies.NoticeBoard.models.modelNotice
import com.pandaapps.abmsstudies.books.MyApplication
import com.pandaapps.abmsstudies.databinding.RowNoticeBinding

class AdapterNotice : RecyclerView.Adapter<AdapterNotice.HolderNotice>, Filterable {

    private var context: Context

    var noticeArrayList: ArrayList<modelNotice>

    private var filterList: ArrayList<modelNotice>

    private var filter: FilterNoticeBoard? = null

    private lateinit var binding: RowNoticeBinding

    constructor(
        context: Context,
        noticeArrayList: ArrayList<modelNotice>,
    ) : super() {
        this.context = context
        this.noticeArrayList = noticeArrayList
        this.filterList = noticeArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderNotice {
        binding = RowNoticeBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderNotice(binding.root)
    }

    override fun onBindViewHolder(holder: HolderNotice, position: Int) {


        //get data and set Data

        //get data
        val model = noticeArrayList[position]
        val noticeId = model.id
        val title = model.title
        val description = model.description
        model.uid
        model.url
        val timestamp = model.timestamp
        val imageUrl = model.imageUrl

        //convert time

        val date = MyApplication.formatTimestampDate(timestamp)

        //set data

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date

//        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)

        MyApplication.loadBookImage(imageUrl, binding.progressBar, context, binding.noticeImageIv)



        holder.itemView.setOnClickListener {
            //pass book id in intent
            val intent = Intent(context, NoticeDetailActivity::class.java)
            intent.putExtra("noticeId", noticeId)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return noticeArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterNoticeBoard(filterList, this)
        }

        return filter as FilterNoticeBoard
    }


    inner class HolderNotice(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var progressBar = binding.progressBar
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var dateTv = binding.dateTv

    }


}

