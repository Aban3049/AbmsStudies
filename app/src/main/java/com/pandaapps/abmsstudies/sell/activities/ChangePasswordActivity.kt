package com.pandaapps.abmsstudies.sell.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding:ActivityChangePasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private companion object{
        private const val TAG = "CHANGE_PASSWORD_TAG"
    }
    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseUser:FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth=FirebaseAuth.getInstance()
        firebaseUser=firebaseAuth.currentUser!!

       progressDialog= ProgressDialog(this@ChangePasswordActivity)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressed()
        }
        binding.changePasswordBtn.setOnClickListener {
            validateData()
        }


    }

    private var currentPassword =""
    private var newPassword=""
    private var confirmNewPassword=""


    private fun validateData(){
        currentPassword=binding.currentPasswordEt.text.toString().trim()
        newPassword=binding.newPasswordEt.text.toString().trim()
        confirmNewPassword=binding.newConfirmPasswordEt.text.toString().trim()

        Log.d(TAG,"validateData: currentPassword: $currentPassword")
        Log.d(TAG,"validateData: newPassword: $newPassword")
        Log.d(TAG,"validateData: currentNewPassword: $confirmNewPassword")

        if (currentPassword.isEmpty()){
            binding.currentPasswordEt.error="Enter Current Password"
            binding.currentPasswordEt.requestFocus()
        }else if (newPassword.isEmpty()){

            binding.newPasswordEt.error="Enter new Password"
            binding.newPasswordEt.requestFocus()
        }else if (confirmNewPassword.isEmpty()){
            binding.newConfirmPasswordEt.error="Enter Confirm password"
            binding.newConfirmPasswordEt.requestFocus()
        }else if (newPassword != confirmNewPassword){
            binding.newConfirmPasswordEt.error ="Password Mismatch"
            binding.newConfirmPasswordEt.requestFocus()
        }else{
            authenticateUserForUpdatePassword()
        }

    }

    private fun authenticateUserForUpdatePassword(){
        progressDialog.setMessage("Authenticating user")
        progressDialog.show()

        val authCredential = EmailAuthProvider.getCredential(firebaseUser.email.toString(), currentPassword)
        firebaseUser.reauthenticate(authCredential)
            .addOnSuccessListener {
                Log.d(TAG,"authenticateUserForUpdatePassword : Auth success")

            }.addOnFailureListener {e ->
                Log.e(TAG,"authenticateUserForUpdatePassword:" ,e)
                Utils.toast(
                    this@ChangePasswordActivity,
                    "Failed to authenticate due to ${e.message}"
                )
            }
    }

    private fun updatePassword(){
        progressDialog.setMessage("Checking Password")
        progressDialog.show()

        firebaseUser.updatePassword(newPassword).addOnSuccessListener {
            Log.d(TAG,"updatePassword: Password Updated....")
            progressDialog.dismiss()
            Utils.toast(this@ChangePasswordActivity, "Password updated... !")
        }.addOnFailureListener {e ->
            Log.d(TAG,"updatePassword: ", e)
            progressDialog.dismiss()
            Utils.toast(
                this@ChangePasswordActivity,
                "Failed to update Password due to ${e.message}"
            )
        }
    }

}