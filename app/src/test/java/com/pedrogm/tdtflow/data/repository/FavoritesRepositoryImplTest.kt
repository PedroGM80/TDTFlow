package com.pedrogm.tdtflow.data.repository

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavoritesRepositoryImplTest {

    private lateinit var repository: FavoritesRepositoryImpl

    @Before
    fun setUp() {
        repository = FavoritesRepositoryImpl()
    }

    @Test
    fun `add inserts url into the set`() {
        repository.add("rtve1.m3u8")

        assertTrue("rtve1.m3u8" in repository.favoriteIds.value)
    }

    @Test
    fun `add same url twice does not duplicate`() {
        repository.add("rtve1.m3u8")
        repository.add("rtve1.m3u8")

        assertEquals(1, repository.favoriteIds.value.size)
    }

    @Test
    fun `remove removes url from set`() {
        repository.add("rtve1.m3u8")
        repository.add("antena3.m3u8")

        repository.remove("rtve1.m3u8")

        assertFalse("rtve1.m3u8" in repository.favoriteIds.value)
        assertTrue("antena3.m3u8" in repository.favoriteIds.value)
    }

    @Test
    fun `remove absent url is a no-op`() {
        repository.add("rtve1.m3u8")

        repository.remove("unknown.m3u8")

        assertEquals(setOf("rtve1.m3u8"), repository.favoriteIds.value)
    }

    @Test
    fun `favoriteIds emits updated values on each change`() = runTest {
        repository.favoriteIds.test {
            assertEquals(emptySet<String>(), awaitItem())

            repository.add("rtve1.m3u8")
            assertEquals(setOf("rtve1.m3u8"), awaitItem())

            repository.add("antena3.m3u8")
            assertEquals(setOf("rtve1.m3u8", "antena3.m3u8"), awaitItem())

            repository.remove("rtve1.m3u8")
            assertEquals(setOf("antena3.m3u8"), awaitItem())
        }
    }

    @Test
    fun `add preserves insertion order`() {
        repository.add("url_c")
        repository.add("url_a")
        repository.add("url_b")

        assertEquals(listOf("url_c", "url_a", "url_b"), repository.favoriteIds.value.toList())
    }

    @Test
    fun `remove preserves order of remaining items`() {
        repository.add("url_1")
        repository.add("url_2")
        repository.add("url_3")

        repository.remove("url_2")

        assertEquals(listOf("url_1", "url_3"), repository.favoriteIds.value.toList())
    }
}
