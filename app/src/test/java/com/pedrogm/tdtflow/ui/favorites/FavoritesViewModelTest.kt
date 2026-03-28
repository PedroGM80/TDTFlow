package com.pedrogm.tdtflow.ui.favorites

import app.cash.turbine.test
import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import com.pedrogm.tdtflow.fakes.FakeFavoritesRepository
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
class FavoritesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeFavoritesRepository
    private lateinit var viewModel: FavoritesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeFavoritesRepository()
        viewModel = FavoritesViewModel(
            addFavorite = AddFavoriteUseCase(repository),
            removeFavorite = RemoveFavoriteUseCase(repository),
            getFavorites = GetFavoritesUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no favorites`() = runTest {
        viewModel.uiState.test {
            assertTrue(awaitItem().favoriteIds.isEmpty())
        }
    }

    @Test
    fun `AddFavorite intent adds url to favorites`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))

            assertTrue("rtve1.m3u8" in awaitItem().favoriteIds)
        }
    }

    @Test
    fun `AddFavorite multiple urls accumulates all`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))
            awaitItem()

            viewModel.onIntent(FavoritesIntent.AddFavorite("antena3.m3u8"))
            val state = awaitItem()

            assertEquals(setOf("rtve1.m3u8", "antena3.m3u8"), state.favoriteIds)
        }
    }

    @Test
    fun `AddFavorite same url twice does not duplicate`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))
            awaitItem()

            viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))
            expectNoEvents() // no new emission since the set hasn't changed

            assertEquals(1, viewModel.uiState.value.favoriteIds.size)
        }
    }

    @Test
    fun `RemoveFavorite intent removes url from favorites`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))
            awaitItem()
            viewModel.onIntent(FavoritesIntent.AddFavorite("antena3.m3u8"))
            awaitItem()

            viewModel.onIntent(FavoritesIntent.RemoveFavorite("rtve1.m3u8"))
            val state = awaitItem()

            assertFalse("rtve1.m3u8" in state.favoriteIds)
            assertTrue("antena3.m3u8" in state.favoriteIds)
        }
    }

    @Test
    fun `RemoveFavorite on absent url is a no-op`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))
            awaitItem()

            viewModel.onIntent(FavoritesIntent.RemoveFavorite("unknown.m3u8"))
            expectNoEvents() // no change in state

            assertEquals(setOf("rtve1.m3u8"), viewModel.uiState.value.favoriteIds)
        }
    }

    @Test
    fun `ToggleFavorite adds when not present`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(FavoritesIntent.ToggleFavorite("rtve1.m3u8"))

            assertTrue("rtve1.m3u8" in awaitItem().favoriteIds)
        }
    }

    @Test
    fun `ToggleFavorite removes when already present`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))
            awaitItem()

            viewModel.onIntent(FavoritesIntent.ToggleFavorite("rtve1.m3u8"))

            assertFalse("rtve1.m3u8" in awaitItem().favoriteIds)
        }
    }

    @Test
    fun `added url appears in state favoriteIds`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))

            assertTrue("rtve1.m3u8" in awaitItem().favoriteIds)
        }
    }

    @Test
    fun `absent url is not in state favoriteIds`() = runTest {
        viewModel.uiState.test {
            assertFalse("rtve1.m3u8" in awaitItem().favoriteIds)
        }
    }

    @Test
    fun `removed url is no longer in state favoriteIds`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial

            viewModel.onIntent(FavoritesIntent.AddFavorite("rtve1.m3u8"))
            awaitItem()

            viewModel.onIntent(FavoritesIntent.RemoveFavorite("rtve1.m3u8"))

            assertFalse("rtve1.m3u8" in awaitItem().favoriteIds)
        }
    }
}
