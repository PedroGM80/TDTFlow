package com.pedrogm.tdtflow.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.tdtchannels.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: TdtChannelsService =
        retrofit.create(TdtChannelsService::class.java)
}
