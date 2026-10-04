package com.pedrogm.tdtflow.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RestrictedChannelsTest {

    @Test
    fun `matches restricted channels ignoring case and accents`() {
        listOf("Antena 3", "antena3", "LA SEXTA", "telecinco", "Tele 5", "cuatro", "Energy", "Neox", "boing")
            .forEach { assertTrue("Expected match for '$it'", RestrictedChannels.matches(it)) }
    }

    @Test
    fun `matches partial searches of at least three characters`() {
        assertTrue(RestrictedChannels.matches("antena"))
        assertTrue(RestrictedChannels.matches("telec"))
    }

    @Test
    fun `does not match free-to-air channels or very short queries`() {
        listOf("La 1", "RTVE", "Clan", "TV3", "Canal Sur", "a", "").forEach {
            assertFalse("Unexpected match for '$it'", RestrictedChannels.matches(it))
        }
    }
}
