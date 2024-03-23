package com.pandaapps.abmsstudies.ChatRoom.activities

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.pandaapps.abmsstudies.ChatRoom.AdapterChatRoom
import com.pandaapps.abmsstudies.ChatRoom.FirebaseCords.FirebaseCords
import com.pandaapps.abmsstudies.ChatRoom.model.ChatRoomModel
import com.pandaapps.abmsstudies.MainHomeActivity
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.databinding.ActivityChatRoomBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.Exception


class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var chatRoomAdapter: AdapterChatRoom

    private lateinit var progressDialog: ProgressDialog

    private var message = ""

    private var imageUri: Uri? = null

    private companion object {
        private const val TAG = "CHAT_ROOM_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityChatRoomBinding.inflate(layoutInflater)


        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        showsplash()

        initChatList()

        chatRoomAdapter.startListening()

        binding.sendFab.setOnClickListener {
            addMessage()
        }

        binding.attachFab.setOnClickListener {
            OpenExplorer()
        }

        binding.imageBackButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }


    private fun initChatList() {

        val firebaseCords = FirebaseCords()

        binding.chatRoomRv.setHasFixedSize(true)


//        binding.chatRoomRv.layoutManager =
//            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
////        binding.chatRoomRv.layoutManager = LinearLayout(this,LinearLayout.VERTICAL,true)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        layoutManager.stackFromEnd = false
        binding.chatRoomRv.layoutManager = layoutManager

//        app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager"


        val query: Query =
            firebaseCords.MAIN_CHAT_DATABASE.orderBy("timestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatRoomModel>()
            .setQuery(query, ChatRoomModel::class.java)
            .build()



        chatRoomAdapter = AdapterChatRoom(options, this){
            scrollToLastItem()
        }
        binding.chatRoomRv.adapter = chatRoomAdapter
        chatRoomAdapter.startListening()

    }



    private fun addMessage() {
        val date = Date()
        val simpleDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        message = binding.chatBox.text.toString()
        val messageId = simpleDataFormat.format(date)

        if (!TextUtils.isEmpty(message)) {
            val uid = firebaseAuth.currentUser!!.uid
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child("${firebaseAuth.uid}")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val name = "${snapshot.child("name").value}"
                        val profileImageUrl = "${snapshot.child("profileImageURl").value}"
                        Log.d(TAG, "onDataChange: name:$name")
                        Log.d(TAG, "onDataChange: profileImageUrl: $profileImageUrl")
                        Log.d(TAG, "onDataChange: firebaseUid: $firebaseAuth")

                        val hashMap = HashMap<String, Any>()
                        hashMap["message"] = message
                        hashMap["userName"] = name
                        hashMap["timestamp"] = FieldValue.serverTimestamp()
                        hashMap["messageId"] = messageId
                        hashMap["profileImageUrl"] = profileImageUrl
                        hashMap["chat_image"] = ""
                        hashMap["chatTime"] = System.currentTimeMillis()
                        hashMap["uid"] = "${firebaseAuth.uid}"

                        val fireBaseCords = FirebaseCords()
                        fireBaseCords.MAIN_CHAT_DATABASE.document(messageId).set(hashMap)
                            .addOnSuccessListener {
                                Log.d(TAG, "addMessage:Success: $it")
                                Log.d(TAG, "onDataChange: timestamp: ${FieldValue.serverTimestamp()}")
                                Log.d(TAG, "onDataChange: messageId: $messageId")
                                Toast.makeText(this@ChatRoomActivity, "Message Sent", Toast.LENGTH_SHORT).show()
                                chatRoomAdapter.notifyDataSetChanged()
                                binding.chatBox.setText("")
                                scrollToLastItem()
                            }.addOnFailureListener { e ->
                                Toast.makeText(this@ChatRoomActivity, "Failed To Send", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "addMessage: Failed due to ${e.message}")
                            }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.d(TAG, "onCancelled: $error")
                    }
                })
        }
    }


    private fun OpenExplorer() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            ChoseImage()
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 20
                )
            else {
                Toast.makeText(
                    this@ChatRoomActivity,
                    "storage Permission Needed",
                    Toast.LENGTH_SHORT
                ).show()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 20
                )
            }


        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == 20) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@ChatRoomActivity, "Permission Granted", Toast.LENGTH_SHORT)
                    .show()
                ChoseImage()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun ChoseImage() {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .start(this@ChatRoomActivity)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode!! == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {


                val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
                if (resultCode == RESULT_OK) {
                    imageUri = result.uri

                    val intent =
                        Intent(this@ChatRoomActivity, ImageUploadPreviewActivity::class.java)
                    intent.putExtra("imageUri", imageUri.toString())
                    startActivity(intent)
                } else if (requestCode!! == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(
                        this@ChatRoomActivity,
                        "${result.error.message}",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }

            }
        } catch (e: Exception) {
            Log.e(TAG, "onActivityResult: ${e.message}")
        }

    }

    private fun showsplash() {
        val dialog =
            Dialog(this, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.splash_chatroom)
        dialog.setCancelable(true)
        dialog.show()
        val handler = Handler()
        val runnable = Runnable {
            if (firebaseAuth.currentUser != null) {
                dialog.dismiss()
            } else if (firebaseAuth.currentUser == null) {
                startActivity(Intent(this, MainHomeActivity::class.java))

            }

        }
        handler.postDelayed(runnable, 5000)
    }


    private fun scrollToLastItem() {
        binding.chatRoomRv.postDelayed({
            val itemCount = chatRoomAdapter.itemCount
            if (itemCount > 0) {
                binding.chatRoomRv.scrollToPosition(0)
            }
        }, 100)
    }



}