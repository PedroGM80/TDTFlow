package com.pedrogm.tdtflow.data.repository

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ChannelCacheTest {

    private val channel1 = Channel("La 1", "rtve1.m3u8", category = ChannelCategory.GENERAL)
    private val channel2 = Channel("La 2", "rtve2.m3u8", category = ChannelCategory.GENERAL)

    private lateinit var cache: ChannelCache

    @Before
    fun setUp() {
        cache = ChannelCache(ttlMs = 60_000L)
    }

    // ── get before put ──────────────────────────────────────────────────────

    @Test
    fun `get returns null on empty cache`() {
        assertNull(cache.get())
    }

    // ── put and get ─────────────────────────────────────────────────────────

    @Test
    fun `get returns channels after put within TTL`() {
        val channels = listOf(channel1, channel2)
        cache.put(channels)

        val result = cache.get()
        assertNotNull(result)
        assertEquals(channels, result)
    }

    @Test
    fun `put replaces previous content`() {
        cache.put(listOf(channel1))
        cache.put(listOf(channel2))

        assertEquals(listOf(channel2), cache.get())
    }

    // ── TTL expiry ──────────────────────────────────────────────────────────

    @Test
    fun `get returns null after TTL expires`() {
        // ttlMs = -1: condition (now - now = 0) > -1 is true → expired immediately
        val expiredCache = ChannelCache(ttlMs = -1L)
        expiredCache.put(listOf(channel1))

        assertNull(expiredCache.get())
    }

    @Test
    fun `get returns data when TTL has not expired`() {
        val longTtlCache = ChannelCache(ttlMs = Long.MAX_VALUE)
        longTtlCache.put(listOf(channel1))

        assertNotNull(longTtlCache.get())
    }

    // ── invalidate ──────────────────────────────────────────────────────────

    @Test
    fun `get returns null after invalidate`() {
        cache.put(listOf(channel1))
        cache.invalidate()

        assertNull(cache.get())
    }

    @Test
    fun `cache can be reused after invalidate`() {
        cache.put(listOf(channel1))
        cache.invalidate()
        cache.put(listOf(channel2))

        assertEquals(listOf(channel2), cache.get())
    }
}
