package com.pedrogm.tdtflow.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ChannelTest {

    // ── init validation ─────────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun `blank name throws IllegalArgumentException`() {
        Channel(name = "  ", url = "https://stream.m3u8")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty name throws IllegalArgumentException`() {
        Channel(name = "", url = "https://stream.m3u8")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank url throws IllegalArgumentException`() {
        Channel(name = "La 1", url = "   ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty url throws IllegalArgumentException`() {
        Channel(name = "La 1", url = "")
    }

    @Test
    fun `valid channel is created successfully`() {
        val channel = Channel(name = "La 1", url = "https://rtve.m3u8")
        assertEquals("La 1", channel.name)
        assertEquals("https://rtve.m3u8", channel.url)
    }

    @Test
    fun `default values are applied`() {
        val channel = Channel(name = "La 1", url = "https://rtve.m3u8")
        assertEquals("", channel.logo)
        assertEquals(ChannelCategory.GENERAL, channel.category)
        assertEquals("", channel.epgId)
    }
}
