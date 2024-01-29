package com.pandaapps.abmsstudies.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
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
import com.pandaapps.abmsstudies.FilterBooksCategoryAdmin
import com.pandaapps.abmsstudies.databinding.RowCategoryBooksAdminBinding
import com.pandaapps.abmsstudies.models.ModelBooksCategoryAdmin
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class AdapterBooksCategoryAdmin : RecyclerView.Adapter<AdapterBooksCategoryAdmin.HolderCategory>,Filterable {

    private val context: Context
    public var categoryArrayList: ArrayList<ModelBooksCategoryAdmin>

    private var filterList:ArrayList<ModelBooksCategoryAdmin>

    private var filter:FilterBooksCategoryAdmin? = null

    private lateinit var binding: RowCategoryBooksAdminBinding


    constructor(context: Context, categoryArrayList: ArrayList<ModelBooksCategoryAdmin>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList=categoryArrayList
    }

    inner class HolderCategory(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var categoryTv: TextView = binding.categoryTv
        var deleteBtn: ImageButton = binding.deleteBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        // inflate bind row category books

        binding = RowCategoryBooksAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderCategory(binding.root)

    }


    override fun onBindViewHolder(holder: HolderCategory, position: Int) {

        // get data set data handle clicks

        //get data

        val model = categoryArrayList[position]

        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        //set data
        holder.categoryTv.text = category

        // handle clicks delete btn

        holder.deleteBtn.setOnClickListener {
            //confirm before delete

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
            builder.setMessage("Are you sure you want to delete this category")
            builder.setPositiveButton("Confirm") { a, d ->
                MotionToast.createColorToast(
                    context as Activity,
                    "Delete",
                    "Deleting...",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(context, www.sanju.motiontoast.R.font.montserrat_bold)
                )

                deleteCategory(model,holder)


            }

            builder.setNegativeButton("Cancel"){a,d->
                a.dismiss()
            }.show()

        }

    }

    private fun deleteCategory(model: ModelBooksCategoryAdmin, holder: HolderCategory) {
        //get id of category to delete

        val id = model.id

        // Firebase Db > Categories > Categories Id

        val ref = FirebaseDatabase.getInstance().getReference("CategoriesBooks")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {

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

    override fun getItemCount(): Int {

        return categoryArrayList.size // numbers of items in list
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter= FilterBooksCategoryAdmin(filterList,this)
        }

        return filter as FilterBooksCategoryAdmin
    }


}