package com.pandaapps.abmsstudies.ChatRoom.model

import java.util.Date

class ChatRoomModel {
    var message:String = ""
    var userName:String = ""
    var timestamp: Date ?=null
    var messageId:String = ""
    var profileImageUrl:String = ""
    var chat_image:String = ""
    var uid:String = ""
    var chatTime:Long = 0

    constructor()
    constructor(
        message: String,
        userName: String,
        timestamp: Date,
        messageId: String,
        profileImageUrl: String,
        chat_image:String,
        uid:String,
        chatTime:Long
    ) {
        this.message = message
        this.userName = userName
        this.timestamp = timestamp
        this.messageId = messageId
        this.profileImageUrl = profileImageUrl
        this.chat_image = chat_image
        this.chatTime=chatTime
        this.uid = uid
    }


}