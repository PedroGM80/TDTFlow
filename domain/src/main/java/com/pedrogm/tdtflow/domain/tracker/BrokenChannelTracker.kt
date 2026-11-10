package com.pedrogm.tdtflow.domain.tracker

import kotlinx.coroutines.flow.StateFlow

interface BrokenChannelTracker {
    val brokenUrls: StateFlow<Set<String>>
    fun markAsBroken(url: String)
    fun unmarkAsBroken(url: String)
    fun clearAll()

    /** Vacía la lista si ha pasado el intervalo de limpieza automática desde la última vez. */
    fun clearIfExpired()
}
