package com.pedrogm.tdtflow.data.remote

import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    private const val BASE_URL = "https://www.tdtchannels.com/"

    private val gson = Gson()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    val service: TdtChannelsService = retrofit.create(TdtChannelsService::class.java)
}
