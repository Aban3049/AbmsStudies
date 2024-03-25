package com.pandaapps.abmsstudies.sell.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.pandaapps.abmsstudies.MainHomeActivity
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityLogInBinding
import java.lang.Exception
import android.content.Context

class LogIn : AppCompatActivity() {

    private companion object {
        private const val TAG = "LOGIN_OPTIONS_TAG"
    }

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding: ActivityLogInBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


//
        binding.LogInEmailBtn.setOnClickListener {
            startActivity(Intent(this@LogIn, LogInEmailActivity::class.java))
        }

        binding.LogInGoogleBtn.setOnClickListener {
            beginGoogleLogin()
        }
        binding.LogInPhoneBtn.setOnClickListener {
            startActivity(Intent(this@LogIn, LoginPhone::class.java))
        }

        binding.LogInGuestBtn.setOnClickListener {


            val sharedPref = getSharedPreferences("MySharedPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putBoolean("GuestMode", true)
                commit() // Changed from apply() to commit()
            }
          startMainHomeActivity(guestMode = true)
            Utils.toast(this@LogIn,"Guest Mode Capacity is At Limit! Try Using Another Method")

        }

    }
    private fun startMainHomeActivity(guestMode: Boolean) {
        val intent = Intent(this, MainHomeActivity::class.java).apply {
            putExtra("GuestMode", guestMode)
        }
        startActivity(intent)
        finish() // Optional: Finish the current activity if needed
    }


    private fun beginGoogleLogin() {
        Log.d(TAG, "beginGoogleLogin:")
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        googleSignnInARL.launch(googleSignInIntent)
    }

    private val googleSignnInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "googleSignInARL: ")
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "googleSignInARL: Account ID: ${account.id}")
                firebaseAuthWithGoogleAccount(account.idToken)
            } catch (e: Exception) {
                Log.d(TAG, "googleSignInARL", e)
                Utils.toast(this@LogIn, "${e.message}")
            }

        } else {
            Utils.toast(this@LogIn, "Cancelled...!")
        }

    }

    private fun firebaseAuthWithGoogleAccount(idToken: String?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken : $idToken")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                if (authResult.additionalUserInfo!!.isNewUser) {
                    Log.d(TAG, "firebaseAuthWithGoogle: New User, Account created....")
                    updateUserInfoDb()
                } else {
                    Log.d(TAG, "firebaseAuthWithGoogle: Existing User, Logged In....")
                    startActivity(Intent(this@LogIn, MainHomeActivity::class.java))
                    finishAffinity()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "firebaseAuthWithGoogleAccount: ", e)
                Utils.toast(this@LogIn, "${e.message}")

            }
    }

    private fun updateUserInfoDb() {
        Log.d(TAG, "Saving User Info")

        progressDialog.setTitle("Saving User Info")
        progressDialog.show()

        val timestamp = Utils.getTimestamp()
        val registerUserEmail = firebaseAuth.currentUser?.email
        val registerUserUid = firebaseAuth.uid
        val name = firebaseAuth.currentUser?.displayName

        val hashMap = HashMap<String, Any?>()

        hashMap["name"] = "$name"
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageURl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Google"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = "$registerUserEmail"
        hashMap["uid"] = "$registerUserUid"
        hashMap["userMode"] = "${Utils.USER_MODE}"

        // set data to firebase

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(registerUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updatedUserInfoDb: User info saved")
                progressDialog.dismiss()
                startActivity(Intent(this, MainHomeActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "updateUserInfoDb", e)
                Utils.toast(this, "Failed to save user info due to ${e.message}")
            }


    }

}