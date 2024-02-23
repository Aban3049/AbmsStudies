package com.pandaapps.abmsstudies.NoticeBoard.activities

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.NoticeBoard.NoticeDetailActivity
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.books.activities.PdfAddActivityBooks
import com.pandaapps.abmsstudies.databinding.ActivityNoticeAdminBinding
import org.json.JSONObject
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.ByteArrayOutputStream
import java.util.Random

class NoticeAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoticeAdminBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri? = null

    private companion object {
        private const val TAG = "NOTICE_ADMIN_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityNoticeAdminBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setMessage("Please wait...")

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ImagePickBtn.setOnClickListener {
            showImagePickOptions()
        }

        binding.closeBtn.setOnClickListener {
            try {

                Log.d(TAG, "onBindViewHolder: imageUri: $imageUri")

                Glide.with(this)
                    .clear(binding.imageIv)
            } catch (e: Exception) {
                Log.e(TAG, "onBindViewHolder:", e)
            }

        }

        binding.publishNoticeBtn.setOnClickListener {
            validateData()
        }

    }

    private var noticeTitle = ""
    private var uploadImageUrl = ""
    private var noticeDescription = ""

    private fun validateData() {

        Log.d(TAG, "validateData: ")

        noticeTitle = binding.noticeEt.text.toString().trim()
        noticeDescription = binding.descriptionEt.text.toString().trim()

        if (noticeTitle.isEmpty()) {
            binding.noticeEt.requestFocus()
            binding.noticeEt.error = "Field Cannot be empty"
        }
        else if (noticeDescription.isEmpty()){
            binding.descriptionEt.requestFocus()
            binding.descriptionEt.error = "Field cannot be empty"
        }

        else if(imageUri ==null){
            MotionToast.createColorToast(
                this@NoticeAdminActivity,
                "Warning",
                "Pick Image",
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
            )
        }
        else {
            //data validate begin upload
            uploadBookImageToStorage()
        }

    }

    private fun uploadBookImageToStorage() {
        Log.d(TAG, "uploadPdfToStorage: uploading to storage....")

        //show progress dialog
        progressDialog.setMessage("Uploading Image...")
        progressDialog.show()

        //timestamp in
        val timestamp = Utils.getTimestamp()

        //path f pdf in firebase storage

        val filePathAndName = "NoticeImage/$timestamp"


        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        // compressing image
        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
        val reducedImage: ByteArray = byteArrayOutputStream.toByteArray()

        storageReference.putBytes(reducedImage)
            .addOnSuccessListener { taskSnapshot ->

                Log.d(TAG, "uploadPdfToStorage: Image uploaded now getting url ...")

                //get url of uploaded Image
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);

                uploadImageUrl = "${uriTask.result}"
                uploadNoticeInfoToDb(uploadImageUrl, timestamp)
            }
            .addOnFailureListener { e ->

                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@NoticeAdminActivity,
                    "Pick Again",
                    "Failed to Get Image due to ${e.message}",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )


                Log.i(TAG, "Failed uploading image to server")

            }
            .addOnProgressListener { it ->

                val dataTransferred = (100 * it.bytesTransferred / it.totalByteCount)
                progressDialog.setMessage("Uploading $dataTransferred%")
            }

    }

    private fun uploadNoticeInfoToDb(uploadNoticeUrl: String, timestamp: Long) {
// upload pdf to firebase db
        Log.d(TAG, "uploadPdfInfoToDb: uploading to db")
        progressDialog.setMessage("Uploading pdf info....")

        //uid of  current user
        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any> = HashMap()


        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$noticeTitle"
        hashMap["description"] = "$noticeDescription"
        hashMap["url"] = "$uploadNoticeUrl"
        hashMap["timestamp"] = timestamp
        hashMap["imageUrl"] = "$uploadImageUrl"


        val ref = FirebaseDatabase.getInstance().getReference("Notices")
        ref.child("$timestamp")
            .setValue(hashMap)

            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: uploaded to db")
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@NoticeAdminActivity,
                    "Success",
                    "Successfully Uploaded",
                    MotionToastStyle.SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )

                prepareNotification(noticeTitle)

                imageUri = null

            }

            .addOnFailureListener { e ->
                Log.d(TAG, "uploadPdfToStorage: failed to upload due to ${e.message}")
                progressDialog.dismiss()
                MotionToast.createColorToast(
                    this@NoticeAdminActivity,
                    "Pick Again",
                    "Failed to Get Pdf due to ${e.message}",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
                )
            }

    }

    private fun loadImages() {
        Log.d(TAG, "loadImages: ")
        try {

            Log.d(TAG, "onBindViewHolder: imageUri: $imageUri")

            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.ic_image_gray)
                .into(binding.imageIv)
        } catch (e: Exception) {
            Log.e(TAG, "onBindViewHolder:", e)
        }

    }

    private fun showImagePickOptions() {
        Log.d(TAG, "showImagePickOptions: ")

        //s how popUp on Button
        val popupMenu = PopupMenu(this, binding.ImagePickBtn)
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.menu.add(Menu.NONE, 2, 2, "Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
// get the id of the item clicked in popup menu
            val itemId = item.itemId
            // check which item is clicked from popUp menu, 1=Camera. 2=Gallery as we defined
            if (itemId == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    val cameraPermission = arrayOf(android.Manifest.permission.CAMERA)
                    requestCameraPermission.launch(cameraPermission)
                } else {
                    // Device version is TIRAMISU,We need Storage permission to launch Camera
                    val cameraPermissions = arrayOf(
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    requestCameraPermission.launch(cameraPermissions)
                }
            } else if (itemId == 2) {
                // Gallery is clicked we need to check if we have permission of Storage before launching Gallery to Pick image
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pickImageGallery()
                } else {
                    // Device version is TIRAMISU,We need Storage permission to launch Gallery
                    val storagePermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                    requestStoragePermission.launch(storagePermission)
                }
            }

            true
        }
    }

    private val requestStoragePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "requestStoragePermission: isGranted: $isGranted")

        if (isGranted) {
            // Let's check if permission is granted or not
            pickImageGallery()
        } else {
            //Storage Permission denied, we can't launch gallery to pick images
            Utils.toast(this, "Storage Permission denied.... ")
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        Log.d(TAG, "requestCameraPermission: result: $result")
        //let's check if permissions are granted or not
        var areAllGranted = true
        for (isGranted in result.values) {
            areAllGranted = areAllGranted && isGranted
        }
        if (areAllGranted) {
            //All Permission Camera,Storage are granted,we can now launch camera to capture image
            pickImageCamera()
        } else {
            //Camera or Storage or Both permission are denied, Can't launch camera to capture image
            Utils.toast(this, "Camera or Storage or both permission denied...")
        }

    }

    private fun pickImageGallery() {
        Log.d(TAG, "pickImageGallery:")
        val intent = Intent(Intent.ACTION_PICK)
        // we only want to pick Images
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageCamera() {
        Log.d(TAG, "pickImageCamera:")

        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.Media.TITLE, "TEMP_IMAGE_TITLE")
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "TEMP_IMAGE_DESCRIPTION")
        // Uri of image to be captured from camera
        imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        //Intent to launch Camera
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "galleryActivityResultLauncher :")
        // Check if image is picked or not
        if (result.resultCode == Activity.RESULT_OK) {

            //get data from result param
            val data = result.data
            //get uri of image picked
            imageUri = data!!.data
            Log.d(TAG, "galleryActivityResultLauncher: imageUri: $imageUri")

            //timestamp will be used as id of image picked
            val timestamp = "${Utils.getTimestamp()}"

            //setup model for image,Param 1 is id,Param 2 is imageUri, Param 3 is imageUrl , from internet
            loadImages()
        } else {
            // Cancelled
            Utils.toast(this, "Cancelled...!")
        }
