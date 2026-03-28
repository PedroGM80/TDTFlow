package com.pedrogm.tdtflow.domain.usecase

import app.cash.turbine.test
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.fakes.FakeChannelsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetChannelsUseCaseTest {

    private lateinit var repository: FakeChannelsRepository
    private lateinit var getChannels: GetChannelsUseCase

    private val channel1 = Channel("La 1", "rtve1.m3u8", category = ChannelCategory.GENERAL)
    private val channel2 = Channel("La 2", "rtve2.m3u8", category = ChannelCategory.GENERAL)

    @Before
    fun setUp() {
        repository = FakeChannelsRepository()
        getChannels = GetChannelsUseCase(repository)
    }

    @Test
    fun `invoke returns channels from repository`() = runTest {
        repository.channels = listOf(channel1, channel2)

        getChannels().test {
            val result = awaitItem()
            assertEquals(listOf(channel1, channel2), result)
            awaitComplete()
        }
    }

    @Test
    fun `invoke returns empty list when repository is empty`() = runTest {
        repository.channels = emptyList()

        getChannels().test {
            assertTrue(awaitItem().isEmpty())
            awaitComplete()
        }
    }

    @Test
    fun `invoke propagates repository exception`() = runTest {
        repository.error = RuntimeException("Network error")

        getChannels().test {
            val error = awaitError()
            assertEquals("Network error", error.message)
        }
    }
}
