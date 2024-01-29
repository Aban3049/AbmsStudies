package com.pandaapps.abmsstudies.activities

import android.app.Dialog
import android.app.ProgressDialog
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
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.pandaapps.abmsstudies.AboutUsActivity
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityMainHomeBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class MainHomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainHomeBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private companion object {
        private const val TAG = "MAIN_HOME_ACTIVITY_TAG"
    }

    private var userMode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

       // showsplash()

        getUserName()

        if (firebaseAuth.currentUser==null){
            startActivity(Intent(this@MainHomeActivity,LogIn::class.java))
        }else{
            updateFcmToken()
            askNotificationPermission()
        }

         binding.books.setOnClickListener {
               Utils.toast(this@MainHomeActivity,"You clicked on books")
             checkUserMode()
            }
        //    binding.chatRoomCv.setOnClickListener {
        //       Utils.toast(this@MainHomeActivity,"You clicked on Chat Room")
        //    }

        binding.personHomeIv.setOnClickListener {
            startActivity(Intent(this@MainHomeActivity, AccountActivity::class.java))
        }

        binding.feesCv.setOnClickListener {
            startActivity(Intent(this@MainHomeActivity, FeesSlipActivity::class.java))

        }
        binding.buyCv.setOnClickListener {
            startActivity(Intent(this@MainHomeActivity, MainActivity::class.java))
        }
        //     binding.papersCv.setOnClickListener {
        //        Utils.toast(this@MainHomeActivity,"You clicked on Papers")
        //    }
        binding.mathLecturesCv.setOnClickListener {
            Utils.toast(this@MainHomeActivity, "You clicked on Math Lectures")
            startActivity(Intent(this@MainHomeActivity, MathLecturesActivity::class.java))
        }
        //     binding.noticeBoardCv.setOnClickListener {
        //        Utils.toast(this@MainHomeActivity,"You clicked on Notice Board")
        //    }
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

    fun showsplash() {
        val dialog =
            Dialog(this@MainHomeActivity, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.activity_splash_screan)
        dialog.setCancelable(true)
        dialog.show()
        val handler = Handler()
        val runnable = Runnable {
            if (firebaseAuth.currentUser != null) {
                dialog.dismiss()
            } else if (firebaseAuth.currentUser == null) {
                startActivity(Intent(this@MainHomeActivity, LogIn::class.java))

            }

        }
        handler.postDelayed(runnable, 5000)
    }

    private fun checkUserMode(){
        if (userMode =="USER"){
            Toast.makeText(this@MainHomeActivity, "User Mode", Toast.LENGTH_SHORT).show()
        }
        else{
            startActivity(Intent(this@MainHomeActivity,BooksAdminDashboardActivity::class.java))
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
    ){isGranted ->

        Log.d(TAG, "requestNotificationPermission: isGranted: $isGranted")
    }


}









