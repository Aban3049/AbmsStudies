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
import com.pandaapps.abmsstudies.sell.filter.FilterChats
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.sell.activities.ChatActivity
import com.pandaapps.abmsstudies.databinding.RowChatsBinding

import com.pandaapps.abmsstudies.sell.model.ModelChats

class AdapterChats : RecyclerView.Adapter<AdapterChats.HolderChats>,Filterable {

    private var context: Context
     var chatsArrayList: ArrayList<ModelChats>
   private var filterList: ArrayList<ModelChats>

   private var filter: FilterChats? = null

    private lateinit var binding: RowChatsBinding

    private var firebaseAuth: FirebaseAuth

    private var myUid = ""


    private companion object {
        private const val TAG = "ADAPTER_CHATS_TAG"
    }

    constructor(context: Context, chatArrayList: ArrayList<ModelChats>) {
        this.context = context
        this.chatsArrayList = chatArrayList
        this.filterList = chatArrayList

        firebaseAuth = FirebaseAuth.getInstance()

        // uid of currently logged in user
        myUid = "${firebaseAuth.uid}"
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChats {

        binding = RowChatsBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderChats(binding.root)
    }

    override fun onBindViewHolder(holder: HolderChats, position: Int) {
        val modelChats = chatsArrayList[position]

        loadLastMessage(modelChats, holder)

        // handle chat item click open chat Activity

        holder.itemView.setOnClickListener {
            val receiptUid = modelChats.receiptUid


            if (receiptUid != null) {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("receiptUid", receiptUid)
                context.startActivity(intent)
            }
        }

    }


    override fun getItemCount(): Int {
        return chatsArrayList.size
    }


    private fun loadLastMessage(modelChats: ModelChats, holder: HolderChats) {
        val chatKey = modelChats.chatKey
        Log.d(TAG, "loadLastMessage: chatKey : $chatKey")
    
        val ref =FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatKey).limitToLast(1)
            .addValueEventListener(object:ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    
                    for (ds in snapshot.children){
                        
                        val fromUid = "${ds.child("fromUid").value}"
                        val message = "${ds.child("message").value}"
                        val messageId = "${ds.child("messageId").value}"
                        val messageType = "${ds.child("messageType").value}"
                        val timestamp = ds.child("timestamp").value as Long
                        val toUid = "${ds.child("toUid").value}"
                        
                        val formattedDate = Utils.formatTimestampDate(timestamp)
                        
                        modelChats.message=message
                        modelChats.messageId = messageId
                        modelChats.messageType =messageType
                        modelChats.fromUid=fromUid
                        modelChats.timestamp=timestamp
                        modelChats.toUid=toUid
                        
                        holder.dateTimeTv.text = "$formattedDate"
                        
                        if (messageType == Utils.MESSAGE_TYPE_TEXT){
                            
                            holder.lastMessageTv.text = message
                        }else{
                            
                            holder.lastMessageTv.text = "Sends Attachments"
                        }
                        
                    }
                    
                    loadReceiptUserInfo(modelChats, holder)
                }

                override fun onCancelled(error: DatabaseError) {
                    
                }
                
            })
    
    }

    private fun loadReceiptUserInfo(modelChats: ModelChats, holder: HolderChats) {
        
        val fromUid = modelChats.fromUid
        val toUid = modelChats.toUid
        
        var receiptUid = ""
        
        if (fromUid == myUid){
            
            receiptUid = toUid
        }
        else{
            receiptUid=fromUid
        }

        Log.d(TAG, "loadReceiptUserInfo: fromUid: $fromUid")
        Log.d(TAG, "loadReceiptUserInfo: toUid: $toUid")
        Log.d(TAG, "loadReceiptUserInfo: receiptUid: $receiptUid")
        
        modelChats.receiptUid = receiptUid
        
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(receiptUid)
            .addValueEventListener(object : ValueEventListener{
                
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name ="${snapshot.child("name").value}"
                    val profileImageUrl = "${snapshot.child("profileImageURl").value}"


                    //setting data to model class

                    modelChats.name=name
                    modelChats.profileImageUrl=profileImageUrl

                    holder.nameTv.text=name

                    try {
                        Glide.with(context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(holder.profileIv)
                    }catch (e:Exception){
                        Log.e(TAG, "onDataChange: ",e )
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    
                }
                
            })
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterChats(this@AdapterChats,filterList)
        }

        return filter!!
    }


    inner class HolderChats(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var profileIv = binding.profileIv
        var nameTv = binding.nameTv
        var lastMessageTv = binding.lastMessageTv
        var dateTimeTv = binding.dateTimeTv
    }


}
