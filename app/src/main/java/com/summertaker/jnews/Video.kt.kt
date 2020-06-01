package com.summertaker.jnews

import android.net.Uri

data class Video(
    var contentUri: Uri,
    //val contentUri: String,
    val filePath: String,
    val displayName: String
)