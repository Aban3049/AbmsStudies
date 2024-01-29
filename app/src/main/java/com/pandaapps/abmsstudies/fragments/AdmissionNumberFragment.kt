package com.pandaapps.abmsstudies.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pandaapps.abmsstudies.Communicator
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.databinding.FragmentAdmissionNumberBinding


class AdmissionNumberFragment : Fragment() {

    private lateinit var binding:FragmentAdmissionNumberBinding

    private lateinit var mContext:Context

    private lateinit var communicator: Communicator

    override fun onAttach(context: Context) {
        mContext=context
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding= FragmentAdmissionNumberBinding.inflate(LayoutInflater.from(mContext),container,false)

        communicator = activity as Communicator

        binding.feesBtn.setOnClickListener {
            communicator.passDataCom(binding.admissionNumberEt.text.toString())

        }

        return binding.root
    }



}