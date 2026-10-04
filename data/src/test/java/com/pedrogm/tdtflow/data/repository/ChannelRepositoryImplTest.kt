package com.pedrogm.tdtflow.data.repository

import com.pedrogm.tdtflow.data.local.ChannelDao
import com.pedrogm.tdtflow.data.local.ChannelEntity
import com.pedrogm.tdtflow.data.remote.TdtApi
import com.pedrogm.tdtflow.data.remote.TdtAmbit
import com.pedrogm.tdtflow.data.remote.TdtChannel
import com.pedrogm.tdtflow.data.remote.TdtChannelsResponse
import com.pedrogm.tdtflow.data.remote.TdtCountry
import com.pedrogm.tdtflow.data.remote.TdtOption
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeChannelDao : ChannelDao {
    override suspend fun getAllChannels(): List<ChannelEntity> = emptyList()
    override suspend fun insertChannels(channels: List<ChannelEntity>) = Unit
    override suspend fun deleteAll() = Unit
}

private class FakeTdtApi(
    private val tvResponse: TdtChannelsResponse = TdtChannelsResponse(),
    private val radioResponse: TdtChannelsResponse = TdtChannelsResponse()
) : TdtApi {
    override suspend fun getTvChannels(): TdtChannelsResponse = tvResponse
    override suspend fun getRadioChannels(): TdtChannelsResponse = radioResponse
    override suspend fun getTvEpg(): List<com.pedrogm.tdtflow.data.remote.TdtEpgChannelEntry> = emptyList()
    override suspend fun getRadioEpg(): List<com.pedrogm.tdtflow.data.remote.TdtEpgChannelEntry> = emptyList()
}

class ChannelRepositoryImplTest {

    private val fakeDao = FakeChannelDao()
    private val fakeApi = FakeTdtApi()
    private val testDispatcher = UnconfinedTestDispatcher()

    private fun channel(
        name: String,
        url: String,
        category: ChannelCategory = ChannelCategory.GENERAL,
        logo: String = ""
    ) = Channel(name = name, url = url, logo = logo, category = category)

    // ── cache hit ───────────────────────────────────────────────────────────

    @Test
    fun `getChannels emits cached channels without hitting network`() = runTest {
        val cachedChannels = listOf(
            channel("La 1", "rtve1.m3u8"),
            channel("La 2", "rtve2.m3u8")
        )
        val cache = ChannelCache(ttlMs = Long.MAX_VALUE).apply { put(cachedChannels) }
        val repository = ChannelRepositoryImpl(tdtApi = fakeApi, channelDao = fakeDao, ioDispatcher = testDispatcher, cache = cache)

        val result = repository.getChannels().first()

        assertEquals(cachedChannels, result)
    }

    @Test
    fun `getChannels returns single emission when cache is warm`() = runTest {
        val cache = ChannelCache(ttlMs = Long.MAX_VALUE).apply {
            put(listOf(channel("Canal Sur", "canalsur.m3u8", ChannelCategory.REGIONAL)))
        }
        val repository = ChannelRepositoryImpl(tdtApi = fakeApi, channelDao = fakeDao, ioDispatcher = testDispatcher, cache = cache)

        val emissions = mutableListOf<List<Channel>>()
        repository.getChannels().collect { emissions.add(it) }

        assertEquals(1, emissions.size)
    }

    // ── onError callback ────────────────────────────────────────────────────

    @Test
    fun `onError is not called when cache is warm`() = runTest {
        var errorCalled = false
        val cache = ChannelCache(ttlMs = Long.MAX_VALUE).apply {
            put(listOf(channel("La 1", "rtve1.m3u8")))
        }
        val repository = ChannelRepositoryImpl(
            tdtApi = fakeApi,
            channelDao = fakeDao,
            ioDispatcher = testDispatcher,
            cache = cache,
            onError = { errorCalled = true }
        )

        repository.getChannels().first()

        assertEquals(false, errorCalled)
    }

    // ── multi-country mapping ───────────────────────────────────────────────

    private fun tdtChannel(name: String, url: String) = TdtChannel(
        name = name,
        web = null,
        logo = "",
        epgId = "",
        options = listOf(TdtOption(format = "m3u8", url = url, geo = null, resolution = null, language = null)),
        extraInfo = null
    )

    @Test
    fun `channels from non-Spain countries are tagged INTERNATIONAL`() = runTest {
        val tvResponse = TdtChannelsResponse(
            countries = listOf(
                TdtCountry(
                    name = "Spain",
                    ambits = listOf(TdtAmbit(name = "Generalistas", channels = listOf(tdtChannel("La 1", "https://es-la1.m3u8"))))
                ),
                TdtCountry(
                    name = "International",
                    ambits = listOf(TdtAmbit(name = "Generalistas", channels = listOf(tdtChannel("BBC World", "https://bbc-world.m3u8"))))
                )
            )
        )
        val api = FakeTdtApi(tvResponse = tvResponse)
        val repository = ChannelRepositoryImpl(tdtApi = api, channelDao = fakeDao, ioDispatcher = testDispatcher)

        val result = repository.getChannels().first()

        val spainChannel = result.first { it.url == "https://es-la1.m3u8" }
        val internationalChannel = result.first { it.url == "https://bbc-world.m3u8" }
        assertEquals(ChannelCategory.GENERAL, spainChannel.category)
        assertEquals(ChannelCategory.INTERNATIONAL, internationalChannel.category)
        assertTrue(result.any { it.url == "https://bbc-world.m3u8" })
    }
}
