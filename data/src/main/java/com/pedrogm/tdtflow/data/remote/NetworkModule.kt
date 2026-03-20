package com.pedrogm.tdtflow.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object NetworkModule {

    private const val BASE_URL = "https://www.tdtchannels.com"

    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun getTvChannels(): TdtChannelsResponse =
        client.get("$BASE_URL/lists/tv.json").body()

    suspend fun getRadioChannels(): TdtChannelsResponse =
        client.get("$BASE_URL/lists/radio.json").body()
}
