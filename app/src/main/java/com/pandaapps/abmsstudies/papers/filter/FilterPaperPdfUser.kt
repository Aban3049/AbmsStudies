package com.pandaapps.abmsstudies.papers.filter

import android.widget.Filter
import com.pandaapps.abmsstudies.papers.adapter.AdapterPaperPdf
import com.pandaapps.abmsstudies.papers.model.ModelPaperPdf
import java.util.Locale

class FilterPaperPdfUser:Filter {

    private var filterList:ArrayList<ModelPaperPdf>

    private var adapterCategory:AdapterPaperPdf

    constructor(
        filterList: ArrayList<ModelPaperPdf>,
        adapterCategory: AdapterPaperPdf
    ) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {

        var constraint = constraint
        val results = Filter.FilterResults()

        if (!constraint.isNullOrEmpty()) {
            // if query is either empty or null
            // convert to UpperCase to make query not case sensitive
            constraint = constraint.toString().uppercase(Locale.getDefault())
            // to hold list of filtered ads based on query
            val filteredModels = ArrayList<ModelPaperPdf>()
            for (i in filterList.indices) {
                //apply filter if query matches to any of brand, category condition, title , then add it to filterModels
                if (
                    filterList[i].title.uppercase(Locale.getDefault()).contains(constraint)
                ) {
                    // query matches to  title add to filtered list

                    filteredModels.add(filterList[i])

                }
            }

            //prepare filtered list and item count
            results.count = filteredModels.size
            results.values = filteredModels


        }

        else{

            //query is either empty or null, prepare original/complete list and item count

            results.count = filterList.size
            results.values = filterList
        }

        return results

    }

    override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults) {

        //apply filter changes
        adapterCategory.pdfArrayList = results.values as ArrayList<ModelPaperPdf>

        //notify changes

        adapterCategory.notifyDataSetChanged()


    }

}