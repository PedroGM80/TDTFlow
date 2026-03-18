package com.pedrogm.tdtflow.domain.usecase

import com.pedrogm.tdtflow.fakes.FakeFavoritesRepository
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class GetFavoritesUseCaseTest {

    private lateinit var repository: FakeFavoritesRepository
    private lateinit var getFavorites: GetFavoritesUseCase

    @Before
    fun setUp() {
        repository = FakeFavoritesRepository()
        getFavorites = GetFavoritesUseCase(repository)
    }

    @Test
    fun `invoke returns the same StateFlow instance as the repository`() {
        assertSame(repository.favoriteIds, getFavorites())
    }
}
