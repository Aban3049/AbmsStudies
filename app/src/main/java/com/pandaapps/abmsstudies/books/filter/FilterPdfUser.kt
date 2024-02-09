package com.pandaapps.abmsstudies.books.filter

import android.widget.Filter
import com.pandaapps.abmsstudies.books.adapter.AdapterPdfAdmin
import com.pandaapps.abmsstudies.books.adapter.AdapterPdfUser
import com.pandaapps.abmsstudies.books.model.ModelBookPdf


class FilterPdfUser: Filter {

    //arrayList in which we want to Search
    var filterList: ArrayList<ModelBookPdf>

    //adapter in which filter need to be implemented
    var adapterPdfUser: AdapterPdfUser

    constructor(filterList: ArrayList<ModelBookPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }


    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint: CharSequence? = constraint // value to search
        val result = FilterResults()

        //value should not be null and not empty

        if (constraint != null && constraint.isNotEmpty()) {
            // change to Upper case or lowercase to avoid case senstivity

            constraint = constraint.toString().lowercase()
            val filteredModels = ArrayList<ModelBookPdf>()

            for (i in filterList.indices) {
                //validate if match

                if (filterList[i].title.lowercase().contains(constraint)) {
                    // searched value is smaller to value in list, add to filtered list
                    filteredModels.add(filterList[i])
                }

            }

            result.count = filteredModels.size
            result.values = filteredModels

        } else {

            // searched value is either null or empty return all data

            result.count = filterList.size
            result.values = filterList

        }

        return result // don't miss

    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {

        //apply filter changes
       adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelBookPdf>

        //notify changes
       adapterPdfUser.notifyDataSetChanged()

        //

    }

}