package com.pandaapps.abmsstudies.sell.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var binding:ActivityForgotPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private companion object{
        private const val TAG ="FORGOT_PASSWORD"
    }
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityForgotPasswordBinding.inflate(layoutInflater)
        firebaseAuth=FirebaseAuth.getInstance()
        setContentView(binding.root)

        progressDialog= ProgressDialog(this@ForgotPasswordActivity)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.ToolbarBackbtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }
    private var email = ""
    private fun validateData(){
        email=binding.emailEt.text.toString().trim()
        Log.d(TAG,"validateData : email: $email")
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.error="Invalid Email Pattern"
            binding.emailEt.requestFocus()
        }
        else{
            sendPasswordRecoveryInstructions()
        }
    }
    private fun sendPasswordRecoveryInstructions(){
        Log.d(TAG,"sendPasswordRecoveryInstructions")

        // show progress
        progressDialog.setMessage("Sending Password reset link to $email")
        progressDialog.show()
        firebaseAuth.sendPasswordResetEmail(email).addOnSuccessListener {
            progressDialog.dismiss()
            Utils.toast(this@ForgotPasswordActivity, "Link sent to $email")
        }.addOnFailureListener {e ->
            Log.e(TAG,"sendPasswordRecoveryLink: ",e)
            progressDialog.dismiss()
            Utils.toast(this@ForgotPasswordActivity, "Failed to send due to ${e.message}")
        }
    }
}