package com.pedrogm.tdtflow.data.repository

import com.pedrogm.tdtflow.domain.model.Channel

class ChannelCache(private val ttlMs: Long = TTL_MS) {

    companion object {
        private const val TTL_MS = 30 * 60 * 1000L // 30 minutes
    }

    private var cached: List<Channel>? = null
    private var timestamp: Long = 0L

    fun get(): List<Channel>? {
        if (System.currentTimeMillis() - timestamp > ttlMs) return null
        return cached
    }

    fun put(channels: List<Channel>) {
        cached = channels
        timestamp = System.currentTimeMillis()
    }

    fun invalidate() {
        cached = null
        timestamp = 0L
    }
}
