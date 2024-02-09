package com.pandaapps.abmsstudies.sell.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.pandaapps.abmsstudies.databinding.FragmentMyAdsBinding

class MyAdsFragment : Fragment() {
    private lateinit var binding: FragmentMyAdsBinding

   private companion object {
    private const val TAG ="MY_ADS_TAG"
    }

    private lateinit var mContext:Context

    private lateinit var myTabViewPagerAdapter: MyTabsViewPagerAdapter

    override fun onAttach(context: Context) {
        this.mContext=context
        super.onAttach(context)
    }


    override fun onCreateView(
 inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
       binding= FragmentMyAdsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Ads"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Favourites"))

        val fragmentManager =childFragmentManager
        myTabViewPagerAdapter = MyTabsViewPagerAdapter(fragmentManager,lifecycle)
        binding.viewPager.adapter = myTabViewPagerAdapter

        binding.tabLayout.addOnTabSelectedListener(object: OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                Log.d(TAG,"onTabSelected: tab: $${tab.position}")
                binding.viewPager.currentItem=tab.position
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }
        })

        // change Tab when swiping

        binding.viewPager.registerOnPageChangeCallback(object :OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })
    }

    class MyTabsViewPagerAdapter(fragmentManager: FragmentManager,lifecycle: Lifecycle) :FragmentStateAdapter(fragmentManager,lifecycle){
        override fun createFragment(position: Int): Fragment {
            // tab position starts form 0. If 0 set/show MyAdsFragment otherwise 1 to show MyAdsFav Fragment
            if (position == 0){
                return MyAdsAdsFragment()
            }
            else{
                return MyAdsFavFragment()
            }
        }

        override fun getItemCount(): Int {
            return 2 // setting static sie 2 because we have 2 tabs
        }
    }

}