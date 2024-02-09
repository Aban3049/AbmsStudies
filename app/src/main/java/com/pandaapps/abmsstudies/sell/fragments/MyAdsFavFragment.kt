package com.pandaapps.abmsstudies.sell.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
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
import com.pandaapps.abmsstudies.databinding.FragmentMyAdsFavBinding
import com.pandaapps.abmsstudies.sell.model.ModelAd

class MyAdsFavFragment : Fragment(){

    private lateinit var binding:FragmentMyAdsFavBinding
    private lateinit var mContext:Context
    private lateinit var firebaseAuth: FirebaseAuth

    private companion object{
        private const val TAG ="FAV_ADS_TAG"
    }

    // adArrayList to hold ads list added to favourite by currently logged-in user to show in Recyclerview
    private lateinit var adArrayList: ArrayList<ModelAd>

    //AdapterAd class instance to set to RecycledView to show Ads list
    private lateinit var adapterAd: AdapterAd

    override fun onAttach(context: Context) {
        super.onAttach(context)
       this.mContext=context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding= FragmentMyAdsFavBinding.inflate(inflater,container,false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        firebaseAuth=FirebaseAuth.getInstance()


        loadAds()

        binding.searchEt.addTextChangedListener(object : TextWatcher {
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

    private fun loadAds(){
        Log.d(TAG,"loadAds: ")

        // init adArrayList before starting adding data into it
        adArrayList = ArrayList()

        val favRef = FirebaseDatabase.getInstance().getReference("Users")
        favRef.child(firebaseAuth.uid!!).child("Favourites")
            .addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    adArrayList.clear()

                    for (ds in snapshot.children){
                     // get id of the Ad. e.g Users > uid >Favourites >adId
                        val adId ="${ds.child("adId").value}"
                        Log.d(TAG,"onDataChange: adId: $adId")

                        //Firebase DB listener to load Ad details based on id of the Ad we just got
                        val adRef = FirebaseDatabase.getInstance().getReference("Ads")
                        adRef.child(adId)
                            .addListenerForSingleValueEvent(object:ValueEventListener{

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    try {

                                        // prepare ModelAd with all the data from Firebase

                                        val modelAd = snapshot.getValue(ModelAd::class.java)

                                        adArrayList.add(modelAd!!)

                                    }catch (e:Exception){
                                        Log.e(TAG,"onDataChange",e)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                    }

                    Handler().postDelayed({

                        adapterAd = AdapterAd(mContext,adArrayList)
                        binding.adsRv.adapter=adapterAd

                    },500)

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }

}