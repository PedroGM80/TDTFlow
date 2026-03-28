package com.pedrogm.tdtflow.ui.options

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import com.pedrogm.tdtflow.data.IOptionsPreferences
import com.pedrogm.tdtflow.data.NoOpOptionsPreferences
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
        viewModel = OptionsMenuViewModel(NoOpOptionsPreferences())
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
            assertEquals(AppLanguage.SYSTEM, state.language)
        }
    }

    @Test
    fun `Open event sets isOpen to true`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onIntent(OptionsMenuIntent.Open)

            val state = awaitItem()
            assertTrue(state.isOpen)
        }
    }

    @Test
    fun `Dismiss event sets isOpen to false`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(OptionsMenuIntent.Open)
            awaitItem() // open

            viewModel.onIntent(OptionsMenuIntent.Dismiss)
            val state = awaitItem()
            assertFalse(state.isOpen)
        }
    }

    @Test
    fun `SelectTheme DARK updates selectedTheme`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(OptionsMenuIntent.SelectTheme(AppTheme.DARK))

            val state = awaitItem()
            assertEquals(AppTheme.DARK, state.selectedTheme)
        }
    }

    @Test
    fun `SelectTheme LIGHT updates selectedTheme`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(OptionsMenuIntent.SelectTheme(AppTheme.LIGHT))

            val state = awaitItem()
            assertEquals(AppTheme.LIGHT, state.selectedTheme)
        }
    }

    @Test
    fun `SelectTheme SYSTEM updates selectedTheme`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(OptionsMenuIntent.SelectTheme(AppTheme.DARK))
            awaitItem()

            viewModel.onIntent(OptionsMenuIntent.SelectTheme(AppTheme.SYSTEM))
            val state = awaitItem()
            assertEquals(AppTheme.SYSTEM, state.selectedTheme)
        }
    }

    @Test
    fun `SelectLanguage ES updates language`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(OptionsMenuIntent.SelectLanguage(AppLanguage.ES))

            val state = awaitItem()
            assertEquals(AppLanguage.ES, state.language)
        }
    }

    @Test
    fun `SelectLanguage EN updates language`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(OptionsMenuIntent.SelectLanguage(AppLanguage.EN))

            val state = awaitItem()
            assertEquals(AppLanguage.EN, state.language)
        }
    }

    @Test
    fun `SelectLanguage CA updates language`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(OptionsMenuIntent.SelectLanguage(AppLanguage.CA))

            val state = awaitItem()
            assertEquals(AppLanguage.CA, state.language)
        }
    }

    @Test
    fun `multiple events update state cumulatively`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(OptionsMenuIntent.Open)
            awaitItem()

            viewModel.onIntent(OptionsMenuIntent.SelectTheme(AppTheme.DARK))
            awaitItem()

            viewModel.onIntent(OptionsMenuIntent.SelectLanguage(AppLanguage.CA))
            val state = awaitItem()

            assertTrue(state.isOpen)
            assertEquals(AppTheme.DARK, state.selectedTheme)
            assertEquals(AppLanguage.CA, state.language)
        }
    }

    // ── prefs persistence ───────────────────────────────────────────────────

    @Test
    fun `SelectTheme persists theme name to prefs`() = runTest {
        val recorded = mutableListOf<String>()
        val spyPrefs = object : IOptionsPreferences by NoOpOptionsPreferences() {
            override fun saveTheme(name: String) { recorded += name }
        }
        val vm = OptionsMenuViewModel(spyPrefs)

        vm.onIntent(OptionsMenuIntent.SelectTheme(AppTheme.DARK))

        assertEquals(listOf("DARK"), recorded)
    }

    @Test
    fun `SelectLanguage persists language name to prefs`() = runTest {
        val recorded = mutableListOf<String>()
        val spyPrefs = object : IOptionsPreferences by NoOpOptionsPreferences() {
            override fun saveLanguage(name: String) { recorded += name }
        }
        val vm = OptionsMenuViewModel(spyPrefs)

        vm.onIntent(OptionsMenuIntent.SelectLanguage(AppLanguage.ES))

        assertEquals(listOf("ES"), recorded)
    }

    @Test
    fun `saved theme is restored on init`() = runTest {
        val prefsWithDark = object : IOptionsPreferences by NoOpOptionsPreferences() {
            override fun loadTheme(): String = "DARK"
        }
        val vm = OptionsMenuViewModel(prefsWithDark)

        vm.uiState.test {
            assertEquals(AppTheme.DARK, awaitItem().selectedTheme)
        }
    }

    @Test
    fun `saved language is restored on init`() = runTest {
        val prefsWithEs = object : IOptionsPreferences by NoOpOptionsPreferences() {
            override fun loadLanguage(): String = "ES"
        }
        val vm = OptionsMenuViewModel(prefsWithEs)

        vm.uiState.test {
            assertEquals(AppLanguage.ES, awaitItem().language)
        }
    }

    @Test
    fun `invalid saved theme falls back to SYSTEM`() = runTest {
        val corruptedPrefs = object : IOptionsPreferences by NoOpOptionsPreferences() {
            override fun loadTheme(): String = "INVALID_VALUE"
        }
        val vm = OptionsMenuViewModel(corruptedPrefs)

        vm.uiState.test {
            assertEquals(AppTheme.SYSTEM, awaitItem().selectedTheme)
        }
    }

    @Test
    fun `invalid saved language falls back to SYSTEM`() = runTest {
        val corruptedPrefs = object : IOptionsPreferences by NoOpOptionsPreferences() {
            override fun loadLanguage(): String = "BOGUS"
        }
        val vm = OptionsMenuViewModel(corruptedPrefs)

        vm.uiState.test {
            assertEquals(AppLanguage.SYSTEM, awaitItem().language)
        }
    }
}
