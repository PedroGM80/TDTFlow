package com.pedrogm.tdtflow.data.repository

import com.pedrogm.tdtflow.domain.model.ChannelCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FallbackChannelsTest {

    private val channels = fallbackChannels()

    // ── basic invariants ────────────────────────────────────────────────────

    @Test
    fun `fallback list is not empty`() {
        assertTrue(channels.isNotEmpty())
    }

    @Test
    fun `all channels have non-blank name`() {
        assertTrue(channels.all { it.name.isNotBlank() })
    }

    @Test
    fun `all channels have non-blank url`() {
        assertTrue(channels.all { it.url.isNotBlank() })
    }

    @Test
    fun `all channel urls are unique`() {
        val urls = channels.map { it.url }
        assertEquals(urls.size, urls.toSet().size)
    }

    // ── category coverage ───────────────────────────────────────────────────

    @Test
    fun `fallback includes at least one GENERAL channel`() {
        assertTrue(channels.any { it.category == ChannelCategory.GENERAL })
    }

    @Test
    fun `fallback includes at least one NEWS channel`() {
        assertTrue(channels.any { it.category == ChannelCategory.NEWS })
    }

    @Test
    fun `fallback includes at least one SPORTS channel`() {
        assertTrue(channels.any { it.category == ChannelCategory.SPORTS })
    }

    @Test
    fun `fallback includes at least one KIDS channel`() {
        assertTrue(channels.any { it.category == ChannelCategory.KIDS })
    }

    @Test
    fun `fallback includes at least one REGIONAL channel`() {
        assertTrue(channels.any { it.category == ChannelCategory.REGIONAL })
    }

    @Test
    fun `fallback includes at least one MUSIC channel`() {
        assertTrue(channels.any { it.category == ChannelCategory.MUSIC })
    }

    @Test
    fun `fallback contains no ENTERTAINMENT or OTHER categories`() {
        assertFalse(channels.any { it.category == ChannelCategory.ENTERTAINMENT })
        assertFalse(channels.any { it.category == ChannelCategory.OTHER })
    }

    // ── key channels present ────────────────────────────────────────────────

    @Test
    fun `La 1 is present in fallback`() {
        assertTrue(channels.any { it.name == "La 1" })
    }

    @Test
    fun `24 Horas is present in fallback`() {
        assertTrue(channels.any { it.name == "24 Horas" })
    }

    @Test
    fun `Teledeporte is present in fallback`() {
        assertTrue(channels.any { it.name == "Teledeporte" })
    }

    @Test
    fun `Clan is present in fallback`() {
        assertTrue(channels.any { it.name == "Clan" })
    }
}
