package com.pandaapps.abmsstudies

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pandaapps.abmsstudies.databinding.ActivityAboutUsBinding

class AboutUsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAboutUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding=ActivityAboutUsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



    }
}