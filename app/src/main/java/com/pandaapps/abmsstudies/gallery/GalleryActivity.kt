package com.pandaapps.abmsstudies.gallery

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.databinding.ActivityGalleryBinding

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityGalleryBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun  loadGallery(){

    }

}