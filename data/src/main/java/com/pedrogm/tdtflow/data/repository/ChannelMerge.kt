package com.pedrogm.tdtflow.data.repository

import com.pedrogm.tdtflow.domain.model.Channel

/**
 * API channels take priority over fallback.
 * Fallback only fills in channels not present in the API (by URL).
 * Logos from fallback enrich API channels that have empty logos.
 */
internal fun mergeChannelsWithFallback(
    fallback: List<Channel>,
    apiChannels: List<Channel>
): List<Channel> {
    val fallbackMap = fallback.associateBy { it.name }
    return (apiChannels + fallback)
        .distinctBy { it.url }
        .map { channel ->
            val fallbackChannel = fallbackMap[channel.name]
            if (channel.logo.isEmpty() && fallbackChannel != null && fallbackChannel.logo.isNotEmpty()) {
                channel.copy(logo = fallbackChannel.logo)
            } else {
                channel
            }
        }
}
