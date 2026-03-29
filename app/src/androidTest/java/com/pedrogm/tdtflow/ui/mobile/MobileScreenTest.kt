package com.pedrogm.tdtflow.ui.mobile

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pedrogm.tdtflow.data.NoOpOptionsPreferences
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.GetChannelsUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import com.pedrogm.tdtflow.fakes.FakeBrokenChannelTracker
import com.pedrogm.tdtflow.fakes.FakeChannelsRepository
import com.pedrogm.tdtflow.fakes.FakeFavoritesRepository
import com.pedrogm.tdtflow.ui.PlayerController
import com.pedrogm.tdtflow.ui.TdtViewModel
import com.pedrogm.tdtflow.ui.favorites.FavoritesViewModel
import com.pedrogm.tdtflow.ui.options.OptionsMenuViewModel
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MobileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val channel1 = Channel("Antena 3", "https://stream1.m3u8", category = ChannelCategory.GENERAL)
    private val channel2 = Channel("La Sexta", "https://stream2.m3u8", category = ChannelCategory.GENERAL)

    private fun buildTdtViewModel(
        channels: List<Channel> = listOf(channel1, channel2),
        error: Throwable? = null
    ): TdtViewModel {
        val tracker = FakeBrokenChannelTracker()
        return TdtViewModel(
            getChannelsUseCase = GetChannelsUseCase(FakeChannelsRepository(channels = channels, error = error)),
            brokenChannelTracker = tracker,
            loadError = { e -> "Error: ${e.message}" },
            playerControllerFactory = { scope ->
                PlayerController(
                    playerFactory = { error("No player in UI tests") },
                    brokenChannelTracker = tracker,
                    scope = scope
                )
            }
        )
    }

    private fun buildFavoritesViewModel(): FavoritesViewModel {
        val repo = FakeFavoritesRepository()
        return FavoritesViewModel(
            addFavorite = AddFavoriteUseCase(repo),
            removeFavorite = RemoveFavoriteUseCase(repo),
            getFavorites = GetFavoritesUseCase(repo)
        )
    }

    private fun buildOptionsViewModel() = OptionsMenuViewModel(NoOpOptionsPreferences())

    private fun setContent(
        tdtViewModel: TdtViewModel = buildTdtViewModel(),
        favoritesViewModel: FavoritesViewModel = buildFavoritesViewModel(),
        optionsViewModel: OptionsMenuViewModel = buildOptionsViewModel(),
        onNavigateToFavorites: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            TDTFlowTheme {
                MobileScreen(
                    viewModel = tdtViewModel,
                    favoritesViewModel = favoritesViewModel,
                    optionsViewModel = optionsViewModel,
                    onNavigateToFavorites = onNavigateToFavorites
                )
            }
        }
    }

    // ── App bar ─────────────────────────────────────────────────────────────

    @Test
    fun appBar_showsAppName() {
        setContent()

        composeTestRule
            .onNodeWithText("TDTFlow")
            .assertIsDisplayed()
    }

    @Test
    fun appBar_showsFavoritesButton() {
        setContent()

        composeTestRule
            .onNodeWithContentDescription("Favoritos")
            .assertIsDisplayed()
    }

    @Test
    fun appBar_showsSearchButton() {
        setContent()

        composeTestRule
            .onNodeWithContentDescription("Buscar")
            .assertIsDisplayed()
    }

    @Test
    fun appBar_showsSettingsButton() {
        setContent()

        composeTestRule
            .onNodeWithContentDescription("Opciones")
            .assertIsDisplayed()
    }

    // ── Channel grid ─────────────────────────────────────────────────────────

    @Test
    fun channelNames_areDisplayed_whenLoaded() {
        setContent()

        composeTestRule
            .onNodeWithText("Antena 3")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("La Sexta")
            .assertIsDisplayed()
    }

    @Test
    fun emptyState_isDisplayed_whenNoChannels() {
        setContent(tdtViewModel = buildTdtViewModel(channels = emptyList()))

        composeTestRule
            .onNodeWithText("No se encontraron canales")
            .assertIsDisplayed()
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    @Test
    fun clickingFavoritesButton_invokesOnNavigateToFavorites() {
        var navigated = false
        setContent(onNavigateToFavorites = { navigated = true })

        composeTestRule
            .onNodeWithContentDescription("Favoritos")
            .performClick()

        assertTrue(navigated)
    }

    // ── Search ───────────────────────────────────────────────────────────────

    @Test
    fun searchBar_isHidden_byDefault() {
        setContent()

        composeTestRule
            .onNodeWithText("Buscar canal\u2026")
            .assertDoesNotExist()
    }

    @Test
    fun searchBar_isShown_afterClickingSearchButton() {
        setContent()

        composeTestRule
            .onNodeWithContentDescription("Buscar")
            .performClick()

        composeTestRule
            .onNodeWithText("Buscar canal\u2026")
            .assertIsDisplayed()
    }
}
