package com.pedrogm.tdtflow.domain.usecase

import com.pedrogm.tdtflow.fakes.FakeFavoritesRepository
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

class RemoveFavoriteUseCaseTest {

    private lateinit var repository: FakeFavoritesRepository
    private lateinit var removeFavorite: RemoveFavoriteUseCase

    @Before
    fun setUp() {
        repository = FakeFavoritesRepository()
        removeFavorite = RemoveFavoriteUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository remove`() {
        repository.add("rtve1.m3u8")

        removeFavorite("rtve1.m3u8")

        assertFalse("rtve1.m3u8" in repository.favoriteIds.value)
    }
}
