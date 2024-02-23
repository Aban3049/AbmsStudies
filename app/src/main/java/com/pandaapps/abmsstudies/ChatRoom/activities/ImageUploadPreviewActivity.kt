package com.pandaapps.abmsstudies.ChatRoom.activities

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.ChatRoom.FirebaseCords.FirebaseCords
import com.pandaapps.abmsstudies.databinding.ActivityImageUploadPreviewBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class ImageUploadPreviewActivity : AppCompatActivity() {

    private lateinit var upload_Image_Container: ImageView

    private lateinit var progressDialog: ProgressDialog

    private var imageUri: Uri? = null

    private var message = ""

    private var firebaseAuth = FirebaseAuth.getInstance()

    private lateinit var binding: ActivityImageUploadPreviewBinding

    private companion object {
        private const val TAG = "ADD_IMAGE_CHATROOM_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityImageUploadPreviewBinding.inflate(layoutInflater)

        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Please Wait....")

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        upload_Image_Container = binding.uploadImageContainer

        imageUri = Uri.parse(intent.getStringExtra("imageUri"))!!

        upload_Image_Container.setImageURI(imageUri)

        binding.floatingActionBackButton.setOnClickListener {
            finish()
        }

        binding.sendFab.setOnClickListener {
            addMessage()
        }


    }

    private var uploadImageUrl = ""
    private fun addMessage() {
        progressDialog.setMessage("Uploading Image....")
        progressDialog.show()

        val date = Date()
        val simpleDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val messageId = simpleDataFormat.format(date)
        val timestamp = FieldValue.serverTimestamp()

        val imgPath = "chat_Room_Image/$messageId"

        val storageReference = FirebaseStorage.getInstance().getReference(imgPath)
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
        val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()

        storageReference.putBytes(reducedImage)


       storageReference.putBytes(reducedImage)
            .addOnSuccessListener {
                    taskSnapshot ->

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);

                uploadImageUrl = "${uriTask.result}"
                addMessageToDb(uploadImageUrl)


                Log.d(TAG, "addMessage: Success")
                Log.d(TAG, "addMessage: $uploadImageUrl")
                progressDialog.setMessage("Updating info to Db")

            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to send due to ${it.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e(TAG, "addMessage: Failed due to ${it.message}")

            }.addOnProgressListener {
                val sendBytes = (it.bytesTransferred / it.totalByteCount) * 100
                progressDialog.setMessage("Sending $sendBytes% ")
                Log.d(TAG, "addMessage: Progress:$sendBytes")
            }
    }

    private fun addMessageToDb(imageUri: String) {


        val uid = firebaseAuth.currentUser!!.uid

        val date = Date()
        val simpleDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val messageId = simpleDataFormat.format(date)

        message = binding.chatBox.text.toString()

        if (!TextUtils.isEmpty(message)) {
            message = "\uD83D\uDCF7"
        }

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
                    hashMap["chat_image"] = imageUri.toString()

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
                                this@ImageUploadPreviewActivity,
                                "Message Sent",
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.chatBox.setText("")
                            progressDialog.dismiss()
                            finish()
                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                this@ImageUploadPreviewActivity,
                                "Failed To Send",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            Log.e(TAG, "addMessage: Failed due to ${e.message}")
                            progressDialog.dismiss()
                        }


                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: $error")
                }

            })
    }
}