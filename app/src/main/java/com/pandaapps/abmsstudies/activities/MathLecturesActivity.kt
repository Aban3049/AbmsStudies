package com.pandaapps.abmsstudies.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityMathLecturesctivityBinding

class MathLecturesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMathLecturesctivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMathLecturesctivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        @JavascriptInterface
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = WebViewClient()
        binding.webView.visibility = View.GONE

        selectCategory()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }


    }

    // Function to load Lectures according to year and department and setting Adapter on Spinner

    private fun selectCategory() {
        val spinner: Spinner = binding.spinnerMain
        val listItem = listOf<String>(
            "--- Select your Category ---",
            "ICT 1st year",
            "ICT 2nd year",
            "ICT 3rd year",
            "Qs 1st year",
            "Qs 2nd year",
            "Qs 3rd year",
            "civil 1st year",
            "civil 2nd year",
            "civil 3rd year"


        )

        val arrayAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
           // com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
            listItem
        )
//        arrayAdapter.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = arrayAdapter

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {


                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent.getItemAtPosition(position).toString()
                    when (parent.selectedItemPosition) {
                        0 -> {

                        }

                        1 -> {
                            Utils.toast(this@MathLecturesActivity, "ICT 1st Year is Selected")
                            val urlToLoad="https://youtu.be/B3hbStPx_Q4?si=eQKe5eTI7uQE5eOh"
                            binding.webView.loadUrl(urlToLoad)
                            binding.webView.visibility=View.VISIBLE
                            binding.spinnerMain.visibility=View.GONE
                        }

                        6 -> {
                            Utils.toast(this@MathLecturesActivity, "Position 6 is clicked")
                        }
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                }


            }

    }
}