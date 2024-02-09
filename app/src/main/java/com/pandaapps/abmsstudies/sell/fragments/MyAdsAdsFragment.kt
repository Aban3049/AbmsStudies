package com.pandaapps.abmsstudies.sell.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.sell.adapter.AdapterAd
import com.pandaapps.abmsstudies.databinding.FragmentMyAdsAdsBinding
import com.pandaapps.abmsstudies.sell.model.ModelAd

class MyAdsAdsFragment : Fragment() {

    private lateinit var binding: FragmentMyAdsAdsBinding

    private companion object {
        private const val TAG = "MY_ADS_TAG"
    }

    private lateinit var mContext: Context

    private lateinit var firebaseAuth: FirebaseAuth

    //adArrayList to hold ads list by currently logged-in user to show in Recycler View

    private lateinit var adArrayList: ArrayList<ModelAd>

    //Adapter class instance to set to Recyclerview to show Ads list

    private lateinit var adapterAd: AdapterAd

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMyAdsAdsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        // function call to load ads by currently Logged In Users
        loadAds()

        // add text change listener to SearchEt to search ads using filter applied in AdapterAd class
        binding.searchEt.addTextChangedListener(object :TextWatcher{
            // this function is called when user type a letter,search based on what user type
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    val query = s.toString()
                    adapterAd.filter.filter(query)
                }catch (e:java.lang.Exception){
                    Log.e(TAG,"onTextChanged: ",e)
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


    }

    private fun loadAds() {
        Log.d(TAG, "loadAds: ")
        // inti adArrayList before starting adding data into it
        adArrayList = ArrayList()
        // Firebase Db listener to load ads ads by currently Logged in Users
        // show Only Ads whose key uid equal to uid of currently logged_in_user
        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.orderByChild("uid").equalTo(firebaseAuth.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                // clear adArraylist each time adding data into It
                    adArrayList.clear()
                   // load Ads list
                    for (ds in snapshot.children) {

                        try {
                            //Prepare ModelAd with all data from Firebase DB
                            val modelAd = ds.getValue(ModelAd::class.java)

                            adArrayList.add(modelAd!!)
                        } catch (e: Exception) {
                            Log.e(TAG, "onDataChanged: ", e)
                        }

                    }
                     // setup AdapterAd class and set to Recyclerview
                    adapterAd= AdapterAd(mContext,adArrayList)
                    binding.adsRv.adapter=adapterAd


                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }
}