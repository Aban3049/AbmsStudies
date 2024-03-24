package com.pandaapps.abmsstudies.ChatRoom

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.ChatRoom.FirebaseCords.FirebaseCords
import com.pandaapps.abmsstudies.ChatRoom.model.ChatRoomModel
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.RowChatroomBinding


class AdapterChatRoom(
    options: FirestoreRecyclerOptions<ChatRoomModel>,
    var context: Context,
    private val onAdapterChangeListener: () -> Unit
) :
    FirestoreRecyclerAdapter<ChatRoomModel, AdapterChatRoom.ChatRoomViewHolder>(options) {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onDataChanged() {
        super.onDataChanged()
        onAdapterChangeListener.invoke()
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int, model: ChatRoomModel) {

        firebaseAuth = FirebaseAuth.getInstance()

        //get Data and Set Data
        val message = model.message
        holder.messageTv.text = message

        val chatTime = model.chatTime
        val formattedChatTime = Utils.formatTimestampDateTimeChat(chatTime)
        holder.chatTime.text = formattedChatTime

        val imageIv = model.profileImageUrl
        Glide.with(context)
            .load(imageIv)
            .into(holder.imageIv)

        if (model.chat_image != "") {
            Glide.with(context)
                .load(model.chat_image)
                .into(holder.chatImage)
            holder.chatImage.visibility = View.VISIBLE
        } else {
            holder.chatImage.visibility = View.GONE
        }

        val userName = model.userName
        holder.userName.text = "@$userName"

        val uid = model.uid


        holder.itemView.setOnClickListener {
            //requirements to delete a chat
            // * 1) User Must be a logged in User
            // * 2)Only user who chatted can delete chat uid of the chat and user must be same to delete

            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                deleteChatDialog(model, holder)
            }
        }

    }

    private lateinit var binding: RowChatroomBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        binding = RowChatroomBinding.inflate(LayoutInflater.from(parent.context), parent, false)



        return ChatRoomViewHolder(binding.root)
    }

    private fun deleteChatDialog(model: ChatRoomModel, holder: AdapterChatRoom.ChatRoomViewHolder) {
        // alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Chat")
            .setMessage("Are you sure you want to delete this chat")
            .setPositiveButton("DELETE") { _, _ ->

                //delete chat

                val fireBaseCords = FirebaseCords()

                val chatId = model.messageId


                val ref = fireBaseCords.MAIN_CHAT_DATABASE.document(chatId)
                ref.delete()
                    .addOnSuccessListener {
                        Utils.toast(context, "Chat Deleted Successfully")

                        try {
                            if (model.chat_image != null) {
                                val storageRef =
                                    FirebaseStorage.getInstance().getReferenceFromUrl(model.chat_image)
                                storageRef.delete()
                                    .addOnSuccessListener {
                                        Utils.toast(context, "Media deleted Successfully")
                                    }
                                    .addOnFailureListener {
                                        Utils.toast(context, "Failed to delete due to ${it.message}")
                                    }
                            }
                        }catch (e:Exception){
//                            Utils.toast(context,"Failed to delete due to ${e.message}")
                        }



                    }.addOnFailureListener {
                        Utils.toast(context, "Failed to delete chat due to ${it.message}")
                    }

            }.setNegativeButton("CANCEL") { d, _ ->
                d.dismiss()
            }
            .show()
    }



    inner class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTv = binding.messageTv
        val imageIv = binding.imageIv
        val chatImage = binding.chatImage
        val userName = binding.userName
        val chatTime = binding.timeTv

    }

}


