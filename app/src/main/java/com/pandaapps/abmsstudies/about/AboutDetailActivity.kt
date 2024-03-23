package com.pandaapps.abmsstudies.about

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.books.activities.PdfViewActivity
import com.pandaapps.abmsstudies.databinding.ActivityAboutDetailBinding

class AboutDetailActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "ABOUT_DETAIL"
    }

    private lateinit var binding: ActivityAboutDetailBinding

    private var aboutType = ""
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityAboutDetailBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        aboutType = intent.getStringExtra("Detail")!!

        @JavascriptInterface
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = WebViewClient()
        binding.webView.visibility = View.GONE

        if (aboutType == "resume") {
            loadResume()
        }else if (aboutType == "github"){
            loadGitHub()
        }else if (aboutType == "linkedin"){
            loadLinkedin()
        }

    }

    private fun loadResume() {

        binding.progressBar.visibility = View.VISIBLE
        binding.pdfView.visibility = View.VISIBLE

        val pdfUrl =
            "https://firebasestorage.googleapis.com/v0/b/abmsstudies.appspot.com/o/Developers%2Fmyresume.pdf?alt=media&token=b83bd8da-20fd-4464-83eb-c6729258e277"

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Utils.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "loadBookFromUrl: pdf got from url $pdfUrl")


                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false)
                    .onPageChange { page, pageCount ->
                        //set current and total pages toolbar subtitle
                        val currentPage = page + 1 // page starts from 0 so we add 1

                        Log.d(TAG, "loadBookFromUrl: $currentPage/$pageCount")
                    }
                    .onError { t ->
                        Log.e(TAG, "loadBookFromUrl: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Utils.toast(this, "Error at page: $page")
                        Log.d(TAG, "loadBookFromUrl: ${t.message}")
                    }

                    .onError { error ->
                        error.message?.let {
                            Utils.toast(this, "$it")
                        }

                    }
                    .load()

                binding.progressBar.visibility = View.GONE


                //load pdf


            }
            .addOnFailureListener { e ->
                Log.e(TAG, "loadBookFromUrl: Failed to get url due to ${e.message}")
                binding.progressBar.visibility = View.GONE
            }

    }

    private fun loadGitHub(){
        val urlToLoad="https://github.com/Aban3049/AbmsStudies"
        binding.webView.loadUrl(urlToLoad)
        binding.webView.visibility=View.VISIBLE
    }

    private fun loadLinkedin(){
        val urlToLoad="https://www.linkedin.com/in/muhammad-aban-b8b6272bb/?profileId=ACoAAEzND9sBidakR9P8Fd9hNfTuHhIiyWKvA_0"
        binding.webView.loadUrl(urlToLoad)
        binding.webView.visibility=View.VISIBLE
    }

}