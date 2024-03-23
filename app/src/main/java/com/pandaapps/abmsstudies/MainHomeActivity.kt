package com.pandaapps.abmsstudies

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.pandaapps.abmsstudies.ChatRoom.activities.ChatRoomActivity
import com.pandaapps.abmsstudies.NoticeBoard.activities.NoticeActivity
import com.pandaapps.abmsstudies.NoticeBoard.activities.NoticeAdminActivity
import com.pandaapps.abmsstudies.about.AboutUsActivity
import com.pandaapps.abmsstudies.books.activities.BooksAdminDashboardActivity
import com.pandaapps.abmsstudies.books.activities.BooksDashboardUserActivity
import com.pandaapps.abmsstudies.databinding.ActivityMainHomeBinding
import com.pandaapps.abmsstudies.gallery.activities.GalleryActivity
import com.pandaapps.abmsstudies.mathLecture.MathLecturesActivity
import com.pandaapps.abmsstudies.papers.activities.AdminPaperActivity
import com.pandaapps.abmsstudies.papers.activities.PaperUserActivity
import com.pandaapps.abmsstudies.sell.activities.LogIn
import com.pandaapps.abmsstudies.sell.activities.MainActivity

class MainHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private companion object {
        private const val TAG = "MAIN_HOME_ACTIVITY_TAG"
    }

    private var userMode = ""

  private  var isGuestMode: Boolean? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

         showsplash()

        val sharedPref = getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE)
        isGuestMode = sharedPref.getBoolean("GuestMode", false)
        Log.d(TAG, "onCreate: checkingGuestUser:$isGuestMode")


        getUserName()

        if (firebaseAuth.currentUser == null) {
            startActivity(Intent(this@MainHomeActivity, LogIn::class.java))
        } else {
            updateFcmToken()
            askNotificationPermission()
        }

        binding.books.setOnClickListener {
            checkUserMode()
        }
        binding.chatRoomCv.setOnClickListener {
            startActivity(Intent(this@MainHomeActivity, ChatRoomActivity::class.java))
        }

        binding.personHomeIv.setOnClickListener {
            startActivity(Intent(this@MainHomeActivity, AccountActivity::class.java))
        }

        binding.buyCv.setOnClickListener {
            startActivity(Intent(this@MainHomeActivity, MainActivity::class.java))
        }

        binding.mathLecturesCv.setOnClickListener {
            Utils.toast(this@MainHomeActivity, "You clicked on Math Lectures")
            startActivity(Intent(this@MainHomeActivity, MathLecturesActivity::class.java))
        }

        binding.noticeCv.setOnClickListener {
            if (userMode == "USER") {
                startActivity(Intent(this, NoticeActivity::class.java))
            } else {
                startActivity(Intent(this@MainHomeActivity, NoticeAdminActivity::class.java))
            }
        }


        binding.accountCv.setOnClickListener {
            startActivity(Intent(this@MainHomeActivity, AccountActivity::class.java))
        }

        binding.logoutCv.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this@MainHomeActivity, LogIn::class.java))
            finish()
        }
        binding.aboutUsCv.setOnClickListener {
            startActivity(Intent(this@MainHomeActivity, AboutUsActivity::class.java))

        }
        binding.galleryCv.setOnClickListener {
            startActivity(Intent(this@MainHomeActivity, GalleryActivity::class.java))
        }
        binding.paperCv.setOnClickListener {
            if (userMode == "USER") {
                startActivity(Intent(this@MainHomeActivity, PaperUserActivity::class.java))
            } else {
                startActivity(Intent(this@MainHomeActivity, AdminPaperActivity::class.java))
            }

        }


    }


    private fun getUserName() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = "${snapshot.child("name").value}"
                val profileImageURl = "${snapshot.child("profileImageURl").value}"
                userMode = "${snapshot.child("userMode").value}"

                binding.nameTv.text = name

                try {
                    Glide.with(this@MainHomeActivity)
                        .load(profileImageURl)
                        .placeholder(R.drawable.ic_person_white)
                        .into(binding.personHomeIv)
                } catch (e: Exception) {
                    Log.e(TAG, "onDataChanged :", e)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: $error")

            }
        })
    }



    private fun showsplash() {
        val dialog =
            Dialog(this@MainHomeActivity, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_splash_screan)
        dialog.setCancelable(true)
        dialog.show()
        val handler = Handler()
        val runnable = Runnable {
            if (firebaseAuth.currentUser != null ||isGuestMode== true) {
                dialog.dismiss()
            } else if (firebaseAuth.currentUser == null) {
                startActivity(Intent(this@MainHomeActivity, LogIn::class.java))
                finish()

            }

        }
        handler.postDelayed(runnable, 5000)
    }

    private fun checkUserMode() {
        if (userMode == "USER") {
            startActivity(Intent(this, BooksDashboardUserActivity::class.java))
        } else {
            startActivity(Intent(this@MainHomeActivity, BooksAdminDashboardActivity::class.java))
        }
    }

    private fun updateFcmToken() {
        val myUid = "${firebaseAuth.uid}"
        Log.d(TAG, "updateFcmToken: ")

        FirebaseMessaging.getInstance().token

            .addOnSuccessListener { fcmToken ->

                Log.d(TAG, "updateFcmToken: fcmToken $fcmToken")
                val hashMap = HashMap<String, Any>()
                hashMap["fcmToken"] = "$fcmToken"

                val ref = FirebaseDatabase.getInstance().getReference("Users")
                ref.child(myUid)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        Log.d(TAG, "updateFcmToken: FCM Token update to db success")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "updateFcmToken: ", e)
                    }

            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateFcmToken: ", e)
            }

    }

    private fun askNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_DENIED
            ) {
                requestNotificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }

        }

    }

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->

        Log.d(TAG, "requestNotificationPermission: isGranted: $isGranted")
    }


}









