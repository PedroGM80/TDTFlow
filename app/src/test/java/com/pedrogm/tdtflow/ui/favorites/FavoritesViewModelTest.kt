package com.pedrogm.tdtflow.ui.favorites

import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import com.pedrogm.tdtflow.fakes.FakeFavoritesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavoritesViewModelTest {

    private lateinit var repository: FakeFavoritesRepository
    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setUp() {
        repository = FakeFavoritesRepository()
        viewModel = FavoritesViewModel(
            addFavorite = AddFavoriteUseCase(repository),
            removeFavorite = RemoveFavoriteUseCase(repository),
            getFavorites = GetFavoritesUseCase(repository)
        )
    }

    @Test
    fun `initial state has no favorites`() {
        assertTrue(viewModel.uiState.value.favoriteIds.isEmpty())
    }

    @Test
    fun `AddFavorite intent adds url to favorites`() {
        viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))

        assertTrue("rtve1.m3u8" in viewModel.uiState.value.favoriteIds)
    }

    @Test
    fun `AddFavorite multiple urls accumulates all`() {
        viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))
        viewModel.onIntent(FavoritesIntent.AddFavorite("antena3.m3u8"))

        assertEquals(setOf("rtve1.m3u8", "antena3.m3u8"), viewModel.uiState.value.favoriteIds)
    }

    @Test
    fun `AddFavorite same url twice does not duplicate`() {
        viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))
        viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))

        assertEquals(1, viewModel.uiState.value.favoriteIds.size)
    }

    @Test
    fun `RemoveFavorite intent removes url from favorites`() {
        viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))
        viewModel.onIntent(FavoritesIntent.AddFavorite("antena3.m3u8"))

        viewModel.onIntent(FavoritesIntent.RemoveFavorite("rtve1.m3u8"))

        assertFalse("rtve1.m3u8" in viewModel.uiState.value.favoriteIds)
        assertTrue("antena3.m3u8" in viewModel.uiState.value.favoriteIds)
    }

    @Test
    fun `RemoveFavorite on absent url is a no-op`() {
        viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))

        viewModel.onIntent(FavoritesIntent.RemoveFavorite("unknown.m3u8"))

        assertEquals(setOf("rtve1.m3u8"), viewModel.uiState.value.favoriteIds)
    }

    @Test
    fun `ToggleFavorite adds when not present`() {
        viewModel.onIntent(FavoritesIntent.ToggleFavorite("rtve1.m3u8"))

        assertTrue("rtve1.m3u8" in viewModel.uiState.value.favoriteIds)
    }

    @Test
    fun `ToggleFavorite removes when already present`() {
        viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))

        viewModel.onIntent(FavoritesIntent.ToggleFavorite("rtve1.m3u8"))

        assertFalse("rtve1.m3u8" in viewModel.uiState.value.favoriteIds)
    }

    @Test
    fun `isFavorite returns true for added url`() {
        viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))

        assertTrue(viewModel.isFavorite("rtve1.m3u8"))
    }

    @Test
    fun `isFavorite returns false for absent url`() {
        assertFalse(viewModel.isFavorite("rtve1.m3u8"))
    }

    @Test
    fun `isFavorite returns false after removal`() {
        viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))
        viewModel.onIntent(FavoritesIntent.RemoveFavorite("rtve1.m3u8"))

        assertFalse(viewModel.isFavorite("rtve1.m3u8"))
    }
}
