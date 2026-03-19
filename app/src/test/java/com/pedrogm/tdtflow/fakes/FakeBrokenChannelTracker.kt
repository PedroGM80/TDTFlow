package com.pedrogm.tdtflow.fakes

import com.pedrogm.tdtflow.domain.tracker.BrokenChannelTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeBrokenChannelTracker : BrokenChannelTracker {
    private val _brokenUrls = MutableStateFlow<Set<String>>(emptySet())
    override val brokenUrls: StateFlow<Set<String>> = _brokenUrls.asStateFlow()

    override fun markAsBroken(url: String) { _brokenUrls.update { it + url } }
    override fun unmarkAsBroken(url: String) { _brokenUrls.update { it - url } }
    override fun clearAll() { _brokenUrls.value = emptySet() }
}
