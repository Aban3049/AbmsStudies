package com.pandaapps.abmsstudies.papers.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pandaapps.abmsstudies.databinding.RowCategoryPaperAdminBinding
import com.pandaapps.abmsstudies.papers.activities.PaperPdfActivity
import com.pandaapps.abmsstudies.papers.filter.FilterPaperCategoryUser
import com.pandaapps.abmsstudies.papers.model.ModelPaperCategory

class AdapterPaperCategory : RecyclerView.Adapter<AdapterPaperCategory.HolderCategory>, Filterable {

    private val context: Context


    var categoryArrayList: ArrayList<ModelPaperCategory>


    private var filterList: ArrayList<ModelPaperCategory>

    private var filter: FilterPaperCategoryUser? = null


    private lateinit var binding: RowCategoryPaperAdminBinding

    constructor(context: Context, categoryArrayList: ArrayList<ModelPaperCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): HolderCategory {
        // inflate bind row category books

        binding = RowCategoryPaperAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory(binding.root)

    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {


        // get data set data handle clicks

        //get data

        val model = categoryArrayList[position]

        val id = model.id
        val category = model.category
        model.uid
        model.timestamp

        //set data
        holder.categoryTv.text = category

        // handle clicks delete btn


        // handle click ,start pdf list admin activity,also pdf Id, Title

        holder.itemView.setOnClickListener {

            val intent = Intent(context, PaperPdfActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)


        }

    }


    override fun getItemCount(): Int {

        return categoryArrayList.size // numbers of items in list
    }

    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var categoryTv: TextView = binding.categoryTv
    }

    override fun getFilter(): Filter {

        if (filter == null) {
            filter = FilterPaperCategoryUser(filterList, this)
        }

        return filter as FilterPaperCategoryUser

    }

}