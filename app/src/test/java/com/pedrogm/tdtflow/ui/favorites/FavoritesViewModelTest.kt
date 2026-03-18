package com.pedrogm.tdtflow.ui.favorites

import com.pedrogm.tdtflow.domain.repository.FavoritesRepository
import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
        assertTrue(viewModel.favoriteIds.value.isEmpty())
    }

    @Test
    fun `addFavorite adds url to favorites`() {
        viewModel.addFavorite("rtve1.m3u8")

        assertTrue("rtve1.m3u8" in viewModel.favoriteIds.value)
    }

    @Test
    fun `addFavorite multiple urls accumulates all`() {
        viewModel.addFavorite("rtve1.m3u8")
        viewModel.addFavorite("antena3.m3u8")

        assertEquals(setOf("rtve1.m3u8", "antena3.m3u8"), viewModel.favoriteIds.value)
    }

    @Test
    fun `addFavorite same url twice does not duplicate`() {
        viewModel.addFavorite("rtve1.m3u8")
        viewModel.addFavorite("rtve1.m3u8")

        assertEquals(1, viewModel.favoriteIds.value.size)
    }

    @Test
    fun `removeFavorite removes url from favorites`() {
        viewModel.addFavorite("rtve1.m3u8")
        viewModel.addFavorite("antena3.m3u8")

        viewModel.removeFavorite("rtve1.m3u8")

        assertFalse("rtve1.m3u8" in viewModel.favoriteIds.value)
        assertTrue("antena3.m3u8" in viewModel.favoriteIds.value)
    }

    @Test
    fun `removeFavorite on absent url is a no-op`() {
        viewModel.addFavorite("rtve1.m3u8")

        viewModel.removeFavorite("unknown.m3u8")

        assertEquals(setOf("rtve1.m3u8"), viewModel.favoriteIds.value)
    }

    @Test
    fun `toggleFavorite adds when not present`() {
        viewModel.toggleFavorite("rtve1.m3u8")

        assertTrue("rtve1.m3u8" in viewModel.favoriteIds.value)
    }

    @Test
    fun `toggleFavorite removes when already present`() {
        viewModel.addFavorite("rtve1.m3u8")

        viewModel.toggleFavorite("rtve1.m3u8")

        assertFalse("rtve1.m3u8" in viewModel.favoriteIds.value)
    }

    @Test
    fun `isFavorite returns true for added url`() {
        viewModel.addFavorite("rtve1.m3u8")

        assertTrue(viewModel.isFavorite("rtve1.m3u8"))
    }

    @Test
    fun `isFavorite returns false for absent url`() {
        assertFalse(viewModel.isFavorite("rtve1.m3u8"))
    }

    @Test
    fun `isFavorite returns false after removal`() {
        viewModel.addFavorite("rtve1.m3u8")
        viewModel.removeFavorite("rtve1.m3u8")

        assertFalse(viewModel.isFavorite("rtve1.m3u8"))
    }
}

// ── Fake ────────────────────────────────────────────────────────────────────

private class FakeFavoritesRepository : FavoritesRepository {
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    override val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    override fun add(channelUrl: String) {
        _favoriteIds.update { it + channelUrl }
    }

    override fun remove(channelUrl: String) {
        _favoriteIds.update { it - channelUrl }
    }
}
