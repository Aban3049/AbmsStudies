package com.pandaapps.abmsstudies.papers.filter

import android.widget.Filter
import com.pandaapps.abmsstudies.papers.adapter.AdapterPaperCategory
import com.pandaapps.abmsstudies.papers.model.ModelPaperCategory
import java.util.Locale

class FilterPaperCategoryUser:Filter {

    private var filterList:ArrayList<ModelPaperCategory>

    private var adapterCategory:AdapterPaperCategory

    constructor(
        filterList: ArrayList<ModelPaperCategory>,
        adapterCategory: AdapterPaperCategory
    ) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }


    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        var result = Filter.FilterResults()

        if (!constraint.isNullOrEmpty()){

            constraint = constraint.toString().uppercase(Locale.getDefault())

            val filteredModels = ArrayList<ModelPaperCategory>()

            for (i in filterList.indices){

                if (filterList[i].category.uppercase(Locale.getDefault()).contains(constraint)){

                    filteredModels.add(filterList[i])

                }

            }

            result.count = filteredModels.size
            result.values = filteredModels

        }
        else{
            result.count = filterList.size
            result.values = filterList
        }

        return result

    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {

        adapterCategory.categoryArrayList = results.values as ArrayList<ModelPaperCategory>

        adapterCategory.notifyDataSetChanged()

    }


}