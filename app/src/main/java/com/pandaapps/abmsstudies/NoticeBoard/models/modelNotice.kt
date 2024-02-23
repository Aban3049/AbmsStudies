package com.pandaapps.abmsstudies.NoticeBoard.models

class modelNotice {

    var uid:String = ""
    var id:String = ""
    var title:String = ""
    var description:String = ""
    var url:String = ""
    var imageUrl = ""
    var timestamp:Long = 0

    constructor()
    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        url: String,
        imageUrl: String,
        timestamp: Long
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.url = url
        this.imageUrl = imageUrl
        this.timestamp = timestamp
    }
}