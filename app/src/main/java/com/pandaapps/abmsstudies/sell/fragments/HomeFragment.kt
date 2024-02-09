package com.pandaapps.abmsstudies.sell.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.RvListenerCategory
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.sell.activities.LocationPickerActivity
import com.pandaapps.abmsstudies.sell.adapter.AdapterAd
import com.pandaapps.abmsstudies.sell.adapter.AdapterCategory
import com.pandaapps.abmsstudies.databinding.FragmentHomeBinding
import com.pandaapps.abmsstudies.sell.model.ModelAd
import com.pandaapps.abmsstudies.sell.model.ModelCategory


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    companion object {
        private const val TAG ="HOME_TAG"
        //Max distance in kiloMeters tho show ads under that distance
        private const val MAX_DISTANCE_TO_LOAD_ADS_KM =10
    }

    private lateinit var mContext: Context

    //adArrayList to hold ads list to show in RecyclerView
    private lateinit var adArrayList: ArrayList<ModelAd>

    //AdapterAd class instance to set ot RecyclerView to show Ads list
    private lateinit var adapterAd: AdapterAd

    //SharedPreferences to store the selected location from map to load ads nearby
    private lateinit var locationSp: SharedPreferences

    private var currentLatitude =0.0
    private var currentLongitude =0.0
    private var currentAddress =""



    override fun onAttach(context: Context) {
        mContext =context
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
       binding=FragmentHomeBinding.inflate(LayoutInflater.from(mContext),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init share preferences
        locationSp = mContext.getSharedPreferences("LOCATION_SP",Context.MODE_PRIVATE)

        currentLatitude = locationSp.getFloat("CURRENT_LATITUDE",0.0f).toDouble()
        currentLongitude= locationSp.getFloat("CURRENT_LONGITUDE",0.0f).toDouble()
        currentAddress = locationSp.getString("CURRENT_ADDRESS","")!!
        // if current location is not null location is picked
        if (currentLatitude != 0.0 && currentLongitude !=0.0){
            //setting last selected location to locationTv
            binding.locationTv.text=currentAddress
        }

        // function to call load Categories
        loadCategories()

        // function call , load all ads
        loadAds("All")

        //add text change listener to searchEt to search as=ds based on query type in searchEt
       binding.searchEt.addTextChangedListener(object :TextWatcher{
           override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

           }
           override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
               Log.d(TAG,"onTextChanged: query: $s")
               try {
                   val query =s.toString()
                   adapterAd.filter.filter(query)
               }
               catch (e:Exception){
                   Log.e(TAG,"onTextChanged",e)
               }
           }

           override fun afterTextChanged(p0: Editable?) {

           }
       })

       binding.locationCv.setOnClickListener {
           val intent =Intent(mContext, LocationPickerActivity::class.java)
           locationPickedActivityResultLauncher.launch(intent)
       }

    }

    private val locationPickedActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->

        if (result.resultCode == Activity.RESULT_OK){
            Log.d(TAG,"locationPickedActivityResultLauncher: RESULT_OK")

            val data =result.data

            if (data != null){
                Log.d(TAG,"locationPickedActivityResultLauncher: Location Picked!")

                currentLatitude= data.getDoubleExtra("latitude",0.0)
                currentLongitude= data.getDoubleExtra("longitude",0.0)
                currentAddress= data.getStringExtra("address").toString()


                locationSp.edit()
                    .putFloat("CURRENT_LATITUDE",currentLatitude.toFloat())
                    .putFloat("CURRENT_LONGITUDE",currentLongitude.toFloat())
                    .putString("CURRENT_ADDRESS",currentAddress)
                    .apply()

                  // set the picked Address
                binding.locationTv.text = currentAddress

                // after picking address reload all ads again based on newly picked location
                loadAds("All")

            }
            else{
                Utils.toast(mContext, "Cancelled..!")
            }
        }
    }

    private fun loadAds(category:String){
        Log.d(TAG,"loadAds: category: $category")

        adArrayList = ArrayList()

        val ref =FirebaseDatabase.getInstance().getReference("Ads")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                adArrayList.clear()

                for (ds in snapshot.children){


                    try {
                        val modelAd = ds.getValue(ModelAd::class.java)


                        val distance = calculateDistanceKm(
                            modelAd?.latitude ?:0.0,
                            modelAd?.longitude?:0.0
                        )

                        Log.d(TAG,"onDataChange: distance: $distance")
                        if (category =="All"){
                            if (distance <= MAX_DISTANCE_TO_LOAD_ADS_KM){
                                adArrayList.add(modelAd!!)
                            }
                        }else{

                            if (modelAd!!.category.equals(category)){
                                //select category is selected ,so let's match if selected category match with ad's category
                                if (distance <= MAX_DISTANCE_TO_LOAD_ADS_KM){
                                    //the distance is <= required e.g 10Km Add to list
                                    adArrayList.add(modelAd)
                                }
                            }
                        }
                    }catch (e:Exception){
                        Log.e(TAG,"onDataChange:",e)
                    }
                }

                // setup adapter and set to recyclerView
                adapterAd = AdapterAd(mContext,adArrayList)
                binding.adsRv.adapter=adapterAd
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG,"")
            }
        })
    }

    private fun calculateDistanceKm(adLatitude:Double,adLongitude:Double):Double{
        Log.d(TAG,"calculateDistanceKm: currentLatitude: $currentLatitude")
        Log.d(TAG,"calculateDistanceKm: currentLongitude: $currentLongitude")
        Log.d(TAG,"calculateDistanceKm: adLatitude: $adLatitude")
        Log.d(TAG,"calculateDistanceKm: adLongitude: $adLongitude")

        // Secure Location i.e, user's current location
        val startPoint = Location(LocationManager.NETWORK_PROVIDER)
        startPoint.latitude = currentLatitude
        startPoint.longitude= currentLongitude

        //Destination location i.e, Ad's location
        val endpoint = Location(LocationManager.NETWORK_PROVIDER)
        endpoint.latitude=adLatitude
        endpoint.longitude=adLongitude

        //calculate distance in meters
        val distanceInMeters = startPoint.distanceTo(endpoint).toDouble()
        //return distance in kilometers km =m/1000

        return distanceInMeters / 1000

    }

    private fun loadCategories(){
        //init categoryArrayList
        val categoryArrayList = ArrayList<ModelCategory>()

        //get categories from Util class and add in categoryArrayList
        for (i in 0 until Utils.categories.size) {
            val modelCategory = ModelCategory(Utils.categories[i], Utils.categoryIcon[i])
            // add modelCategory to categoryArrayList
            categoryArrayList.add(modelCategory)

        }
        // inti/setup Adapter Category
        val adapterCategory = AdapterCategory(mContext,categoryArrayList,object:
            RvListenerCategory {
            override fun onCategoryClick(modelCategory: ModelCategory) {
                // get selected Category
                val selectedCategory =modelCategory.category
                // load ads based on selected category
            loadAds(selectedCategory)
            }
        })

        // set adapter to Recycler View
        binding.categoriesRv.adapter= adapterCategory

    }

}