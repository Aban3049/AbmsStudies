package com.pandaapps.abmsstudies.papers.model

class ModelPaperPdf {

    var uid: String = ""
    var id: String = ""
    var title: String = ""
    var categoryId: String = ""
    var url: String = ""
    var imageUrl = ""
    var timestamp: Long = 0


    constructor()
    constructor(
        uid: String,
        id: String,
        title: String,
        categoryId: String,
        url: String,
        imageUrl: String,
        timestamp: Long,
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.categoryId = categoryId
        this.url = url
        this.imageUrl = imageUrl
        this.timestamp = timestamp

    }

}