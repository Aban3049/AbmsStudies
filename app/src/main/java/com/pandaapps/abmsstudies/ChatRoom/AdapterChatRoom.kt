package com.pandaapps.abmsstudies.ChatRoom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.pandaapps.abmsstudies.ChatRoom.model.ChatRoomModel
import com.pandaapps.abmsstudies.databinding.RowChatroomBinding


class AdapterChatRoom(
    options: FirestoreRecyclerOptions<ChatRoomModel>,
    var context: Context
) :
    FirestoreRecyclerAdapter<ChatRoomModel, AdapterChatRoom.ChatRoomViewHolder>(options) {


    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int, model: ChatRoomModel) {

        //get Data and Set Data
        val message = model.message
        holder.messageTv.text = message

        val imageIv = model.profileImageUrl
        Glide.with(context)
            .load(imageIv)
            .into(holder.imageIv)

        if (model.chat_image != ""){
            Glide.with(context)
                .load(model.chat_image)
                .into(holder.chatImage)
            holder.chatImage.visibility = View.VISIBLE
        }else{
            holder.chatImage.visibility = View.GONE
        }

    }

    private lateinit var binding: RowChatroomBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        binding = RowChatroomBinding.inflate(LayoutInflater.from(parent.context), parent, false)



        return ChatRoomViewHolder(binding.root)
    }


    inner class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTv = binding.messageTv
        val imageIv = binding.imageIv
        val chatImage = binding.chatImage

    }

}


//, var chatRoomArrayList: ArrayList<ChatRoomModel> constructor code to see