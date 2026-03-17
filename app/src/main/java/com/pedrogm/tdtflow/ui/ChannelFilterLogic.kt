package com.pedrogm.tdtflow.ui

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory

/**
 * Encapsulates channel filtering logic to keep TdtViewModel lean.
 *
 * Applies a pipeline of filters:
 * 1. Valid URLs only
 * 2. Exclude broken channels (unless explicitly showing)
 * 3. Category filtering (with special "Todos" handling excluding MUSIC)
 * 4. Search query matching
 */
object ChannelFilterLogic {

    fun applyFilters(
        channels: List<Channel>,
        category: ChannelCategory?,
        query: String,
        brokenUrls: Set<String>,
        showBroken: Boolean
    ): List<Channel> =
        channels
            .asSequence()
            .filterValidUrls()
            .filterBrokenChannels(brokenUrls, showBroken)
            .filterByCategory(category)
            .filterByQuery(query)
            .toList()

    private fun Sequence<Channel>.filterValidUrls(): Sequence<Channel> =
        filter { it.url.isNotBlank() }

    private fun Sequence<Channel>.filterBrokenChannels(
        brokenUrls: Set<String>,
        showBroken: Boolean
    ): Sequence<Channel> =
        filter { showBroken || it.url !in brokenUrls }

    private fun Sequence<Channel>.filterByCategory(
        category: ChannelCategory?
    ): Sequence<Channel> =
        filter { channel ->
            if (category == null) {
                // "Todos" hides MUSIC channels (audio-only)
                channel.category != ChannelCategory.MUSIC
            } else {
                channel.category == category
            }
        }

    private fun Sequence<Channel>.filterByQuery(query: String): Sequence<Channel> =
        filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
}