// Check if image is picked or not
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "cameraActivityResultLauncher:")
        //Check if image is picked or not
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "cameraActivityResultLauncher: imageUri $imageUri")

            val timestamp = "${Utils.getTimestamp()}"


            loadImages()
        } else {
            Utils.toast(this, "Cancelled...!")
        }
    }

    private fun prepareNotification(message: String) {

        Log.d(TAG, "prepareNotification: ")

        val notificationJo = JSONObject()
        val notificationDataJo = JSONObject()
        val notificationNotificationJo = JSONObject()

        try {

            notificationDataJo.put("notificationType", "${Utils.NOTIFICATION_TYPE_NEW_MESSAGE}")

            notificationNotificationJo.put("title", "$noticeTitle")
            notificationNotificationJo.put("body", "$message")
            notificationNotificationJo.put("sound", "default")


            notificationJo.put("notification", notificationNotificationJo)
            notificationJo.put("data", notificationDataJo)

        } catch (e: java.lang.Exception) {
            Log.e(TAG, "prepareNotification: ", e)
        }

        sendFcmNotification(notificationJo)


    }

    private fun sendFcmNotification(notificationJo: JSONObject) {

        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            "https://fcm.googleapis.com/fcm/send",
            notificationJo,
            Response.Listener {
                // Notification sent
                Log.d(TAG, "sendFcmNotification: Notification Send $it")

            },
            Response.ErrorListener { e ->

                //Notification failed to sent
                Log.e(TAG, "sendFcmNotification: ",e )

            }

        ) {

            override fun getHeaders(): MutableMap<String, String> {

                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/Json"
                headers["Authorization"] = "key=${Utils.FCM_SERVER_KEY}"

                return headers

            }

        }

        Volley.newRequestQueue(this).add(jsonObjectRequest)

    }

    class MyFcmService2: FirebaseMessagingService() {

        private companion object{
            private const val TAG ="MY_FCM_TAG"

            //Notification Channel ID

            private const val NOTIFICATION_CHANNEL_ID = "ABMS_CHANNEL_TO"

        }

        override fun onMessageReceived(remoteMessage: RemoteMessage) {
            super.onMessageReceived(remoteMessage)

            val title = "${remoteMessage.notification?.title}"
            val body = "${remoteMessage.notification?.body}"

            val notificationType = "${remoteMessage.data["notificationType"]}"

            Log.d(TAG, "onMessageReceived: title: $title")
            Log.d(TAG, "onMessageReceived: body: $body")
            Log.d(TAG, "onMessageReceived: notificationType: $notificationType")

            //function cal to show notification
            showChatNotification(title,body)

        }

        private fun showChatNotification(notificationTitle:String,notificationDescription:String){
            // generate random number for notification id
            val notificationId = Random().nextInt(3000)


            //init notification Manger
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // function call to setup notification channel in android 0 and above

            setupNotificationChannel(notificationManager)

            val intent = Intent(this,NoticeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            // pending intent to add in Notification
            val pendingIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_IMMUTABLE)

            val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)

            notificationManager.notify(notificationId,notificationBuilder.build())
        }

        private fun setupNotificationChannel(notificationManager: NotificationManager){


            // Android 8.0 all notification Must be Assigned to a Channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

                val notificationChannel = NotificationChannel(

                    NOTIFICATION_CHANNEL_ID,
                    "Chat Chanel",
                    NotificationManager.IMPORTANCE_HIGH
                )

                notificationChannel.description = "Show Chats Notifications"
                notificationChannel.enableVibration(true)

                notificationManager.createNotificationChannel(notificationChannel)

            }

        }

    }


}