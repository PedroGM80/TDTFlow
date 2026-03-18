package com.pedrogm.tdtflow.domain.usecase

import com.pedrogm.tdtflow.fakes.FakeFavoritesRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddFavoriteUseCaseTest {

    private lateinit var repository: FakeFavoritesRepository
    private lateinit var addFavorite: AddFavoriteUseCase

    @Before
    fun setUp() {
        repository = FakeFavoritesRepository()
        addFavorite = AddFavoriteUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository add`() {
        addFavorite("rtve1.m3u8")

        assertTrue("rtve1.m3u8" in repository.favoriteIds.value)
    }
}
