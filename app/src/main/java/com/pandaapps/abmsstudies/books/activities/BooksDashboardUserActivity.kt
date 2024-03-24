package com.pandaapps.abmsstudies.books.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pandaapps.abmsstudies.books.fragments.BookUserFragment
import com.pandaapps.abmsstudies.books.model.ModelBooksCategoryAdmin
import com.pandaapps.abmsstudies.databinding.ActivityBooksDashboardUserBinding

class BooksDashboardUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBooksDashboardUserBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList:ArrayList<ModelBooksCategoryAdmin>
    private lateinit var viewpagerAdapter:ViewPagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {

       binding= ActivityBooksDashboardUserBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth =FirebaseAuth.getInstance()

        setupWithViewPagerAdapter(binding.viewPager)

        binding.tabLayout.setupWithViewPager(binding.viewPager)

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun setupWithViewPagerAdapter(viewpager:ViewPager){
        viewpagerAdapter = ViewPagerAdapter(supportFragmentManager,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this)

        //init Array list
        categoryArrayList = ArrayList()

        //load category from db
        val ref = FirebaseDatabase.getInstance().getReference("CategoriesBooks")
        ref.addListenerForSingleValueEvent(object:ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                //clearList
                categoryArrayList.clear()

                // load static categories e.g All Most Viewed ,most Downloads
                //Add data to model
                val modelAll = ModelBooksCategoryAdmin("01","All",1,"")
                val modelMostViewed = ModelBooksCategoryAdmin("01","Most Viewed",1,"")
                val modelMostDownloaded = ModelBooksCategoryAdmin("01","Most Download",1,"")

                //add to load list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelMostDownloaded)

                //add to viewPage Adapter
                viewpagerAdapter.addFragment(BookUserFragment.newInstance(
                    modelAll.id,
                    modelAll.category,
                    modelAll.uid
                ),modelAll.category
                )

                viewpagerAdapter.addFragment(BookUserFragment.newInstance(
                    modelMostViewed.id,
                    modelMostViewed.category,
                    modelMostViewed.uid
                ),modelMostViewed.category
                )

                viewpagerAdapter.addFragment(BookUserFragment.newInstance(
                    modelMostDownloaded.id,
                    modelMostDownloaded.category,
                    modelMostDownloaded.uid
                ),modelMostDownloaded.category
                )

                //refresh list

                viewpagerAdapter.notifyDataSetChanged()

                //Now load from firebase db

                for (ds in snapshot.children){
                    //get data in model
                    val model = ds.getValue(ModelBooksCategoryAdmin::class.java)
                    //add to list
                    categoryArrayList.add(model!!)
                    //add to viewpager adapter
                    viewpagerAdapter.addFragment(BookUserFragment.newInstance(
                        model.id,
                        model.category,
                        model.uid
                    ),model.category
                    )

                    //refresh list
                    viewpagerAdapter.notifyDataSetChanged()
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        //setup adapter
        viewpager.adapter = viewpagerAdapter
    }

    class ViewPagerAdapter(fm:FragmentManager,behavior: Int, context:Context):FragmentPagerAdapter(fm,behavior){

        //holds list of fragments i.e new instance for each category
        private val fragmentList:ArrayList<BookUserFragment> = ArrayList()

        //list of titles of categories for tabs
        private val fragmentTitleList:ArrayList<String> = ArrayList()

        private val context:Context

        init {
            this.context = context
        }
        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
           return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

         fun addFragment(fragment:BookUserFragment, title:String){
            //add fragment
            fragmentList.add(fragment)

            //title that will passed as parameter

            fragmentTitleList.add(title)

        }


    }

}