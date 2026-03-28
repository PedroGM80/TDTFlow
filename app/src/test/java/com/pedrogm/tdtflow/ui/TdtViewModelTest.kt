package com.pedrogm.tdtflow.ui

import app.cash.turbine.test
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.domain.usecase.GetChannelsUseCase
import com.pedrogm.tdtflow.fakes.FakeBrokenChannelTracker
import com.pedrogm.tdtflow.fakes.FakeChannelsRepository
import com.pedrogm.tdtflow.player.PlayerState
import com.pedrogm.tdtflow.util.TimeConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TdtViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeChannels: FakeChannelsRepository
    private lateinit var fakeTracker: FakeBrokenChannelTracker
    private lateinit var viewModel: TdtViewModel

    private val channel1 = Channel("La 1", "rtve1.m3u8", category = ChannelCategory.GENERAL)
    private val channel2 = Channel("La 2", "rtve2.m3u8", category = ChannelCategory.GENERAL)
    private val newsChannel = Channel("24h", "rtve24h.m3u8", category = ChannelCategory.NEWS)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeChannels = FakeChannelsRepository()
        fakeTracker = FakeBrokenChannelTracker()
        viewModel = TdtViewModel(
            getChannelsUseCase = GetChannelsUseCase(fakeChannels),
            brokenChannelTracker = fakeTracker,
            loadError = { e -> "Error: ${e.message}" },
            playerFactory = { error("Player not expected in unit tests") }
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading with no channels`() = runTest {
        fakeChannels.channels = emptyList()
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.channels.isEmpty())
            assertNull(state.error)
        }
    }

    @Test
    fun `channels load successfully updates state`() = runTest {
        fakeChannels.channels = listOf(channel1, channel2)
        val vm = buildViewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.channels.size)
            assertNull(state.error)
        }
    }

    @Test
    fun `channel load error sets error in state`() = runTest {
        fakeChannels.error = RuntimeException("Network unavailable")
        val vm = buildViewModel()

        vm.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertTrue(state.error?.contains("Network unavailable") == true)
            assertTrue(state.channels.isEmpty())
        }
    }

    @Test
    fun `Retry clears error and reloads channels`() = runTest {
        fakeChannels.error = RuntimeException("fail")
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem() // error state

            fakeChannels.error = null
            fakeChannels.channels = listOf(channel1)
            vm.onIntent(TdtIntent.Retry)

            val state = awaitItem()
            assertNull(state.error)
            assertEquals(1, state.channels.size)
        }
    }

    @Test
    fun `FilterByCategory filters the channel list`() = runTest {
        fakeChannels.channels = listOf(channel1, channel2, newsChannel)
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem() // loaded

            vm.onIntent(TdtIntent.FilterByCategory(ChannelCategory.NEWS))

            val state = awaitItem()
            assertEquals(1, state.filteredChannels.size)
            assertEquals("24h", state.filteredChannels.first().name)
        }
    }

    @Test
    fun `FilterByCategory null shows all channels`() = runTest {
        fakeChannels.channels = listOf(channel1, channel2, newsChannel)
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem()

            vm.onIntent(TdtIntent.FilterByCategory(ChannelCategory.NEWS))
            awaitItem()

            vm.onIntent(TdtIntent.FilterByCategory(null))
            val state = awaitItem()
            assertEquals(3, state.filteredChannels.size)
        }
    }

    @Test
    fun `DismissError clears error from state`() = runTest {
        fakeChannels.error = RuntimeException("oops")
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem() // error state

            vm.onIntent(TdtIntent.DismissError)

            val state = awaitItem()
            assertNull(state.error)
        }
    }

    @Test
    fun `ToggleShowBrokenChannels flips the flag`() = runTest {
        fakeChannels.channels = listOf(channel1)
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem()

            vm.onIntent(TdtIntent.ToggleShowBrokenChannels)
            assertTrue(awaitItem().showBrokenChannels)

            vm.onIntent(TdtIntent.ToggleShowBrokenChannels)
            assertFalse(awaitItem().showBrokenChannels)
        }
    }

    @Test
    fun `RevalidateChannels clears broken tracker and hides broken channels`() = runTest {
        fakeTracker.markAsBroken("rtve1.m3u8")
        fakeChannels.channels = listOf(channel1)
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem()

            vm.onIntent(TdtIntent.ToggleShowBrokenChannels)
            awaitItem()

            vm.onIntent(TdtIntent.RevalidateChannels)
            val state = awaitItem()
            assertFalse(state.showBrokenChannels)
            assertEquals(0, state.brokenChannelsCount)
        }
    }

    @Test
    fun `RetryBrokenChannel removes url from broken tracker`() = runTest {
        fakeTracker.markAsBroken(channel1.url)
        fakeChannels.channels = listOf(channel1)
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem()
            assertEquals(1, awaitItem().brokenChannelsCount)

            vm.onIntent(TdtIntent.RetryBrokenChannel(channel1))
            val state = awaitItem()
            assertEquals(0, state.brokenChannelsCount)
        }
    }

    @Test
    fun `initial playerState is IDLE`() = runTest {
        val vm = buildViewModel()
        vm.uiState.test {
            assertEquals(PlayerState.IDLE, awaitItem().playerState)
        }
    }

    @Test
    fun `Search updates searchQuery in state`() = runTest {
        fakeChannels.channels = listOf(channel1, channel2)
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem() // loaded

            vm.onIntent(TdtIntent.Search("rtve"))
            advanceTimeBy(TimeConstants.SEARCH_DEBOUNCE_MS + 1)

            assertEquals("rtve", awaitItem().searchQuery)
        }
    }

    @Test
    fun `Search filters filteredChannels by channel name`() = runTest {
        fakeChannels.channels = listOf(channel1, channel2, newsChannel) // "La 1", "La 2", "24h"
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem() // loaded

            vm.onIntent(TdtIntent.Search("La"))
            advanceTimeBy(TimeConstants.SEARCH_DEBOUNCE_MS + 1)

            val state = awaitItem()
            assertEquals(2, state.filteredChannels.size)
            assertTrue(state.filteredChannels.none { it.name == "24h" })
        }
    }

    @Test
    fun `Search with empty string restores full channel list`() = runTest {
        fakeChannels.channels = listOf(channel1, channel2, newsChannel)
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem() // loaded

            vm.onIntent(TdtIntent.Search("La"))
            advanceTimeBy(TimeConstants.SEARCH_DEBOUNCE_MS + 1)
            awaitItem() // filtered to 2

            vm.onIntent(TdtIntent.Search(""))
            advanceTimeBy(TimeConstants.SEARCH_DEBOUNCE_MS + 1)

            assertEquals(3, awaitItem().filteredChannels.size)
        }
    }

    @Test
    fun `StopPlayback before selecting any channel does not crash and keeps IDLE state`() = runTest {
        fakeChannels.channels = listOf(channel1)
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem() // loaded

            vm.onIntent(TdtIntent.StopPlayback) // player is null — must be safe

            expectNoEvents()
            assertNull(vm.uiState.value.currentChannel)
            assertEquals(PlayerState.IDLE, vm.uiState.value.playerState)
        }
    }

    @Test
    fun `isPlaying is false initially`() = runTest {
        val vm = buildViewModel()
        vm.uiState.test {
            assertFalse(awaitItem().isPlaying)
        }
    }

    @Test
    fun `currentChannel is null initially`() = runTest {
        val vm = buildViewModel()
        vm.uiState.test {
            assertNull(awaitItem().currentChannel)
        }
    }

    @Test
    fun `broken channel is excluded from filteredChannels when showBroken is false`() = runTest {
        fakeChannels.channels = listOf(channel1, channel2)
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem() // loaded

            fakeTracker.markAsBroken(channel1.url)
            val state = awaitItem()
            assertFalse(state.filteredChannels.any { it.url == channel1.url })
            assertEquals(1, state.filteredChannels.size)
        }
    }

    @Test
    fun `broken channel is included in filteredChannels when showBroken is true`() = runTest {
        fakeChannels.channels = listOf(channel1, channel2)
        fakeTracker.markAsBroken(channel1.url)
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem() // loaded (broken hidden by default)

            vm.onIntent(TdtIntent.ToggleShowBrokenChannels)
            val state = awaitItem()
            assertEquals(2, state.filteredChannels.size)
            assertTrue(state.filteredChannels.any { it.url == channel1.url })
        }
    }

    @Test
    fun `Search and category filter work together`() = runTest {
        val sportsChannel = Channel("Teledeporte", "tdp.m3u8", category = ChannelCategory.SPORTS)
        fakeChannels.channels = listOf(channel1, channel2, newsChannel, sportsChannel)
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem() // loaded

            vm.onIntent(TdtIntent.FilterByCategory(ChannelCategory.GENERAL))
            awaitItem() // filtered to GENERAL (channel1, channel2)

            vm.onIntent(TdtIntent.Search("1"))
            advanceTimeBy(TimeConstants.SEARCH_DEBOUNCE_MS + 1)

            val state = awaitItem()
            assertEquals(1, state.filteredChannels.size)
            assertEquals("La 1", state.filteredChannels.first().name)
        }
    }

    @Test
    fun `PausePlayer does not crash when no channel has been selected`() = runTest {
        fakeChannels.channels = listOf(channel1)
        val vm = buildViewModel()

        vm.uiState.test {
            awaitItem() // loaded
            vm.onIntent(TdtIntent.PausePlayer) // player is null — must be safe
            expectNoEvents()
        }
    }

    private fun buildViewModel() = TdtViewModel(
        getChannelsUseCase = GetChannelsUseCase(fakeChannels),
        brokenChannelTracker = fakeTracker,
        loadError = { e -> "Error: ${e.message}" },
        playerFactory = { error("Player not expected in unit tests") }
    )
}
