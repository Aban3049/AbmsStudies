package com.pandaapps.abmsstudies.gallery.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.books.MyApplication
import com.pandaapps.abmsstudies.databinding.RowVideosBinding
import com.pandaapps.abmsstudies.gallery.activities.DownloadActivity
import com.pandaapps.abmsstudies.gallery.model.modelVideo

class adapterVideos : RecyclerView.Adapter<adapterVideos.viewHolderVideo> {

    private var context: Context

    private lateinit var binding: RowVideosBinding

    private var uid: String = ""

    private var name: String = ""

    private var VideoUrl: String = ""

    private var profileImageURl: String = ""

    var galleryVideoArrayList: ArrayList<modelVideo>

    private lateinit var firebaseAuth: FirebaseAuth

    private companion object {
        private const val TAG = "ADAPTER_VIDEO"
    }

    constructor(context: Context, galleryVideoArrayList: ArrayList<modelVideo>) : super() {
        this.context = context
        this.galleryVideoArrayList = galleryVideoArrayList
    }

    init {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolderVideo {

        binding = RowVideosBinding.inflate(LayoutInflater.from(context), parent, false)

        return viewHolderVideo(binding.root)

    }


    override fun onBindViewHolder(holder: viewHolderVideo, position: Int) {

        //get data
        val model = galleryVideoArrayList[position]

        val id = model.id
        val videoUrl = model.videoUrl
        val timestamp = model.timestamp
        val title = model.title
        uid = model.uid

        val formattedDate = MyApplication.formatTimestampDate(timestamp)

        //set data
        holder.date.text = formattedDate
        holder.titleTv.text = title

        holder.userNameTv.text = name

        loadVideo(videoUrl)

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("$uid")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    name = "${snapshot.child("name").value}"
                    profileImageURl = "${snapshot.child("profileImageURl").value}"

                    holder.userNameTv.text = name
                    loadPersonImage(profileImageURl, context, binding.personIv)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


        holder.moreBtn.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

    }

    override fun getItemCount(): Int {
        return galleryVideoArrayList.size
    }

    fun loadPersonImage(
        imageUrl: String,
        context: Context,
        imageView: ShapeableImageView
    ) {

        val TAG = "BOOK_IMAGE_TAG"

        //using url we can get image
        try {

            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_image_gray)
                .into(imageView)

        } catch (e: Exception) {
            Log.e(TAG, "onDataChanged", e)
        }

    }

    private fun moreOptionsDialog(
        model: modelVideo,
        holder: viewHolderVideo
    ) {

        //get id,url ,title of the book
        val videoUrl = model.videoUrl
        val id = model.id
        // options to show in Dialog

        if (firebaseAuth.uid == model.uid) {
            val options = arrayOf("Download", "Delete")

            // alert dialog
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Choose Option")
                .setItems(options) { dialog, position ->

                    if (position == 0) {
                        val intent = Intent(context, DownloadActivity::class.java)
                        intent.putExtra("videoUrl", model.videoUrl)
                        context.startActivity(intent)

                    } else if (position == 1) {
                        //delete is  is clicked
                        //show confirmation dialog
                        deleteVideoFromStorage(videoUrl)
                        deleteFromDb(id)
                    }

                }
                .show()
        } else {
            val options = arrayOf("Download")

            // alert dialog
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Choose Option")
                .setItems(options) { dialog, position ->

                    val intent = Intent(context, DownloadActivity::class.java)
                    intent.putExtra("videoUrl", model.videoUrl)
                    context.startActivity(intent)

                }
                .show()
        }


    }

    private fun loadVideo(videoUrl: String) {
        // Initialize the player
        val player = SimpleExoPlayer.Builder(context).build()
        binding.playerView.player = player

        // Set the media item
        player.setMediaItem(MediaItem.fromUri(videoUrl))

        // Prepare the player
        player.prepare()

//        // Autoplay the video
//        player.play()
    }

    private fun deleteVideoFromStorage(videoUrl: String) {
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("$videoUrl")

        // Delete the video file
        storageRef.delete()
            .addOnSuccessListener {
                // Video successfully deleted
                Log.d("TAG", "Video deleted successfully")
                Utils.toast(context, "Deleted Successfully")

            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred during the deletion
                Log.e("TAG", "Error deleting video: ${exception.message}")
                Utils.toast(context, "Failed to delete due to $exception")
            }
    }

    private fun deleteFromDb(id: String) {

        val ref = FirebaseDatabase.getInstance().getReference("GalleryVideos")
        ref.child(id).removeValue()
            .addOnSuccessListener {

            }.addOnFailureListener {

            }

    }


    inner class viewHolderVideo(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val personIv = binding.personIv
        val titleTv = binding.titleTv
        val userNameTv = binding.userNameTv
        val date = binding.publishDateTv
        val moreBtn = binding.moreBtn
        val videoPlayer = binding.playerView

    }

}