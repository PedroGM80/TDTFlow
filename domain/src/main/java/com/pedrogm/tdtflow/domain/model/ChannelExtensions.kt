package com.pedrogm.tdtflow.domain.model

/**
 * Returns the subset of channels whose URL is in [urls], preserving the
 * order defined by [urls] (i.e. the order favorites were added).
 */
fun List<Channel>.filterByUrls(urls: Set<String>): List<Channel> {
    val index = associateBy { it.url }
    return urls.mapNotNull { index[it] }
}
