package com.pandaapps.abmsstudies.sell.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.pandaapps.abmsstudies.MainHomeActivity
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityLogInEmailBinding

class LogInEmailActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLogInEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private companion object{
        private const val TAG = "LOGIN_TAG"
    }
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth=FirebaseAuth.getInstance()
        binding=ActivityLogInEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
     progressDialog.setTitle("Please wait....")
     progressDialog.setCanceledOnTouchOutside(false)

     binding.ToolbarBackbtn.setOnClickListener {
         onBackPressed()

     }
        binding.registerBtn.setOnClickListener {
            val intent = Intent(this@LogInEmailActivity, RegisterEmailActivity::class.java)
            startActivity(intent)
        }

        binding.logInBtn.setOnClickListener {
            validateData()
        }

        binding.forgotPasswordTv.setOnClickListener {
       startActivity(Intent(this@LogInEmailActivity, ForgotPasswordActivity::class.java))
        }



    }

    private var email = ""
    private var password = ""

    private fun validateData(){
        email=binding.emailEt.text.toString().trim()
        password=binding.passwordEt.text.toString().trim()

        Log.d(TAG,"validate email: $email")
        Log.d(TAG,"validate password: $password")

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.error="Invalid Email Format"
            binding.emailEt.requestFocus()
        }
        else if (password.isEmpty()){
            binding.passwordEt.error="Enter Password"
            binding.passwordEt.requestFocus()
        }
        else{
            loginUser()
        }

    }
    private fun loginUser(){
        Log.d(TAG,"loginUser")
        progressDialog.setTitle("Logging In...")
        progressDialog.setMessage("Loading...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                Log.e(TAG, "loginUser: Logged In...")
                progressDialog.dismiss()
                startActivity(Intent(this@LogInEmailActivity, MainHomeActivity::class.java))
                finishAffinity()
            }.addOnFailureListener { e ->
                Log.e(TAG, "logInUser", e)
                progressDialog.dismiss()

                Utils.toast(this, "Unable to Login due to ${e.message}")
            }
    }

}