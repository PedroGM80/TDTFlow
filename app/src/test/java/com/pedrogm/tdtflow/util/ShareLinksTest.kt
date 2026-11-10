package com.pedrogm.tdtflow.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ShareLinksTest {

    @Test
    fun `channelUrl encodes spaces and accents`() {
        assertEquals(
            "https://pedrogm80.github.io/TDTFlow/canal.html?c=Canal%20Sur%20Andaluc%C3%ADa",
            ShareLinks.channelUrl("Canal Sur Andalucía")
        )
    }

    @Test
    fun `channelUrl encodes reserved characters`() {
        assertEquals(
            "https://pedrogm80.github.io/TDTFlow/canal.html?c=A%26B%3F",
            ShareLinks.channelUrl("A&B?")
        )
    }
}
