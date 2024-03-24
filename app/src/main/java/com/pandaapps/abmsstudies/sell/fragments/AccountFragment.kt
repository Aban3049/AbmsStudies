package com.pandaapps.abmsstudies.sell.fragments

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.sell.activities.ChangePasswordActivity
import com.pandaapps.abmsstudies.sell.activities.DeleteAccountActivity
import com.pandaapps.abmsstudies.sell.activities.ProfileEditActivity
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.sell.activities.LogIn
import com.pandaapps.abmsstudies.databinding.FragmentAccountBinding


class AccountFragment : Fragment() {
    private lateinit var mContext: Context
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: FragmentAccountBinding
    private lateinit var progressDialog: ProgressDialog

    private companion object {
        private const val TAG = "ACCOUNT_TAG"
    }

    override fun onAttach(context: Context) {
        mContext = context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(mContext)
        progressDialog.setTitle("please wait....")
        progressDialog.setCanceledOnTouchOutside(false)



        loadMyInfo()

        binding.logoutCv.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(mContext, LogIn::class.java))
            activity?.finish()
        }
        binding.editProfileCv.setOnClickListener {
            startActivity(Intent(mContext, ProfileEditActivity::class.java))
        }
        binding.changePasswordCv.setOnClickListener {
            startActivity(Intent(mContext, ChangePasswordActivity::class.java))
        }
        binding.verifyAccountCv.setOnClickListener {

            verifyAccount()
        }
        binding.deleteAccountCv.setOnClickListener {
            startActivity(Intent(mContext, DeleteAccountActivity::class.java))
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

                    try {
                        Glide.with(mContext)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.profileIv)
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChanged", e)
                    }


                }

                override fun onCancelled(error: DatabaseError) {

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
                Utils.toast(mContext, "Failed to send verification link due to ${e.message}")

            }
        } catch (e: Exception) {
            Utils.toast(mContext, "${e.message}")

        }


    }

}