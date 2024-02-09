package com.pandaapps.abmsstudies.sell.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.pandaapps.abmsstudies.databinding.RowImageSliderBinding
import com.pandaapps.abmsstudies.sell.model.ModelImageSlider
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.bumptech.glide.Glide
import com.pandaapps.abmsstudies.R

class AdapterImageSlider :Adapter<AdapterImageSlider.HolderImageSlider>{


    private lateinit var binding: RowImageSliderBinding

    private companion object{
        private const val TAG ="IMAGE_SLIDER_TAG"
    }


    private  var context: Context
    private var imageArrayList: ArrayList<ModelImageSlider>

    constructor(context: Context,imageArrayList: ArrayList<ModelImageSlider>){
        this.context=context
        this.imageArrayList=imageArrayList
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderImageSlider {
        binding= RowImageSliderBinding.inflate(LayoutInflater.from(context),parent,false)
       return HolderImageSlider(binding.root)
    }

    override fun onBindViewHolder(holder: HolderImageSlider, position: Int) {
       val modelImageSlider = imageArrayList[position]


        val imageUrl=modelImageSlider.imageUrl
        val imageCount ="${position+1}/${imageArrayList.size}"

        holder.imageCountTv.text=imageCount
        try {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_gray)
                .into(holder.imageIv)
        }catch (e:Exception){
            Log.e(TAG,"onBindViewHolder",e)
        }

        //handle image click open in full screen
        holder.itemView.setOnClickListener {

        }

    }

    override fun getItemCount(): Int {
        return  imageArrayList.size
    }
    inner class HolderImageSlider(itemView: View) : RecyclerView.ViewHolder(itemView) {


        var imageIv: ShapeableImageView =binding.imageIv
        var imageCountTv:TextView = binding.imageCountTV
    }




}