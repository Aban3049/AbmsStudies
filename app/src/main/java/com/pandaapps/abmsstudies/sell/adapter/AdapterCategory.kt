package com.pandaapps.abmsstudies.sell.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.pandaapps.abmsstudies.RvListenerCategory
import com.pandaapps.abmsstudies.databinding.RowCategoryBinding
import com.pandaapps.abmsstudies.sell.model.ModelCategory
import java.util.Random

class AdapterCategory(
    private val context: Context,
    private val categoryArrayList: ArrayList<ModelCategory>,
    private val rvListenerCategory: RvListenerCategory
) : Adapter<AdapterCategory.HolderCategory>() {

    private lateinit var binding: RowCategoryBinding


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        //inflate/bind the row category XML
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory(binding.root)
    }

    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        // Get data from particular position of list and set to the UI Views of row_category.xml and handle clicks

        val modelCategory = categoryArrayList[position]

        // get Data from modelCategory
        val icon = modelCategory.icon
        val category = modelCategory.category

        // get random color to set as the background color of the categoryIconIv
        val random = Random()
        val color = Color.argb(255, random.nextInt(255), random.nextInt(255), random.nextInt(255))

        //set data to UI View of row category.xml
        holder.categoryIconIv.setImageResource(icon)
        holder.categoryTv.text = category
        holder.categoryIconIv.setBackgroundColor(color)

        //Handle item clicks, call Interface(RvListenerCategory) method to perform click in calling activity/fragment class instead of this class
        holder.itemView.setOnClickListener {
            rvListenerCategory.onCategoryClick(modelCategory)
        }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    inner class HolderCategory(itemView: View) : ViewHolder(itemView) {


        var categoryIconIv = binding.categoryIconIv
        var categoryTv = binding.categoryTv

    }


}