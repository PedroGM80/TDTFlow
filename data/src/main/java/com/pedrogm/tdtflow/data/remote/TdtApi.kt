package com.pedrogm.tdtflow.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface TdtApi {
    suspend fun getTvChannels(): TdtChannelsResponse
    suspend fun getRadioChannels(): TdtChannelsResponse
    suspend fun getTvEpg(): List<TdtEpgChannelEntry>
    suspend fun getRadioEpg(): List<TdtEpgChannelEntry>
}

class KtorTdtApi(private val client: HttpClient) : TdtApi {
    private companion object {
        const val BASE_URL = "https://www.tdtchannels.com"
    }

    override suspend fun getTvChannels(): TdtChannelsResponse =
        client.get("$BASE_URL/lists/tv.json").body()

    override suspend fun getRadioChannels(): TdtChannelsResponse =
        client.get("$BASE_URL/lists/radio.json").body()

    override suspend fun getTvEpg(): List<TdtEpgChannelEntry> =
        client.get("$BASE_URL/epg/TV.json").body()

    override suspend fun getRadioEpg(): List<TdtEpgChannelEntry> =
        client.get("$BASE_URL/epg/RADIO.json").body()
}
