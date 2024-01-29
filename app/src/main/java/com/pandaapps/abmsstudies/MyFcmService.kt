package com.pandaapps.abmsstudies


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pandaapps.abmsstudies.activities.ChatActivity
import java.util.Random

class MyFcmService:FirebaseMessagingService() {

    private companion object{
        private const val TAG ="MY_FCM_TAG"

        //Notification Channel ID

        private const val NOTIFICATION_CHANNEL_ID = "ABMS_CHANNEL_TO"

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = "${remoteMessage.notification?.title}"
        val body = "${remoteMessage.notification?.body}"

        val sendUid = "${remoteMessage.data["senderUid"]}"
        val notificationType = "${remoteMessage.data["notificationType"]}"

        Log.d(TAG, "onMessageReceived: title: $title")
        Log.d(TAG, "onMessageReceived: body: $body")
        Log.d(TAG, "onMessageReceived: senderUid: $sendUid")
        Log.d(TAG, "onMessageReceived: notificationType: $notificationType")

        //function cal to show notification
        showChatNotification(title,body,sendUid)

    }

    private fun showChatNotification(notificationTitle:String,notificationDescription:String,senderUid:String){
        // generate random number for notification id
        val notificationId = Random().nextInt(3000)


        //init notification Manger
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // function call to setup notification channel in android 0 and above

        setupNotificationChannel(notificationManager)

        val intent = Intent(this,ChatActivity::class.java)
        intent.putExtra("receiptUid",senderUid)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            // pending intent to add in Notification
        val pendingIntent =PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(notificationTitle)
            .setContentText(notificationDescription)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        notificationManager.notify(notificationId,notificationBuilder.build())
    }

    private fun setupNotificationChannel(notificationManager:NotificationManager){


        // Android 8.0 all notification Must be Assigned to a Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            val notificationChannel = NotificationChannel(

                NOTIFICATION_CHANNEL_ID,
                "Chat Chanel",
                NotificationManager.IMPORTANCE_HIGH
            )

            notificationChannel.description = "Show Chats Notifications"
            notificationChannel.enableVibration(true)

            notificationManager.createNotificationChannel(notificationChannel)

        }

    }

}