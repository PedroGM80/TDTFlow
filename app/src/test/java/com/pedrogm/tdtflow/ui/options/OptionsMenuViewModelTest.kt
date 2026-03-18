package com.pedrogm.tdtflow.ui.options

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OptionsMenuViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: OptionsMenuViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = OptionsMenuViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isOpen)
            assertEquals(AppTheme.SYSTEM, state.selectedTheme)
            assertFalse(state.showBrokenChannels)
            assertEquals(AppLanguage.SYSTEM, state.language)
        }
    }

    @Test
    fun `Open event sets isOpen to true`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onEvent(OptionsMenuEvent.Open)

            val state = awaitItem()
            assertTrue(state.isOpen)
        }
    }

    @Test
    fun `Dismiss event sets isOpen to false`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(OptionsMenuEvent.Open)
            awaitItem() // open

            viewModel.onEvent(OptionsMenuEvent.Dismiss)
            val state = awaitItem()
            assertFalse(state.isOpen)
        }
    }

    @Test
    fun `SelectTheme DARK updates selectedTheme`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(OptionsMenuEvent.SelectTheme(AppTheme.DARK))

            val state = awaitItem()
            assertEquals(AppTheme.DARK, state.selectedTheme)
        }
    }

    @Test
    fun `SelectTheme LIGHT updates selectedTheme`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(OptionsMenuEvent.SelectTheme(AppTheme.LIGHT))

            val state = awaitItem()
            assertEquals(AppTheme.LIGHT, state.selectedTheme)
        }
    }

    @Test
    fun `SelectTheme SYSTEM updates selectedTheme`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(OptionsMenuEvent.SelectTheme(AppTheme.DARK))
            awaitItem()

            viewModel.onEvent(OptionsMenuEvent.SelectTheme(AppTheme.SYSTEM))
            val state = awaitItem()
            assertEquals(AppTheme.SYSTEM, state.selectedTheme)
        }
    }

    @Test
    fun `ToggleShowBrokenChannels flips from false to true`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial (false)

            viewModel.onEvent(OptionsMenuEvent.ToggleShowBrokenChannels)

            val state = awaitItem()
            assertTrue(state.showBrokenChannels)
        }
    }

    @Test
    fun `ToggleShowBrokenChannels flips from true to false`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(OptionsMenuEvent.ToggleShowBrokenChannels)
            awaitItem() // true

            viewModel.onEvent(OptionsMenuEvent.ToggleShowBrokenChannels)
            val state = awaitItem()
            assertFalse(state.showBrokenChannels)
        }
    }

    @Test
    fun `SelectLanguage ES updates language`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(OptionsMenuEvent.SelectLanguage(AppLanguage.ES))

            val state = awaitItem()
            assertEquals(AppLanguage.ES, state.language)
        }
    }

    @Test
    fun `SelectLanguage EN updates language`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(OptionsMenuEvent.SelectLanguage(AppLanguage.EN))

            val state = awaitItem()
            assertEquals(AppLanguage.EN, state.language)
        }
    }

    @Test
    fun `SelectLanguage CA updates language`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(OptionsMenuEvent.SelectLanguage(AppLanguage.CA))

            val state = awaitItem()
            assertEquals(AppLanguage.CA, state.language)
        }
    }

    @Test
    fun `multiple events update state cumulatively`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onEvent(OptionsMenuEvent.Open)
            awaitItem()

            viewModel.onEvent(OptionsMenuEvent.SelectTheme(AppTheme.DARK))
            awaitItem()

            viewModel.onEvent(OptionsMenuEvent.ToggleShowBrokenChannels)
            awaitItem()

            viewModel.onEvent(OptionsMenuEvent.SelectLanguage(AppLanguage.CA))
            val state = awaitItem()

            assertTrue(state.isOpen)
            assertEquals(AppTheme.DARK, state.selectedTheme)
            assertTrue(state.showBrokenChannels)
            assertEquals(AppLanguage.CA, state.language)
        }
    }
}
