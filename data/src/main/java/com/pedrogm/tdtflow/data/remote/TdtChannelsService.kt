package com.pedrogm.tdtflow.data.remote

import retrofit2.http.GET

interface TdtChannelsService {

    @GET("lists/tv.json")
    suspend fun getChannels(): TdtChannelsResponse
}
