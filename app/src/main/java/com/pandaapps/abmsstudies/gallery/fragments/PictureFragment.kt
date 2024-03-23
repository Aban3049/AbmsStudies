package com.pandaapps.abmsstudies.gallery.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.databinding.FragmentPictureBinding
import com.pandaapps.abmsstudies.gallery.activities.AddPicturesActivity
import com.pandaapps.abmsstudies.gallery.adapter.adapterPictures
import com.pandaapps.abmsstudies.gallery.model.modelPictures


class PictureFragment : Fragment() {

    private lateinit var binding: FragmentPictureBinding

    private lateinit var mContext: Context


    private lateinit var pictureArrayList: ArrayList<modelPictures>

    private lateinit var adapterPictures: adapterPictures
    
    private companion object{
        private const val TAG = "PICTURES_FRAG"
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = FragmentPictureBinding.inflate(LayoutInflater.from(context), container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addPicture.setOnClickListener {
            startActivity(Intent(mContext, AddPicturesActivity::class.java))
        }

        loadPictures()
    }

    private fun loadPictures() {

        //init arrayList

        Log.d(TAG, "loadPictures: ")
        pictureArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("GalleryPictures")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    //clear list before setting data to it
                    pictureArrayList.clear()

                    Log.d(TAG, "onDataChange: ")

                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(modelPictures::class.java)
                        //add to list
                        pictureArrayList.add(model!!)
                        

                    }

                    //setup adapter
                    adapterPictures = adapterPictures(mContext, pictureArrayList)
                    binding.picturesRv.adapter = adapterPictures

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


    }


}