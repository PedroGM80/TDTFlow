package com.pedrogm.tdtflow.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChannelExtensionsTest {

    private fun ch(name: String, url: String) = Channel(name = name, url = url)

    private val channels = listOf(
        ch("La 1", "rtve1.m3u8"),
        ch("La 2", "rtve2.m3u8"),
        ch("Antena 3", "antena3.m3u8")
    )

    @Test
    fun `filterByUrls returns matching channels`() {
        val result = channels.filterByUrls(setOf("rtve1.m3u8", "antena3.m3u8"))
        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "La 1" })
        assertTrue(result.any { it.name == "Antena 3" })
    }

    @Test
    fun `filterByUrls preserves order of url set`() {
        // LinkedHashSet preserves insertion order
        val urls = linkedSetOf("antena3.m3u8", "rtve1.m3u8")
        val result = channels.filterByUrls(urls)
        assertEquals("Antena 3", result[0].name)
        assertEquals("La 1", result[1].name)
    }

    @Test
    fun `filterByUrls ignores unknown urls`() {
        val result = channels.filterByUrls(setOf("unknown.m3u8"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterByUrls with empty set returns empty list`() {
        val result = channels.filterByUrls(emptySet())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterByUrls on empty channel list returns empty list`() {
        val result = emptyList<Channel>().filterByUrls(setOf("rtve1.m3u8"))
        assertTrue(result.isEmpty())
    }
}
