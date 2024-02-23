package com.pandaapps.abmsstudies.ChatRoom.FirebaseCords

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseCords {

    private val firebaseStore:FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth:FirebaseAuth = FirebaseAuth.getInstance()

    val  MAIN_CHAT_DATABASE = firebaseStore.collection("CHAT")

}