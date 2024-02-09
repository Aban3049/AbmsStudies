package com.pandaapps.abmsstudies.books.filter

import android.widget.Filter
import com.pandaapps.abmsstudies.books.adapter.AdapterBooksCategoryAdmin
import com.pandaapps.abmsstudies.books.model.ModelBooksCategoryAdmin
import java.util.Locale

class FilterBooksCategoryAdmin:Filter {

    private var filterList:ArrayList<ModelBooksCategoryAdmin>

    private var adapterCategory: AdapterBooksCategoryAdmin

    constructor(
        filterList: ArrayList<ModelBooksCategoryAdmin>,
        adapterCategory: AdapterBooksCategoryAdmin
    ) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()

        if (!constraint.isNullOrEmpty()) {
            // if query is either empty or null
            // convert to UpperCase to make query not case sensitive
            constraint = constraint.toString().uppercase(Locale.getDefault())
            // to hold list of filtered ads based on query
            val filteredModels = ArrayList<ModelBooksCategoryAdmin>()
            for (i in filterList.indices) {
                //apply filter if query matches to any of brand, category condition, title , then add it to filterModels
                if (
                    filterList[i].category.uppercase(Locale.getDefault()).contains(constraint)
                ) {
                    // query matches to  category add to filtered list

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

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {

        //apply filter changes
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelBooksCategoryAdmin>

        //notify changes

        adapterCategory.notifyDataSetChanged()


    }


}