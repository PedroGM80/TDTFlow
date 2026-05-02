package com.pedrogm.tdtflow.ui.favorites

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.ClearFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.ImportFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import com.pedrogm.tdtflow.fakes.FakeFavoritesRepository
import com.pedrogm.tdtflow.ui.theme.TDTFlowTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FavoritesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val channel1 = Channel(
        name = "Antena 3",
        url = "https://stream1.example.com",
        logo = "",
        category = ChannelCategory.GENERAL
    )

    private val channel2 = Channel(
        name = "La Sexta",
        url = "https://stream2.example.com",
        logo = "",
        category = ChannelCategory.GENERAL
    )

    private val channel3 = Channel(
        name = "Telecinco",
        url = "https://stream3.example.com",
        logo = "",
        category = ChannelCategory.ENTERTAINMENT
    )

    private fun createViewModel(
        initialFavorites: Set<String> = emptySet()
    ): FavoritesViewModel {
        val repo = FakeFavoritesRepository()
        initialFavorites.forEach { repo.add(it) }
        return FavoritesViewModel(
            addFavorite = AddFavoriteUseCase(repo),
            removeFavorite = RemoveFavoriteUseCase(repo),
            getFavorites = GetFavoritesUseCase(repo),
            importFavorites = ImportFavoritesUseCase(repo),
            clearFavorites = ClearFavoritesUseCase(repo)
        )
    }

    private fun setContent(
        allChannels: List<Channel> = listOf(channel1, channel2, channel3),
        currentChannel: Channel? = null,
        viewModel: FavoritesViewModel = createViewModel(),
        onChannelClick: (Channel) -> Unit = {},
        onBack: () -> Unit = {}
    ) {
        composeTestRule.setContent {
            TDTFlowTheme {
                FavoritesScreen(
                    allChannels = allChannels,
                    currentChannel = currentChannel,
                    viewModel = viewModel,
                    onChannelClick = onChannelClick,
                    onBack = onBack
                )
            }
        }
    }

    @Test
    fun emptyState_shown_whenNoFavorites() {
        setContent(viewModel = createViewModel(initialFavorites = emptySet()))

        composeTestRule
            .onNodeWithText(context.getString(R.string.no_favorites))
            .assertIsDisplayed()
    }

    @Test
    fun channelGrid_shown_whenFavoritesExist() {
        val vm = createViewModel(initialFavorites = setOf(channel1.url, channel2.url))
        setContent(viewModel = vm)

        composeTestRule
            .onNodeWithText(channel1.name)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(channel2.name)
            .assertIsDisplayed()
    }

    @Test
    fun channelCard_showsChannelName() {
        val vm = createViewModel(initialFavorites = setOf(channel1.url))
        setContent(viewModel = vm)

        composeTestRule
            .onNodeWithText("Antena 3")
            .assertIsDisplayed()
    }

    @Test
    fun clickingChannel_invokesOnChannelClick() {
        val clickedChannels = mutableListOf<Channel>()
        val vm = createViewModel(initialFavorites = setOf(channel1.url))

        setContent(
            viewModel = vm,
            onChannelClick = { clickedChannels.add(it) }
        )

        composeTestRule
            .onNodeWithText(channel1.name)
            .performClick()

        assertEquals(1, clickedChannels.size)
        assertEquals(channel1, clickedChannels.first())
    }

    @Test
    fun backButton_invokesOnBack() {
        var backClicked = false
        setContent(onBack = { backClicked = true })

        composeTestRule
            .onNodeWithContentDescription(context.getString(R.string.back_description))
            .performClick()

        assertTrue(backClicked)
    }

    @Test
    fun onlyFavoriteChannels_shownInGrid() {
        val vm = createViewModel(initialFavorites = setOf(channel1.url))
        setContent(viewModel = vm)

        composeTestRule
            .onNodeWithText(channel1.name)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(channel2.name)
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText(channel3.name)
            .assertDoesNotExist()
    }
}
