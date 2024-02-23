package com.pandaapps.abmsstudies.ChatRoom.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.text.TextUtils
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
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
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.books.activities.PdfAddActivityBooks
import com.pandaapps.abmsstudies.databinding.ActivityChatRoomBinding
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.text.SimpleDateFormat
import java.util.Date

class ChatRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatRoomBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var chatRoomAdapter: AdapterChatRoom

    private var message = ""

    private var imageUri: Uri? = null

    private companion object {
        private const val TAG = "CHAT_ROOM_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityChatRoomBinding.inflate(layoutInflater)


        firebaseAuth = FirebaseAuth.getInstance()

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initChatList()

        chatRoomAdapter.startListening()

        binding.sendFab.setOnClickListener {
            addMessage()
        }

        binding.attachFab.setOnClickListener {
            OpenExplorer()
        }

    }


    private fun initChatList() {

        val firebaseCords = FirebaseCords()

        binding.chatRoomRv.setHasFixedSize(true)


        binding.chatRoomRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
//        binding.chatRoomRv.layoutManager = LinearLayout(this,LinearLayout.VERTICAL,true)

        val query: Query =
            firebaseCords.MAIN_CHAT_DATABASE.orderBy("timestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatRoomModel>()
            .setQuery(query, ChatRoomModel::class.java)
            .build()



        chatRoomAdapter = AdapterChatRoom(options, this)
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
                .addValueEventListener(object : ValueEventListener {
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


                        val fireBaseCords = FirebaseCords()
                        fireBaseCords.MAIN_CHAT_DATABASE.document(messageId).set(hashMap)
                            .addOnSuccessListener {
                                Log.d(TAG, "addMessage:Success: $it")
                                Log.d(
                                    TAG,
                                    "onDataChange: timestamp: ${FieldValue.serverTimestamp()}"
                                )
                                Log.d(TAG, "onDataChange: messageId: $messageId")

                                Toast.makeText(
                                    this@ChatRoomActivity,
                                    "Message Sent",
                                    Toast.LENGTH_SHORT
                                ).show()
                                binding.chatBox.setText("")
                            }.addOnFailureListener { e ->
                                Toast.makeText(
                                    this@ChatRoomActivity,
                                    "Failed To Send",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
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
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            val result: CropImage.ActivityResult = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                imageUri = result.uri

                val intent = Intent(this@ChatRoomActivity, ImageUploadPreviewActivity::class.java)
                intent.putExtra("imageUri", imageUri.toString())
                startActivity(intent)
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this@ChatRoomActivity, "${result.error.message}", Toast.LENGTH_SHORT)
                    .show()
            }

        }
    }

}