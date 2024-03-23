package com.pandaapps.abmsstudies.gallery.model

class modelPictures {

    var id:String = ""
    var imageUrl:String = ""
    var timestamp:Long = 0
    var title:String = ""
    var uid:String = ""

    constructor()
    constructor(id: String, imageUrl: String, timestamp: Long, title: String, uid: String) {
        this.id = id
        this.imageUrl = imageUrl
        this.timestamp = timestamp
        this.title = title
        this.uid = uid
    }


}