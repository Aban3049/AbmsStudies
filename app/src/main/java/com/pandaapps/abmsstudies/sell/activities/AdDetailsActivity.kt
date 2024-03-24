package com.pandaapps.abmsstudies.sell.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.PopupMenu
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.pandaapps.abmsstudies.R
import com.pandaapps.abmsstudies.Utils
import com.pandaapps.abmsstudies.sell.adapter.AdapterImageSlider
import com.pandaapps.abmsstudies.databinding.ActivityAdDetailsBinding
import com.pandaapps.abmsstudies.sell.model.ModelAd
import com.pandaapps.abmsstudies.sell.model.ModelImageSlider



class AdDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdDetailsBinding

    private companion object {
        private const val TAG = "AD_DETAIL_TAG"
    }

    private lateinit var firebaseAuth: FirebaseAuth

    private var adId = ""

    private var adLatitude = 0.0
    private var adLongitude = 0.0

    private var sellerUid = ""
    private var sellerPhone = ""

    private var favourites = false

    private var userMode =""

    // list of Ad's images to show in slider
    private lateinit var imageSliderArrayList: ArrayList<ModelImageSlider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //hide view like Edit,delete if user is owner of the Ad view will be visible other options are visible like call sms chat

        binding.toolbarEditBtn.visibility = View.GONE
        binding.toolbarDeleteBtn.visibility = View.GONE
        binding.chatBtn.visibility = View.GONE
        binding.callBtn.visibility = View.GONE
        binding.smsBtn.visibility = View.GONE



        firebaseAuth = FirebaseAuth.getInstance()

        adId = intent.getStringExtra("adId").toString()
        Log.d(TAG, "onCreate: adId: $adId")

        if (firebaseAuth.currentUser != null) {
            checkIsFavorite()
        }

        loadAdDetails()
        loadAdImages()

        binding.toolbarBackBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbarDeleteBtn.setOnClickListener {
            val materialAlertDialogBuilder = MaterialAlertDialogBuilder(this@AdDetailsActivity)
            materialAlertDialogBuilder.setTitle("Delete Ad")
                .setMessage("Are you sure you want to delete this Ad")
                .setPositiveButton("Delete") { _, _ ->
                    Log.d(TAG, "onCreate: DELETE clicked...")
                    deleteAd()
                }
                .setNegativeButton("CANCEL") { dialog, _ ->
                    Log.d(TAG, "onCreate: CANCEL...")
                    dialog.dismiss()
                }
                .show()
        }

        binding.toolbarEditBtn.setOnClickListener {
            editOptionsDialog()
        }

        binding.toolbarFavBtn.setOnClickListener {
            if (favourites) {
                Utils.removeFromFavourite(this, adId)
            } else {
                Utils.addToFavourite(this, adId)
            }
        }

        binding.sellerProfileCv.setOnClickListener {
            val intent = Intent(this@AdDetailsActivity,AdSellerProfileActivity::class.java)
            intent.putExtra("sellerUid",sellerUid)
            startActivity(intent)
        }

        binding.chatBtn.setOnClickListener {
            val intent = Intent(this@AdDetailsActivity,ChatActivity::class.java)
            intent.putExtra("receiptUid",sellerUid)
            startActivity(intent)
        }

        binding.callBtn.setOnClickListener {
            Utils.callIntent(this@AdDetailsActivity, sellerPhone)
        }
        binding.smsBtn.setOnClickListener {
            Utils.smsIntent(this@AdDetailsActivity, sellerPhone)
        }
        binding.mapBtn.setOnClickListener {
            Utils.mapIntent(this@AdDetailsActivity, adLatitude, adLongitude)
        }


    }

    private fun editOptionsDialog() {
        Log.d(TAG, "editOptionsDialog: ")

        val popupMenu = PopupMenu(this@AdDetailsActivity, binding.toolbarEditBtn)


        popupMenu.menu.add(Menu.NONE, 0, 0, "Edit")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Mark As Sold")

        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->

            val itemId = menuItem.itemId

            if (itemId == 0) {
                val intent = Intent(this, AdCreateActivity::class.java)
                intent.putExtra("isEditMode", true)
                intent.putExtra("adId", adId)
                startActivity(intent)
            } else if (itemId == 1) {
                showMarkAsSoldDialog()
            }

            return@setOnMenuItemClickListener true
        }
    }

    private fun showMarkAsSoldDialog() {
        Log.d(TAG, "showMarkAsSoldDialog")
        val alertDialogBuilder = MaterialAlertDialogBuilder(this)
        alertDialogBuilder.setTitle("Mark as sold?")
            .setMessage("Are you sure you want to mark this Ad as sold?")
            .setPositiveButton("SOLD") { _, _ ->
                Log.d(TAG, "showMarkAsSoldDialog: SOLD clicked")

                val hashMap = HashMap<String, Any>()
                hashMap["status"] = "${Utils.AD_STATUS_SOLD}"

                val ref = FirebaseDatabase.getInstance().getReference("Ads")
                ref.child(adId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        Log.d(TAG, "showMarkAsSoldDialog: Marked as sold")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "showMarkAsSoldDialog", e)
                        Utils.toast(
                            this@AdDetailsActivity,
                            "Failed to mark as Sold due to ${e.message}"
                        )
                    }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                Log.d(TAG, "showMarkAsSoldDialog: CANCEL clicked")
                dialog.dismiss()
            }
            .show()
    }

    private fun loadAdDetails() {
        Log.d(TAG, "loadAdsDetails:")

        val userId= firebaseAuth.uid
        val refForUserMode = FirebaseDatabase.getInstance().getReference("Users")
        refForUserMode.child(userId!!)
            .addValueEventListener(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    userMode = "${snapshot.child("userMode").value}"
                    Log.d(TAG, "onDataChange: userMode: $userMode ")
                    Log.d(TAG, "onDataChange: UserUid: $userId"  )
                }

                override fun onCancelled(error: DatabaseError) {

                    Log.e(TAG, "onCancelled: UserMode: $error" )
                }

            })




        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val modelAd = snapshot.getValue(ModelAd::class.java)



                        sellerUid = "${modelAd!!.uid}"
                        val title = modelAd.title
                        val description = modelAd.description
                        val address = modelAd.address
                        val condition = modelAd.condition
                        val category = modelAd.category
                        val price = modelAd.price
                        adLatitude = modelAd.latitude
                        adLongitude = modelAd.longitude
                        val timestamp = modelAd.timestamp

                        val formattedDate = Utils.formatTimestampDate(timestamp)


                        if (sellerUid == firebaseAuth.uid) {


                            // if Ad is created by current user should be able to edit delete the Ad
                            binding.toolbarEditBtn.visibility = View.VISIBLE
                            binding.toolbarDeleteBtn.visibility = View.VISIBLE

                            //Cannot message or chat to himself
                            binding.chatBtn.visibility = View.GONE
                            binding.callBtn.visibility = View.GONE
                            binding.smsBtn.visibility = View.GONE
                            binding.receiptProfileTv.visibility=View.GONE
                            binding.sellerProfileCv.visibility=View.GONE


                        }

                        else if (userMode =="Admin"){
                            binding.toolbarEditBtn.visibility = View.VISIBLE
                            binding.toolbarDeleteBtn.visibility = View.VISIBLE
                            binding.chatBtn.visibility = View.VISIBLE
                            binding.callBtn.visibility = View.VISIBLE
                            binding.smsBtn.visibility = View.VISIBLE
                            binding.sellerProfileCv.visibility=View.VISIBLE
                            binding.receiptProfileTv.visibility=View.VISIBLE
                        }

                        else {

                            binding.toolbarEditBtn.visibility = View.GONE
                            binding.toolbarDeleteBtn.visibility = View.GONE
                            binding.chatBtn.visibility = View.VISIBLE
                            binding.callBtn.visibility = View.VISIBLE
                            binding.smsBtn.visibility = View.VISIBLE
                            binding.sellerProfileCv.visibility=View.VISIBLE
                            binding.receiptProfileTv.visibility=View.VISIBLE
                        }

                        binding.titleTv.text = title
                        binding.descriptionTv.text = description
                        binding.addressTv.text = address
                        binding.conditionTv.text = condition
                        binding.categoryTv.text = category
                        binding.priceTv.text = price
                        binding.dateTv.text = formattedDate

                        //function call,load seller info e.g. profile image,name, member since
                        loadSellerDetails()

                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChanged: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun loadSellerDetails() {
        Log.d(TAG, "loadSellerDetails:")
        //Db path to load seller info users -> seller uId
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(sellerUid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    val phoneCode = "${snapshot.child("phoneCode").value}"
                    val phoneNumber = "${snapshot.child("phoneNumber").value}"
                    val profileImageUrl = "${snapshot.child("profileImageURl").value}"
                    val name = "${snapshot.child("name").value}"
                    val timestamp = snapshot.child("timestamp").value as Long ?:0
                    val formattedDate = Utils.formatTimestampDate(timestamp)

                    binding.memberSinceTv.text = formattedDate
                    binding.sellerNameTv.text = name


                    sellerPhone = "$phoneCode$phoneNumber"

                    try {
                        Glide.with(this@AdDetailsActivity)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.ic_person_white)
                            .into(binding.sellerProfileIv)
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChange: ${e.message}")
                        Utils.toast(this@AdDetailsActivity, "${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, error.message)
                }
            })
    }

    private fun checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite")
        //Db path to check if Ad is in Favourite of Current user
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}").child("Favourites").child(adId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    favourites = snapshot.exists()
                    Log.d(TAG, "onDataChange: favorite: $favourites")

                    if (favourites) {
                        binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_yes)
                    } else {
                        binding.toolbarFavBtn.setImageResource(R.drawable.ic_fav_no)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun loadAdImages() {
        Log.d(TAG, "loadAdImages:")

        //init list before starting to put data into It
        imageSliderArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Ads")
        ref.child(adId).child("Images")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    imageSliderArrayList.clear()

                    for (ds in snapshot.children) {

                        try {
                            val modelImageSlider = ds.getValue(ModelImageSlider::class.java)

                            imageSliderArrayList.add(modelImageSlider!!)
                        } catch (e: Exception) {
                            Log.e(TAG, "onDataChanged: ", e)
                        }
                    }
                    val adapterImageSlider =
                        AdapterImageSlider(this@AdDetailsActivity, imageSliderArrayList)
                    binding.imageSliderVp.adapter = adapterImageSlider
                }

                override fun onCancelled(error: DatabaseError) {

                }


            })
    }


    private fun deleteAd() {
        Log.d(TAG, "deleteAd: ")

        val refForImages = FirebaseDatabase.getInstance().getReference("Ads")
        refForImages.child(adId).child("Images")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    for (ds in snapshot.children) {

                        try {

                            val imageUrl = "${ds.child("imageUrl").value}"

                            val storageRef =
                                FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                            storageRef.delete()
                        } catch (e: Exception) {
                            Log.e(TAG, "onDataChanged: ", e)
                        }
                    }

                    val ref = FirebaseDatabase.getInstance().getReference("Ads")
                    ref.child(adId)
                        .removeValue()
                        .addOnSuccessListener {
                            Log.d(TAG, "deleteAd: Deleted")
                            Utils.toast(this@AdDetailsActivity, "Deleted...!")

                            finish()
                        }

                        .addOnFailureListener { e ->
                            Log.e(TAG, "deleteAd: ", e)
                            Utils.toast(
                                this@AdDetailsActivity,
                                "Failed to delete due to ${e.message}"
                            )
                        }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }




    }
