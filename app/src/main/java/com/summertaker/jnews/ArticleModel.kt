package com.summertaker.jnews

data class ArticleModel (
    val error: String,
    var articles : ArrayList<Article>
)

data class Article (
    val id: Int,
    val yid: String,
    val title: String,
    val file: String,
    var contentUri: String
)