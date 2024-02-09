package com.pandaapps.abmsstudies.books.model

class ModelBookPdf {

    var uid:String = ""
    var id:String = ""
    var title:String = ""
    var description:String = ""
    var categoryId:String = ""
    var url:String = ""
    var imageUrl = ""
    var timestamp:Long = 0
    var viewCount:Long = 0
    var downloadsCount:Long = 0



    constructor()
    constructor(
        uid: String,
        id: String,
        title: String,
        description: String,
        categoryId: String,
        url: String,
        imageUrl:String,
        timestamp:Long,
        viewCount:Long,
        downloadsCount:Long
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.description = description
        this.categoryId = categoryId
        this.url = url
        this.imageUrl = imageUrl
        this.timestamp = timestamp
        this.viewCount = viewCount
        this.downloadsCount = downloadsCount
    }


}