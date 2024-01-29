package com.pandaapps.abmsstudies.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.pandaapps.abmsstudies.fragments.HomeFragment
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityMainBinding
import com.pandaapps.abmsstudies.fragments.AccountFragment
import com.pandaapps.abmsstudies.fragments.ChatsFragment
import com.pandaapps.abmsstudies.fragments.MyAdsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        firebaseAuth=FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null){
            startLogInOption()  
        }
        setContentView(binding.root)

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

showHomeFragment()
       binding.bottomNav.setOnItemSelectedListener {
           item ->
           when(item.itemId)
           {
               R.id.menu_home ->{
                   showHomeFragment()
                   true
               }
               R.id.menu_chats ->{
                   if (firebaseAuth.currentUser == null){
                       Utils.toast(this@MainActivity, "LogIn Required")
                       startLogInOption()
                       false
                   }
                   else{
                       showChatFragment()
                       true
                   }

               }

               R.id.menu_account ->{

                   if (firebaseAuth.currentUser == null){
                       Utils.toast(this@MainActivity, "LogIn Required")
                       startLogInOption()
                       false
                   }
                   else{
                       showAccountFragment()
                       true
                   }



               }
               R.id.menu_myAds ->{
                   if (firebaseAuth.currentUser == null){
                       Utils.toast(this@MainActivity, "LogIn Required")
                       startLogInOption()
                       false
                   }
                   else{
                       showMyAdsFragment()
                       true
                   }


               }
               R.id.menu_sell ->{
                   if (firebaseAuth.currentUser == null){
                       Utils.toast(this@MainActivity, "LogIn Required")
                       startLogInOption()
                       false
                   }
                   else{

                       true
                   }


               }
               else ->{
                   false
               }

           }
       }
        binding.sellFab.setOnClickListener {
            val intent = Intent(this,AdCreateActivity::class.java)
            intent.putExtra("isEditMode",false)
            startActivity(intent)
        }

    }



    private fun showHomeFragment(){
binding.toolbarTitleTv.text="Home"
        val fragment = HomeFragment()
        val fragmentTransaction =supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id,fragment,"HomeFragment")
fragmentTransaction.commit()
    }
    private fun showChatFragment(){
binding.toolbarTitleTv.text="Chat"
        val fragment = ChatsFragment()
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id,fragment,"ChatFragment")
        fragmentTransaction.commit()
    }
    private fun showMyAdsFragment(){
binding.toolbarTitleTv.text="My Ads"
        val fragment = MyAdsFragment()
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id,fragment,"MyAdsFragment")
        fragmentTransaction.commit()
    }
    private fun showAccountFragment(){
binding.toolbarTitleTv.text="Account"
        val fragment= AccountFragment()
        val fragmentTransaction=supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id,fragment,"AccountFragment")
        fragmentTransaction.commit()
    }
    private fun startLogInOption(){
        val intent = Intent(this@MainActivity, LogIn::class.java)
        startActivity(intent)
    }

}



