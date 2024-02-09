package com.pandaapps.abmsstudies.sell.filter

import android.widget.Filter
import com.pandaapps.abmsstudies.sell.adapter.AdapterAd
import com.pandaapps.abmsstudies.sell.model.ModelAd
import java.util.Locale


class FilterAd(
    private val adapter: AdapterAd,
    private val filterList:ArrayList<ModelAd>

):Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results =FilterResults()

        if (!constraint.isNullOrEmpty()){
              // if query is either empty or null
            // convert to UpperCase to make query not case sensitive
            constraint=constraint.toString().uppercase(Locale.getDefault())
            // to hold list of filtered ads based on query
            val filteredModels = ArrayList<ModelAd>()
            for (i in filterList.indices){
                //apply filter if query matches to any of brand, category condition, title , then add it to filterModels
                if (filterList[i].brand.uppercase(Locale.getDefault()).contains(constraint) ||
                    filterList[i].category.uppercase(Locale.getDefault()).contains(constraint) ||
                    filterList[i].condition.uppercase(Locale.getDefault()).contains(constraint) ||
                    filterList[i].title.uppercase(Locale.getDefault()).contains(constraint)

                    ){
                    // query matches to any of brand, category condition, title then add it to the filteredModels
                    filteredModels.add(filterList[i])
                }
            }

                //prepare filtered list and item count
            results.count = filteredModels.size
            results.values = filteredModels

        }
        else{
            //query is either empty or null, prepare original/complete list and item count
            results.count=filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
adapter.adArrayList = results.values as ArrayList<ModelAd>

        adapter.notifyDataSetChanged()
    }


}