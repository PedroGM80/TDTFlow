package com.pedrogm.tdtflow.ui

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChannelFilterLogicTest {

    private fun channel(
        name: String = "Test Channel",
        url: String = "https://stream.m3u8",
        category: ChannelCategory = ChannelCategory.GENERAL
    ) = Channel(name = name, url = url, category = category)

    private val sampleChannels = listOf(
        channel(name = "Antena 3", url = "antena3.m3u8", category = ChannelCategory.GENERAL),
        channel(name = "La Sexta", url = "lasexta.m3u8", category = ChannelCategory.GENERAL),
        channel(name = "24 Horas", url = "24h.m3u8", category = ChannelCategory.NEWS),
        channel(name = "Teledeporte", url = "tdp.m3u8", category = ChannelCategory.SPORTS),
        channel(name = "Clan", url = "clan.m3u8", category = ChannelCategory.KIDS),
        channel(name = "Radio Música", url = "musica.m3u8", category = ChannelCategory.MUSIC),
        channel(name = "Broken Channel", url = "broken.m3u8", category = ChannelCategory.GENERAL),
        channel(name = "Blank Url", url = "", category = ChannelCategory.GENERAL),
        channel(name = "  ", url = "   ", category = ChannelCategory.GENERAL)
    )

    // ── filterValidUrls ─────────────────────────────────────────────────────

    @Test
    fun `channel with blank url is excluded`() {
        val result = ChannelFilterLogic.applyFilters(
            channels = sampleChannels,
            category = null,
            query = "",
            brokenUrls = emptySet(),
            showBroken = true
        )

        assertTrue(result.none { it.url.isBlank() })
    }

    // ── filterBrokenChannels ────────────────────────────────────────────────

    @Test
    fun `broken channel excluded when showBroken is false`() {
        val result = ChannelFilterLogic.applyFilters(
            channels = sampleChannels,
            category = null,
            query = "",
            brokenUrls = setOf("broken.m3u8"),
            showBroken = false
        )

        assertTrue(result.none { it.url == "broken.m3u8" })
    }

    @Test
    fun `broken channel included when showBroken is true`() {
        val result = ChannelFilterLogic.applyFilters(
            channels = sampleChannels,
            category = null,
            query = "",
            brokenUrls = setOf("broken.m3u8"),
            showBroken = true
        )

        assertTrue(result.any { it.url == "broken.m3u8" })
    }

    // ── filterByCategory ────────────────────────────────────────────────────

    @Test
    fun `category null (Todos) hides MUSIC channels and shows all others`() {
        val result = ChannelFilterLogic.applyFilters(
            channels = sampleChannels,
            category = null,
            query = "",
            brokenUrls = emptySet(),
            showBroken = true
        )

        assertTrue(result.none { it.category == ChannelCategory.MUSIC })
        assertTrue(result.any { it.category == ChannelCategory.GENERAL })
        assertTrue(result.any { it.category == ChannelCategory.NEWS })
        assertTrue(result.any { it.category == ChannelCategory.SPORTS })
        assertTrue(result.any { it.category == ChannelCategory.KIDS })
    }

    @Test
    fun `category NEWS returns only NEWS channels`() {
        val result = ChannelFilterLogic.applyFilters(
            channels = sampleChannels,
            category = ChannelCategory.NEWS,
            query = "",
            brokenUrls = emptySet(),
            showBroken = true
        )

        assertTrue(result.all { it.category == ChannelCategory.NEWS })
        assertEquals(1, result.size)
        assertEquals("24 Horas", result.first().name)
    }

    @Test
    fun `category MUSIC returns only MUSIC channels`() {
        val result = ChannelFilterLogic.applyFilters(
            channels = sampleChannels,
            category = ChannelCategory.MUSIC,
            query = "",
            brokenUrls = emptySet(),
            showBroken = true
        )

        assertTrue(result.all { it.category == ChannelCategory.MUSIC })
        assertEquals(1, result.size)
    }

    // ── filterByQuery ───────────────────────────────────────────────────────

    @Test
    fun `blank query returns all channels`() {
        val channels = listOf(
            channel(name = "Antena 3"),
            channel(name = "La Sexta")
        )

        val result = ChannelFilterLogic.applyFilters(
            channels = channels,
            category = null,
            query = "",
            brokenUrls = emptySet(),
            showBroken = true
        )

        assertEquals(2, result.size)
    }

    @Test
    fun `query matches channel name case-insensitively`() {
        val result = ChannelFilterLogic.applyFilters(
            channels = sampleChannels,
            category = null,
            query = "antena",
            brokenUrls = emptySet(),
            showBroken = true
        )

        assertEquals(1, result.size)
        assertEquals("Antena 3", result.first().name)
    }

    @Test
    fun `query with no matches returns empty list`() {
        val result = ChannelFilterLogic.applyFilters(
            channels = sampleChannels,
            category = null,
            query = "XYZ",
            brokenUrls = emptySet(),
            showBroken = true
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `whitespace-only query is treated as blank and returns all valid channels`() {
        val channels = listOf(
            channel(name = "Antena 3"),
            channel(name = "La Sexta")
        )

        val result = ChannelFilterLogic.applyFilters(
            channels = channels,
            category = null,
            query = "   ",
            brokenUrls = emptySet(),
            showBroken = true
        )

        assertEquals(2, result.size)
    }

    @Test
    fun `query does not match against channel url`() {
        val channels = listOf(
            channel(name = "Canal Uno", url = "antena-stream.m3u8", category = ChannelCategory.GENERAL)
        )

        val result = ChannelFilterLogic.applyFilters(
            channels = channels,
            category = null,
            query = "antena", // presente en la URL pero no en el nombre
            brokenUrls = emptySet(),
            showBroken = true
        )

        assertTrue(result.isEmpty())
    }

    // ── applyFilters full pipeline ──────────────────────────────────────────

    @Test
    fun `applyFilters combines broken, category, and query filters`() {
        val channels = listOf(
            channel(name = "Antena 3", url = "antena3.m3u8", category = ChannelCategory.GENERAL),
            channel(name = "Antena Noticias", url = "anoticias.m3u8", category = ChannelCategory.NEWS),
            channel(name = "La Sexta", url = "lasexta.m3u8", category = ChannelCategory.GENERAL),
            channel(name = "Broken Antena", url = "broken.m3u8", category = ChannelCategory.GENERAL),
            channel(name = "Blank", url = "", category = ChannelCategory.GENERAL)
        )

        val result = ChannelFilterLogic.applyFilters(
            channels = channels,
            category = ChannelCategory.GENERAL,
            query = "antena",
            brokenUrls = setOf("broken.m3u8"),
            showBroken = false
        )

        // Only "Antena 3" should survive: GENERAL + matches "antena" + not broken + valid url
        assertEquals(1, result.size)
        assertEquals("Antena 3", result.first().name)
    }

    @Test
    fun `applyFilters with all filters permissive returns valid non-music channels`() {
        val result = ChannelFilterLogic.applyFilters(
            channels = sampleChannels,
            category = null,
            query = "",
            brokenUrls = emptySet(),
            showBroken = true
        )

        // Excludes: blank urls (2) and MUSIC (1)
        assertEquals(6, result.size)
        assertTrue(result.none { it.url.isBlank() })
        assertTrue(result.none { it.category == ChannelCategory.MUSIC })
    }
}
