package com.pedrogm.tdtflow.data.repository

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class ChannelMergeTest {

    private fun channel(
        name: String,
        url: String,
        logo: String = "",
        category: ChannelCategory = ChannelCategory.GENERAL
    ) = Channel(name = name, url = url, logo = logo, category = category)

    // ── API priority ────────────────────────────────────────────────────────

    @Test
    fun `API channels take priority over fallback for the same URL`() {
        val fallback = listOf(channel("La 1", "rtve1.m3u8", logo = "fallback-logo.png"))
        val api = listOf(channel("La 1", "rtve1.m3u8", logo = "api-logo.png"))

        val result = mergeChannelsWithFallback(fallback, api)

        assertEquals(1, result.size)
        assertEquals("api-logo.png", result.first().logo)
    }

    @Test
    fun `fallback channel is included when its URL is absent from API`() {
        val fallback = listOf(channel("Canal Regional", "regional.m3u8"))
        val api = listOf(channel("La 1", "rtve1.m3u8"))

        val result = mergeChannelsWithFallback(fallback, api)

        assertEquals(2, result.size)
    }

    // ── logo enrichment ─────────────────────────────────────────────────────

    @Test
    fun `fallback logo fills in empty logo of matching API channel`() {
        val fallback = listOf(channel("La 1", "rtve1.m3u8", logo = "fallback-logo.png"))
        val api = listOf(channel("La 1", "rtve1-api.m3u8", logo = ""))  // different URL, same name

        val result = mergeChannelsWithFallback(fallback, api)

        val la1 = result.first { it.url == "rtve1-api.m3u8" }
        assertEquals("fallback-logo.png", la1.logo)
    }

    @Test
    fun `existing API logo is not overwritten by fallback`() {
        val fallback = listOf(channel("La 1", "rtve1.m3u8", logo = "fallback-logo.png"))
        val api = listOf(channel("La 1", "rtve1-api.m3u8", logo = "api-logo.png"))

        val result = mergeChannelsWithFallback(fallback, api)

        val la1 = result.first { it.url == "rtve1-api.m3u8" }
        assertEquals("api-logo.png", la1.logo)
    }

    @Test
    fun `channel with no fallback match keeps empty logo`() {
        val fallback = listOf(channel("Other", "other.m3u8", logo = "other-logo.png"))
        val api = listOf(channel("La 1", "rtve1.m3u8", logo = ""))

        val result = mergeChannelsWithFallback(fallback, api)

        val la1 = result.first { it.url == "rtve1.m3u8" }
        assertEquals("", la1.logo)
    }

    // ── deduplication ───────────────────────────────────────────────────────

    @Test
    fun `duplicate URLs are deduplicated — API version kept`() {
        val fallback = listOf(channel("La 1 FB", "rtve1.m3u8"))
        val api = listOf(channel("La 1 API", "rtve1.m3u8"))

        val result = mergeChannelsWithFallback(fallback, api)

        assertEquals(1, result.size)
        assertEquals("La 1 API", result.first().name)
    }

    @Test
    fun `channels with different URLs are both kept`() {
        val fallback = listOf(channel("A", "url-a.m3u8"))
        val api = listOf(channel("B", "url-b.m3u8"))

        val result = mergeChannelsWithFallback(fallback, api)

        assertEquals(2, result.size)
    }

    // ── edge cases ──────────────────────────────────────────────────────────

    @Test
    fun `empty API returns only fallback channels`() {
        val fallback = listOf(channel("La 1", "rtve1.m3u8"), channel("La 2", "rtve2.m3u8"))

        val result = mergeChannelsWithFallback(fallback, emptyList())

        assertEquals(fallback, result)
    }

    @Test
    fun `empty fallback returns only API channels`() {
        val api = listOf(channel("Antena 3", "antena3.m3u8"))

        val result = mergeChannelsWithFallback(emptyList(), api)

        assertEquals(api, result)
    }

    @Test
    fun `both empty returns empty list`() {
        val result = mergeChannelsWithFallback(emptyList(), emptyList())

        assertEquals(emptyList<Channel>(), result)
    }
}
