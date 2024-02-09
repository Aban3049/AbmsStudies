package com.pandaapps.abmsstudies.fees.activity



import android.os.Bundle
import android.os.Environment
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.pandaapps.abmsstudies.Communicator
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.databinding.ActivityFeesSlipBinding
import com.pandaapps.abmsstudies.fees.fragments.AdmissionNumberFragment
import com.pandaapps.abmsstudies.fees.fragments.FeesSlipFragment
import java.io.File
import java.io.FileOutputStream
import java.util.Date


class FeesSlipActivity : AppCompatActivity(), Communicator {
    private lateinit var binding: ActivityFeesSlipBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityFeesSlipBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)



        binding.toolbarBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.downloadBtn.setOnClickListener {
            Utils.toast(this@FeesSlipActivity, "Btn clicked :")



        }

        showAdmissionNumberFragment()

    }


    override fun passDataCom(editTextInput: String) {

        val bundle = Bundle()
        bundle.putString("admissionNumber", editTextInput)
        val transaction = this.supportFragmentManager.beginTransaction()
        val feeSlipFragment = FeesSlipFragment()
        feeSlipFragment.arguments = bundle
        transaction.replace(binding.fragmentFl.id, feeSlipFragment)
        transaction.commit()
    }

    private fun showAdmissionNumberFragment() {
        val fragment = AdmissionNumberFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding.fragmentFl.id, fragment, "AdmissionNumberFragment")
        fragmentTransaction.commit()

    }




}