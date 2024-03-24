package com.pandaapps.abmsstudies.sell.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.pandaapps.abmsstudies.sell.fragments.HomeFragment
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityMainBinding
import com.pandaapps.abmsstudies.sell.fragments.AccountFragment
import com.pandaapps.abmsstudies.sell.fragments.ChatsFragment
import com.pandaapps.abmsstudies.sell.fragments.MyAdsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null) {
            startLogInOption()
        }
        setContentView(binding.root)

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        showHomeFragment()
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    showHomeFragment()
                    true
                }

                R.id.menu_chats -> {
                    if (firebaseAuth.currentUser == null) {
                        Utils.toast(this@MainActivity, "LogIn Required")
                        startLogInOption()
                        false
                    } else {
                        showChatFragment()
                        true
                    }

                }

                R.id.menu_account -> {

                    if (firebaseAuth.currentUser == null) {
                        Utils.toast(this@MainActivity, "LogIn Required")
                        startLogInOption()
                        false
                    } else {
                        showAccountFragment()
                        true
                    }


                }

                R.id.menu_myAds -> {
                    if (firebaseAuth.currentUser == null) {
                        Utils.toast(this@MainActivity, "LogIn Required")
                        startLogInOption()
                        false
                    } else {
                        showMyAdsFragment()
                        true
                    }


                }

                R.id.menu_sell -> {
                    if (firebaseAuth.currentUser == null) {
                        Utils.toast(this@MainActivity, "LogIn Required")
                        startLogInOption()
                        false
                    } else {

                        true
                    }


                }

                else -> {
                    false
                }

            }
        }
        binding.sellFab.setOnClickListener {
            val intent = Intent(this, AdCreateActivity::class.java)
            intent.putExtra("isEditMode", false)
            startActivity(intent)
        }

    }


    private fun showHomeFragment() {
        binding.toolbarTitleTv.text = getString(R.string.home)
        val fragment = HomeFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "HomeFragment")
        fragmentTransaction.commit()
    }

    private fun showChatFragment() {
        binding.toolbarTitleTv.text = getString(R.string.chat)
        val fragment = ChatsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "ChatFragment")
        fragmentTransaction.commit()
    }

    private fun showMyAdsFragment() {
        binding.toolbarTitleTv.text = getString(R.string.my_ads)
        val fragment = MyAdsFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "MyAdsFragment")
        fragmentTransaction.commit()
    }

    private fun showAccountFragment() {
        binding.toolbarTitleTv.text = getString(R.string.account)
        val fragment = AccountFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment, "AccountFragment")
        fragmentTransaction.commit()
    }

    private fun startLogInOption() {
        val intent = Intent(this@MainActivity, LogIn::class.java)
        startActivity(intent)
    }

}




