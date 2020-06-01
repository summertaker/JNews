package com.summertaker.jnews

import kotlin.collections.ArrayList

interface DataInterface {
    fun getLocalDataCallback(videos: ArrayList<Video>)
    //fun getRemoteDataCallback(articles: ArrayList<Article>)
}