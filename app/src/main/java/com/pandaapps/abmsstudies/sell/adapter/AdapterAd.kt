package com.pandaapps.abmsstudies.sell.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.sell.filter.FilterAd
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.sell.activities.AdDetailsActivity
import com.pandaapps.abmsstudies.databinding.RowAdBinding
import com.pandaapps.abmsstudies.sell.model.ModelAd

class AdapterAd : RecyclerView.Adapter<AdapterAd.HolderAd>, Filterable {

    private lateinit var binding: RowAdBinding


    private companion object {
        private const val TAG = "ADAPTER_AD_TAG"
    }

    // context of Activity/Fragments from where instance of AdapterAd class is Created
    private var context: Context

    //ArrayList the List of the Ads
    var adArrayList: ArrayList<ModelAd>

    private var filterList: ArrayList<ModelAd>

    private var filter: FilterAd? = null

    private var firebaseAuth: FirebaseAuth

    constructor(context: Context, adArrayList: ArrayList<ModelAd>) {
        this.context = context
        this.adArrayList = adArrayList
        this.filterList = adArrayList
        this.firebaseAuth = FirebaseAuth.getInstance()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAd {
        //inflate/bind the row_ad.xml
        binding = RowAdBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderAd(binding.root)
    }

    override fun onBindViewHolder(holder: HolderAd, position: Int) {
        //get data from particular position of list and set to the UI View of row_ad.xml and Handle Clicks
        val modelAd = adArrayList[position]

        val title = modelAd.title
        val description = modelAd.description
        val address = modelAd.address
        val condition = modelAd.condition
        val price = modelAd.price
        val timestamp = modelAd.timestamp
        val formattedDate = Utils.formatTimestampDate(timestamp)


        holder.itemView.setOnClickListener {

            val intent = Intent(context,AdDetailsActivity::class.java)
            intent.putExtra("adId",modelAd.id)
            context.startActivity(intent)

        }

        loadAdFirstImage(modelAd, holder)

        //if user is logged in then chk if the ad is in favourite of current User
        if (firebaseAuth != null) {
            checkIsFavourite(modelAd, holder)
        }

        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.addressTv.text = address
        holder.conditionTv.text = condition
        holder.priceTv.text = price
        holder.dateTv.text = formattedDate

        holder.favBtn.setOnClickListener {

            val favourite = modelAd.favourite
            // chk if the ad is in favourite of current User
            if (favourite) {
                Utils.removeFromFavourite(context, modelAd.id)
            } else {
                //ad is not in favourite of User ad in favourite
                Utils.addToFavourite(context, modelAd.id)
            }
        }

    }

    private fun checkIsFavourite(modelAd: ModelAd, holder: HolderAd) {
        //check if the ad is in favourite of Current User
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favourites").child(modelAd.id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //if snapshot exit (value is true)means ad is in favourite of current user otherwise no
                    val favorite = snapshot.exists()
                     //set the value true or false to model
                    modelAd.favourite = favorite
                     // chk if favourite or not to set image accordingly
                    if (favorite) {
                        holder.favBtn.setImageResource(R.drawable.ic_fav_yes)
                    } else {
                        holder.favBtn.setImageResource(R.drawable.ic_fav_no)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


    override fun getItemCount(): Int {
        // return size of the list
        return adArrayList.size
    }

    override fun getFilter(): Filter {
        // inti filter object if it is null
        if (filter == null) {
            filter = FilterAd(this, filterList)

        }

        return filter as FilterAd
    }

    private fun loadAdFirstImage(modelAd: ModelAd, holder: HolderAd) {
        // load first image of Available images of Ad
        //Ad id to get image of it

        val adId = modelAd.id

        Log.d(TAG, "loadAdFirstImage: adId: $adId")

        val reference = FirebaseDatabase.getInstance().getReference("Ads")
        reference.child(adId).child("Images").limitToFirst(1)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    // this will return only 1 image as we have used query. limit to First(1)
                    for (ds in snapshot.children) {
                        //get url of the image,
                        val imageUrl = "${ds.child("imageUrl").value}"
                        Log.d(TAG, "onDataChanged: imageUrl: $imageUrl")


                        //set image to image view
                        try {
                            Glide.with(context)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_image_gray)
                                .into(holder.imageIv)
                        } catch (e: Exception) {
                            Log.d(TAG, "onDataChanged: $e")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    // A viewHolder class to hold/init UI View of row_ad.xml*/
    inner class HolderAd(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //init UI Views of the row_ad.xml
        var imageIv = binding.ImageIv
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var favBtn = binding.favBtn
        var addressTv = binding.addressTv
        var conditionTv = binding.conditionTv
        var priceTv = binding.priceTv
        var dateTv = binding.dateTv
    }


}