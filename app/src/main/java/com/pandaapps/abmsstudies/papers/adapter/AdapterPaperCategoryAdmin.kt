package com.pandaapps.abmsstudies.papers.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.pandaapps.abmsstudies.databinding.RowCategoryPaperAdminBinding
import com.pandaapps.abmsstudies.papers.activities.AdminPaperPdfActivity
import com.pandaapps.abmsstudies.papers.filter.filterPaperAdminCategory
import com.pandaapps.abmsstudies.papers.model.ModelPaperCategory
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class AdapterPaperCategoryAdmin :
    RecyclerView.Adapter<AdapterPaperCategoryAdmin.HolderCategoryAdmin>,Filterable {

    private var context: Context

    private lateinit var binding: RowCategoryPaperAdminBinding

    private var filterList:ArrayList<ModelPaperCategory>

    private var filter:filterPaperAdminCategory?=null

    var categoryArrayList: ArrayList<ModelPaperCategory>

    constructor(context: Context, categoryArrayList: ArrayList<ModelPaperCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategoryAdmin {

        binding = RowCategoryPaperAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategoryAdmin(binding.root)

    }

    override fun onBindViewHolder(holder: HolderCategoryAdmin, position: Int) {


        val model = categoryArrayList[position]

        val id = model.id
        val category = model.category
        model.uid
        model.timestamp

        //set data
        holder.categoryTv.text = category

        // handle clicks delete btn

        holder.deleteBtn.visibility = View.VISIBLE



        holder.deleteBtn.setOnClickListener {
            //confirm before delete

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
            builder.setMessage("Are you sure you want to delete this category")
            builder.setPositiveButton("Confirm") { _, _ ->
                MotionToast.createColorToast(
                    context as Activity,
                    "Delete",
                    "Deleting...",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(context, www.sanju.motiontoast.R.font.montserrat_bold)
                )

                deleteCategory(model)


            }

            builder.setNegativeButton("Cancel") { a, _ ->
                a.dismiss()
            }.show()

        }

        // handle click ,start pdf list admin activity,also pdf Id, Title

        holder.itemView.setOnClickListener {

            val intent = Intent(context, AdminPaperPdfActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)


        }

    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }

    private fun deleteCategory(
        model: ModelPaperCategory
    ) {
        //get id of category to delete

        val id = model.id

        // Firebase Db > Categories > Categories Id

        val ref = FirebaseDatabase.getInstance().getReference("CategoriesPapers")
        ref.child(id).removeValue().addOnSuccessListener {

            MotionToast.createColorToast(
                context as Activity,
                "Delete",
                "Deleted Successfully",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(context, www.sanju.motiontoast.R.font.montserrat_bold)
            )

        }.addOnFailureListener { e ->

            MotionToast.createColorToast(
                context as Activity,
                "Deleting",
                "Failed to delete due to ${e.message}",
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(context, www.sanju.motiontoast.R.font.montserrat_bold)
            )

        }

    }


    inner class HolderCategoryAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var categoryTv: TextView = binding.categoryTv
        var deleteBtn: ImageButton = binding.deleteBtn

    }

    override fun getFilter(): Filter {

        if (filter == null) {
            filter = filterPaperAdminCategory(filterList, this)
        }

        return filter as filterPaperAdminCategory

    }

}