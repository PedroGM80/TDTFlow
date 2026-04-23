package com.pedrogm.tdtflow.data.repository

import com.pedrogm.tdtflow.data.local.ChannelDao
import com.pedrogm.tdtflow.data.local.ChannelEntity
import com.pedrogm.tdtflow.data.remote.TdtApi
import com.pedrogm.tdtflow.data.remote.TdtChannelsResponse
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeChannelDao : ChannelDao {
    override suspend fun getAllChannels(): List<ChannelEntity> = emptyList()
    override suspend fun insertChannels(channels: List<ChannelEntity>) = Unit
    override suspend fun deleteAll() = Unit
}

private class FakeTdtApi : TdtApi {
    override suspend fun getTvChannels(): TdtChannelsResponse = TdtChannelsResponse()
    override suspend fun getRadioChannels(): TdtChannelsResponse = TdtChannelsResponse()
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
}
