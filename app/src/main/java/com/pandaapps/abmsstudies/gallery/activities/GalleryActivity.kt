package com.pandaapps.abmsstudies.gallery.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pandaapps.abmsstudies.databinding.ActivityGalleryBinding
import com.pandaapps.abmsstudies.gallery.fragments.PictureFragment
import com.pandaapps.abmsstudies.gallery.fragments.VideoFragment

class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityGalleryBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        showPicturesFragment()

        binding.pictureBtn.setOnClickListener{
            showPicturesFragment()
        }
        binding.videoBtn.setOnClickListener {
            showVideoFragment()
        }

    }

    private fun showPicturesFragment() {
        val fragment = PictureFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment)
        fragmentTransaction.commit()
    }

    private fun showVideoFragment() {
        val fragment = VideoFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentsFl.id, fragment)
        fragmentTransaction.commit()
    }

}