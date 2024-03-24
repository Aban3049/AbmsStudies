package com.pandaapps.abmsstudies

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.databinding.ActivityAccountBinding
import com.pandaapps.abmsstudies.sell.activities.ChangePasswordActivity
import com.pandaapps.abmsstudies.sell.activities.DeleteAccountActivity
import com.pandaapps.abmsstudies.sell.activities.LogIn
import com.pandaapps.abmsstudies.sell.activities.ProfileEditActivity

class AccountActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAccountBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog:ProgressDialog

    private companion object {
        private const val TAG = "ACCOUNT_ACTIVITY_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth=FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this@AccountActivity)
        progressDialog.setTitle("please wait....")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


        loadMyInfo()

        binding.logoutCv.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this@AccountActivity , LogIn::class.java))
            finish()

        }

        binding.editProfileCv.setOnClickListener {
            startActivity(Intent(this@AccountActivity, ProfileEditActivity::class.java))
        }

        binding.changePasswordCv.setOnClickListener {
            startActivity(Intent(this@AccountActivity, ChangePasswordActivity::class.java))
        }
        binding.verifyAccountCv.setOnClickListener {
            verifyAccount()
        }
        binding.deleteAccountCv.setOnClickListener {
            startActivity(Intent(this@AccountActivity, DeleteAccountActivity::class.java))
        }


    }


    private fun loadMyInfo() {

        //Retriving data from DataBase

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val dob = "${snapshot.child("dob").value}"
                    val email = "${snapshot.child("email").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val profileImageUrl = "${snapshot.child("profileImageURl").value}"
                    var timestamp = "${snapshot.child("timestamp").value}"
                    val userType = "${snapshot.child("userType").value}"
                    val name = "${snapshot.child("name").value}"

                    val phone = phoneCode + phoneNumber

                    if (timestamp == "null") {
                        timestamp = "0"
                    }
                    //format time to dd/MM/yyyy
                    val formattedData = Utils.formatTimestampDate(timestamp.toLong())

                    //Set data to UI

                    binding.emailTv.text = email
                    binding.nameTv.text = name
                    binding.dobTv.text = dob
                    binding.phoneTv.text = phone
                    binding.memberSinceTv.text = formattedData

                    try {
                        Glide.with(this@AccountActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_black)
                            .into(binding.profileIvAccount)
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChanged", e)
                    }


                    if (userType == "Email") {
                        val isVerified = firebaseAuth.currentUser!!.isEmailVerified
                        if (isVerified) {
                            binding.verificationTv.text = getString(R.string.verified)
                            binding.verifyAccountCv.visibility = View.GONE
                        } else {
                            binding.verificationTv.text = getString(R.string.not_verified)
                            binding.verifyAccountCv.visibility = View.VISIBLE
                        }
                    } else {
                        binding.verificationTv.text = getString(R.string.verified)
                        binding.verifyAccountCv.visibility = View.GONE
                    }

                    // set Profile Image to ImageView




                }

                override fun onCancelled(error: DatabaseError) {
                  Log.e(TAG,"onDataChanged:$error")
                }


            })


    }

    private fun verifyAccount() {
        Log.d(TAG, "verifying Account: ")

        try {
            progressDialog.setMessage("Sending verification link to email:")
            progressDialog.show()
            firebaseAuth.currentUser!!.sendEmailVerification().addOnSuccessListener {
                Log.d(TAG, "Account Verification link: sent successfully")
                progressDialog.dismiss()


            }.addOnFailureListener { e ->
                Log.e(TAG, "Failed to send verification link:", e)
                progressDialog.dismiss()
                Utils.toast(
                    this@AccountActivity,
                    "Failed to send verification link due to ${e.message}"
                )

            }
        } catch (e: Exception) {
            Utils.toast(this@AccountActivity, "${e.message}")

        }


    }
}