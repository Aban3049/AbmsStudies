package com.pandaapps.abmsstudies.mathLecture

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
            "Qs 1st year",
            "Qs 2nd year",
            "Civil 1st year",
            "Civil 2nd year",
            "Mech 1st year",
            "Mech 2nd year",
            "A&D 1st year",
            "A&D 2nd year"


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
                        1 -> {
                            Utils.toast(this@MathLecturesActivity, "ICT 1st Year is Selected:PlayLists are Not Combined!")
                            val urlToLoad = "https://www.youtube.com/watch?v=vF1vjt5SPsA&list=PLeXzC6ljzQQ2m4wFkNhtzbGD5ExS1X2h3&pp=iAQB"
                            binding.webView.loadUrl(urlToLoad)
                            binding.webView.visibility = View.VISIBLE
                            binding.spinnerMain.visibility = View.GONE
                        }

                        2 -> {
                            Utils.toast(this@MathLecturesActivity, "ICT 2nd Year is Selected")
                            val urlToLoad = "https://www.youtube.com/watch?v=mu16aZtW7lc&list=PLeXzC6ljzQQ1Qeemp9vb66O2EElDj4B89&pp=iAQB"
                            binding.webView.loadUrl(urlToLoad)
                            binding.webView.visibility = View.VISIBLE
                            binding.spinnerMain.visibility = View.GONE
                        }

                        3->{
                            Utils.toast(this@MathLecturesActivity, "Qs 1st Year is Selected")
                            val urlToLoad =
                                "https://www.youtube.com/watch?v=B3hbStPx_Q4&list=PLeXzC6ljzQQ0i4QWYdvf0PIemi2j9dEIK"
                            binding.webView.loadUrl(urlToLoad)
                            binding.webView.visibility = View.VISIBLE
                            binding.spinnerMain.visibility = View.GONE
                        }

                        4->{
                            Utils.toast(this@MathLecturesActivity, "Qs 2nd Year is Selected")
                            val urlToLoad =
                                "https://www.youtube.com/watch?v=nQQ_1HUy0Ac&list=PLeXzC6ljzQQ1nI4H1oMlE162Fa4pwxKdR&pp=iAQB"
                            binding.webView.loadUrl(urlToLoad)
                            binding.webView.visibility = View.VISIBLE
                            binding.spinnerMain.visibility = View.GONE
                        }

                        5->{
                            Utils.toast(this@MathLecturesActivity, "Civil 1st Year is Selected")
                            val urlToLoad =
                                "https://www.youtube.com/watch?v=B3hbStPx_Q4&list=PLeXzC6ljzQQ0i4QWYdvf0PIemi2j9dEIK"
                            binding.webView.loadUrl(urlToLoad)
                            binding.webView.visibility = View.VISIBLE
                            binding.spinnerMain.visibility = View.GONE
                        }
                        6->{
                            Utils.toast(this@MathLecturesActivity, "Civil 2nd Year is Selected")
                            val urlToLoad =
                                "https://www.youtube.com/watch?v=nQQ_1HUy0Ac&list=PLeXzC6ljzQQ1nI4H1oMlE162Fa4pwxKdR&pp=iAQB"
                            binding.webView.loadUrl(urlToLoad)
                            binding.webView.visibility = View.VISIBLE
                            binding.spinnerMain.visibility = View.GONE
                        }

                        7 -> {
                            Utils.toast(this@MathLecturesActivity, "Mechanical 1st Year is Selected")
                            val urlToLoad =
                                "https://www.youtube.com/watch?v=B3hbStPx_Q4&list=PLeXzC6ljzQQ0i4QWYdvf0PIemi2j9dEIK"
                            binding.webView.loadUrl(urlToLoad)
                            binding.webView.visibility = View.VISIBLE
                            binding.spinnerMain.visibility = View.GONE
                        }

                        8->{
                            Utils.toast(this@MathLecturesActivity, "Mechanical 2nd Year is Selected")
                            val urlToLoad =
                                "https://www.youtube.com/watch?v=nQQ_1HUy0Ac&list=PLeXzC6ljzQQ1nI4H1oMlE162Fa4pwxKdR&pp=iAQB"
                            binding.webView.loadUrl(urlToLoad)
                            binding.webView.visibility = View.VISIBLE
                            binding.spinnerMain.visibility = View.GONE
                        }

                        9 -> {
                            Utils.toast(this@MathLecturesActivity, "A&D 1st Year is Selected")
                            val urlToLoad =
                                "https://www.youtube.com/watch?v=B3hbStPx_Q4&list=PLeXzC6ljzQQ0i4QWYdvf0PIemi2j9dEIK"
                            binding.webView.loadUrl(urlToLoad)
                            binding.webView.visibility = View.VISIBLE
                            binding.spinnerMain.visibility = View.GONE
                        }

                        10 ->{
                            Utils.toast(this@MathLecturesActivity, "A&D 2nd Year is Selected")
                            val urlToLoad =
                                "https://www.youtube.com/watch?v=nQQ_1HUy0Ac&list=PLeXzC6ljzQQ1nI4H1oMlE162Fa4pwxKdR&pp=iAQB"
                            binding.webView.loadUrl(urlToLoad)
                            binding.webView.visibility = View.VISIBLE
                            binding.spinnerMain.visibility = View.GONE
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