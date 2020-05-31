package com.summertaker.jnews

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {
    fun getService(): RetrofitInterface = retrofit.create(RetrofitInterface::class.java)

    private val retrofit =
        Retrofit.Builder()
            .baseUrl("http://summertaker.cafe24.com/reader/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}
