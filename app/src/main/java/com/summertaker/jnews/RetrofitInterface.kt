package com.summertaker.jnews

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitInterface {

    @GET("youtube_json.php")
    fun requestArticles(
        @Query("id") id: Int,
        @Query("title") title: String
    ): Call<ArticleModel>
}