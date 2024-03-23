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
import com.pandaapps.abmsstudies.databinding.FragmentVideoBinding
import com.pandaapps.abmsstudies.gallery.activities.AddVideoActivity
import com.pandaapps.abmsstudies.gallery.adapter.adapterVideos
import com.pandaapps.abmsstudies.gallery.model.modelVideo


class VideoFragment : Fragment() {

    private lateinit var mContext: Context

    private lateinit var binding: FragmentVideoBinding

    private lateinit var videoArrayList:ArrayList<modelVideo>

    private lateinit var adapterVideos: adapterVideos

    private companion object {
        private const val TAG = "VIDEO_TAG"
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
        binding = FragmentVideoBinding.inflate(LayoutInflater.from(context), container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.addVideo.setOnClickListener {
            startActivity(Intent(mContext, AddVideoActivity::class.java))
        }

        loadVideos()

    }

    private fun loadVideos() {

        //init arrayList

        Log.d(TAG, "loadVideos: ")
       videoArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("GalleryVideos")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    //clear list before setting data to it
                   videoArrayList.clear()

                    Log.d(TAG, "onDataChange: ")

                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(modelVideo::class.java)
                        //add to list
                        videoArrayList.add(model!!)


                    }

                    //setup adapter
                    adapterVideos = adapterVideos(mContext, videoArrayList)
                    binding.videosRv.adapter = adapterVideos

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


    }
}