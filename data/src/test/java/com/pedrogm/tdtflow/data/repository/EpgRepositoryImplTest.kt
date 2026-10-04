package com.pedrogm.tdtflow.data.repository

import com.pedrogm.tdtflow.data.remote.TdtApi
import com.pedrogm.tdtflow.data.remote.TdtChannelsResponse
import com.pedrogm.tdtflow.data.remote.TdtEpgChannelEntry
import com.pedrogm.tdtflow.data.remote.TdtEpgEvent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeEpgApi(
    private val tvEpg: List<TdtEpgChannelEntry> = emptyList(),
    private val radioEpg: List<TdtEpgChannelEntry> = emptyList()
) : TdtApi {
    override suspend fun getTvChannels(): TdtChannelsResponse = TdtChannelsResponse()
    override suspend fun getRadioChannels(): TdtChannelsResponse = TdtChannelsResponse()
    override suspend fun getTvEpg(): List<TdtEpgChannelEntry> = tvEpg
    override suspend fun getRadioEpg(): List<TdtEpgChannelEntry> = radioEpg
}

class EpgRepositoryImplTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private fun event(hi: Long, hf: Long, t: String) = TdtEpgEvent(hi = hi, hf = hf, t = t)

    @Test
    fun `getSchedule converts epoch seconds to millis`() = runTest {
        val api = FakeEpgApi(
            tvEpg = listOf(TdtEpgChannelEntry(name = "La1.TV", events = listOf(event(1_000, 2_000, "Noticias"))))
        )
        val repository = EpgRepositoryImpl(tdtApi = api, ioDispatcher = testDispatcher)

        val program = repository.getSchedule("La1.TV").first().first()

        assertEquals(1_000_000L, program.startTime)
        assertEquals(2_000_000L, program.endTime)
        assertEquals("Noticias", program.title)
    }

    @Test
    fun `getSchedule returns empty list for unknown epgId`() = runTest {
        val api = FakeEpgApi(
            tvEpg = listOf(TdtEpgChannelEntry(name = "La1.TV", events = listOf(event(1_000, 2_000, "Noticias"))))
        )
        val repository = EpgRepositoryImpl(tdtApi = api, ioDispatcher = testDispatcher)

        val schedule = repository.getSchedule("Unknown.TV").first()

        assertTrue(schedule.isEmpty())
    }

    @Test
    fun `getSchedule returns empty list for blank epgId without hitting the network`() = runTest {
        val repository = EpgRepositoryImpl(tdtApi = FakeEpgApi(), ioDispatcher = testDispatcher)

        val schedule = repository.getSchedule("").first()

        assertTrue(schedule.isEmpty())
    }

    @Test
    fun `getNowPlaying returns null for blank epgId`() = runTest {
        val repository = EpgRepositoryImpl(tdtApi = FakeEpgApi(), ioDispatcher = testDispatcher)

        val now = repository.getNowPlaying("").first()

        assertNull(now)
    }

    @Test
    fun `getNowPlaying finds the event covering the current time`() = runTest {
        val nowSeconds = System.currentTimeMillis() / 1000
        val api = FakeEpgApi(
            tvEpg = listOf(
                TdtEpgChannelEntry(
                    name = "La1.TV",
                    events = listOf(
                        event(nowSeconds - 3600, nowSeconds - 1, "Programa anterior"),
                        event(nowSeconds - 1, nowSeconds + 3600, "Programa actual")
                    )
                )
            )
        )
        val repository = EpgRepositoryImpl(tdtApi = api, ioDispatcher = testDispatcher)

        val now = repository.getNowPlaying("La1.TV").first()

        assertEquals("Programa actual", now?.title)
    }

    @Test
    fun `getSchedule merges TV and radio EPG by epgId`() = runTest {
        val api = FakeEpgApi(
            tvEpg = listOf(TdtEpgChannelEntry(name = "La1.TV", events = listOf(event(1_000, 2_000, "TV show")))),
            radioEpg = listOf(TdtEpgChannelEntry(name = "Cadena.FM", events = listOf(event(3_000, 4_000, "Radio show"))))
        )
        val repository = EpgRepositoryImpl(tdtApi = api, ioDispatcher = testDispatcher)

        val radioSchedule = repository.getSchedule("Cadena.FM").first()

        assertEquals(1, radioSchedule.size)
        assertEquals("Radio show", radioSchedule.first().title)
    }
}
