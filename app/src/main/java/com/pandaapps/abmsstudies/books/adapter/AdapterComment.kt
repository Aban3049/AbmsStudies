package com.pandaapps.abmsstudies.books.adapter


import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.books.MyApplication
import com.pandaapps.abmsstudies.books.model.ModelComments
import com.pandaapps.abmsstudies.databinding.RowCommentsBinding
class AdapterComment : RecyclerView.Adapter<AdapterComment.HolderComments> {

    private val context: Context

    //array list to hold comments

    private val commentArrayList: ArrayList<ModelComments>

    private lateinit var binding: RowCommentsBinding

    private  var firebaseAuth: FirebaseAuth

    constructor(context: Context, commentArrayList: ArrayList<ModelComments>) {
        this.context = context
        this.commentArrayList = commentArrayList

        //initialize firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()
    }

    //view holder class for comments.xml


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComments {
        binding = RowCommentsBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderComments(binding.root)
    }

    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    override fun onBindViewHolder(holder: HolderComments, position: Int) {

        //get dat set data handle clicks
        val model = commentArrayList[position]

        model.id
        model.bookId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        //format timestamp
        val formattedData = MyApplication.formatTimestampDate(timestamp.toLong())

        //set Data
        holder.dateTv.text = formattedData
        holder.commentTv.text = comment

        //we don't have user,profile picture but we have user uid, so we will load using uid
        loadUserDetails(model, holder)

        // handle click, show dialog delete comment
        holder.itemView.setOnClickListener {
            //requirements to delete a comment
            // * 1) User Must be a logged in User
            // * 2)Only user who commented can delete comment uid of the comment and user must be same to delete

            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                deleteCommentDialog(model, holder)
            }

        }

    }

    private fun deleteCommentDialog(model: ModelComments, holder: AdapterComment.HolderComments) {
        // alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Comment")
            .setMessage("Are you sure you want to delete this comment")
            .setPositiveButton("DELETE") { _, _ ->

                //delete comment

                val bookId = model.bookId
                val commentId = model.id

                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Utils.toast(context, "Comment Deleted Successfully")
                    }.addOnFailureListener {
                        Utils.toast(context, "Failed to delete comment due to ${it.message}")
                    }

            }.setNegativeButton("CANCEL") { d, _ ->
                d.dismiss()
            }
            .show()
    }

    private fun loadUserDetails(model: ModelComments, holder: AdapterComment.HolderComments) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    //get name , profile image
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImageURl").value}"

                    //set data
                    holder.nameTv.text = name
                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(holder.profileIv)
                    } catch (e: Exception) {
                        //in case image is empty or null, set default image
                        holder.profileIv.setImageResource(R.drawable.ic_person_gray)
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    inner class HolderComments(itemView: View) : RecyclerView.ViewHolder(itemView) {
//init UI Views

        val profileIv = binding.profileIv
        val nameTv = binding.nameTv
        val dateTv = binding.dateTv
        val commentTv = binding.commentTv

    }

}