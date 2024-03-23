package com.pandaapps.abmsstudies.gallery.model

class modelVideo {

    var id: String =""
    var timestamp: Long = 0
    var title: String = ""
    var uid: String = ""
    var videoUrl: String = ""

    constructor()
    constructor(id: String, timestamp: Long, title: String, uid: String, videoUrl: String) {
        this.id = id
        this.timestamp = timestamp
        this.title = title
        this.uid = uid
        this.videoUrl = videoUrl
    }


}