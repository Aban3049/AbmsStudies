package com.pandaapps.abmsstudies.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.pandaapps.abmsstudies.R

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screan)


        firebaseAuth=FirebaseAuth.getInstance()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (firebaseAuth.currentUser != null) {
                val intent = Intent(this, MainHomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                startActivity(Intent(this@SplashScreenActivity, LogIn::class.java))
                finish()
            }

        },10000)


//
//        try {
//            Thread.sleep(10000)
//        } catch (e: Exception) {
//            Toast.makeText(this@SplashScreenActivity, "This is Exception $e", Toast.LENGTH_LONG)
//                .show()
//        }
      /*  finally {


        }*/


    }
}