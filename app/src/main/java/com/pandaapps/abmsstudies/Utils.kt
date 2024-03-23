package com.pandaapps.abmsstudies

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateFormat
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Arrays
import java.util.Calendar
import java.util.Locale


object Utils {


    const val MESSAGE_TYPE_TEXT = "TEXT"
    const val MESSAGE_TYPE_IMAGE = "IMAGE"
    const val USER_MODE = "USER"
    const val MAX_BYTES_PDF:Long = 100000000 // 50MB

    // constants to define Ads status, when ad is published the Ad status will be set Available in firebase db,so user can mark as SOLD later when it is sold
    const val AD_STATUS_AVAILABLE = "AVAILABLE"
    const val AD_STATUS_SOLD = "SOLD"

    const val NOTIFICATION_TYPE_NEW_MESSAGE = "NOTIFICATION_TYPE_NEW_MESSAGE"
    const val FCM_SERVER_KEY =
        "AAAAvdFKpX0:APA91bGKbZgEg8q_UZ3WqY345O_w1lCuasP4Vyh6jBOuDPbY-Sk2g-GBOMMAJcsbrb5jmo3IP45HCOhWfEXxzTL9V5Mx3jtBheNR2lUZFCQ3mLRzaKJjqcHGcmcJxj6dyqttd5uDd5s3"

    //Categories array of the Ads
    val categories = arrayOf(
        "Mobiles",
        "Computer/Laptop",
        "Books",
        "Electronic",
        "Vehicles",
        "Uniform",
        "All"
    )

    // category icons in Array
    val categoryIcon = arrayOf(
        R.drawable.ic_category_phone,
        R.drawable.ic_category_computer,
        R.drawable.ic_category_electronics,
        R.drawable.ic_category_vehicle,
        R.drawable.ic_category_books,
        R.drawable.ic_category_unifomr,
        R.drawable.ic_category_other2

    )


    //Ad product conditions e.g. New, Used, Refurbished
    val condition = arrayOf(
        "New",
        "Used",
        "Refurbished"
    )

    fun toast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun getTimestamp(): Long {
        return System.currentTimeMillis()
    }

    fun formatTimestampDate(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy", calendar).toString()

    }

    fun formatTimestampDateTime(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp

        return DateFormat.format("dd/MM/yyyy hh:mm:a", calendar).toString()

    }

    fun formatTimestampDateTimeChat(timestamp: Long): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp

        return DateFormat.format("hh:mm:a", calendar).toString()

    }




    fun chatPath(receiptUid: String, yourUid: String): String {
        val arrayUids = arrayOf(receiptUid, yourUid)

        Arrays.sort(arrayUids)

        return "${arrayUids[0]}_${arrayUids[1]}"
    }

    // Ad data to db. Users > Uid >Favourites >adId >favouriteDataObj
    fun addToFavourite(context: Context, adId: String) {
        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null) {
            Utils.toast(context, "Your are not logged in!")
        } else {

            val timestamp = Utils.getTimestamp()

            val hashMap = HashMap<String, Any>()
            hashMap["adId"] = adId
            hashMap["timestamp"] = timestamp

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favourites").child(adId)
                .setValue(hashMap)
                .addOnSuccessListener {
                    Utils.toast(context, "Added to favourite...")
                }.addOnFailureListener { e ->
                    Utils.toast(context, "Failed to add to favourite due to ${e.message}")
                }
        }
    }

    fun removeFromFavourite(context: Context, adId: String) {

        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null) {

            Utils.toast(context, "You are not logged-in!")
        } else {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favourites").child(adId)
                .removeValue()
                .addOnSuccessListener {

                    Utils.toast(context, "Removed from favourite!")
                }.addOnFailureListener { e ->
                    Utils.toast(context, "Failed to remove from Favourite due to ${e.message}")
                }
        }
    }

    fun callIntent(context: Context, phone: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tel:${Uri.encode(phone)}"))
        context.startActivity(intent)
    }

    fun smsIntent(context: Context, phone: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms: ${Uri.encode(phone)}"))
        context.startActivity(intent)
    }

    fun mapIntent(context: Context, latitude: Double, longitude: Double) {
        val gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=$latitude,$longitude")


        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(context.packageManager) != null) {

            context.startActivity(mapIntent)
        } else {
            Utils.toast(context, "Google Map not installed")
        }
    }
}