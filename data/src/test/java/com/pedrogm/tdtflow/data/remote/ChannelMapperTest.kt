package com.pedrogm.tdtflow.data.remote

import com.pedrogm.tdtflow.domain.model.ChannelCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChannelMapperTest {

    private fun option(format: String, url: String) = TdtOption(
        format = format,
        url = url,
        geo = null,
        resolution = null,
        language = null
    )

    private fun tdtChannel(
        name: String = "Test Channel",
        logo: String = "https://logo.png",
        epgId: String? = "test.epg",
        options: List<TdtOption> = listOf(option("m3u8", "https://stream.m3u8"))
    ) = TdtChannel(
        name = name,
        web = null,
        logo = logo,
        epgId = epgId,
        options = options,
        extraInfo = null
    )

    // ── toChannel: null when options empty ──────────────────────────────────

    @Test
    fun `toChannel returns null when options is empty`() {
        val channel = tdtChannel(options = emptyList())

        assertNull(channel.toChannel("Generalistas"))
    }

    // ── toChannel: format priority ──────────────────────────────────────────

    @Test
    fun `toChannel picks m3u8 over aac`() {
        val channel = tdtChannel(
            options = listOf(
                option("aac", "https://audio.aac"),
                option("m3u8", "https://stream.m3u8")
            )
        )

        val result = channel.toChannel("Generalistas")!!
        assertEquals("https://stream.m3u8", result.url)
    }

    @Test
    fun `toChannel picks aac when no m3u8`() {
        val channel = tdtChannel(
            options = listOf(
                option("mp3", "https://audio.mp3"),
                option("aac", "https://audio.aac")
            )
        )

        val result = channel.toChannel("Generalistas")!!
        assertEquals("https://audio.aac", result.url)
    }

    @Test
    fun `toChannel picks mp3 when no m3u8 or aac`() {
        val channel = tdtChannel(
            options = listOf(
                option("stream", "https://live.stream"),
                option("mp3", "https://audio.mp3")
            )
        )

        val result = channel.toChannel("Generalistas")!!
        assertEquals("https://audio.mp3", result.url)
    }

    @Test
    fun `toChannel picks stream when no other formats`() {
        val channel = tdtChannel(
            options = listOf(option("stream", "https://live.stream"))
        )

        val result = channel.toChannel("Generalistas")!!
        assertEquals("https://live.stream", result.url)
    }

    @Test
    fun `toChannel returns null when no supported format`() {
        val channel = tdtChannel(
            options = listOf(option("unsupported", "https://something"))
        )

        assertNull(channel.toChannel("Generalistas"))
    }

    // ── toChannel: field mapping ────────────────────────────────────────────

    @Test
    fun `toChannel maps name correctly`() {
        val channel = tdtChannel(name = "Antena 3")

        val result = channel.toChannel("Generalistas")!!
        assertEquals("Antena 3", result.name)
    }

    @Test
    fun `toChannel maps logo correctly`() {
        val channel = tdtChannel(logo = "https://cdn.logo/antena3.png")

        val result = channel.toChannel("Generalistas")!!
        assertEquals("https://cdn.logo/antena3.png", result.logo)
    }

    @Test
    fun `toChannel maps epgId correctly`() {
        val channel = tdtChannel(epgId = "antena3.epg")

        val result = channel.toChannel("Generalistas")!!
        assertEquals("antena3.epg", result.epgId)
    }

    @Test
    fun `toChannel maps null epgId to empty string`() {
        val channel = tdtChannel(epgId = null)

        val result = channel.toChannel("Generalistas")!!
        assertEquals("", result.epgId)
    }

    // ── mapAmbitToCategory (tested via toChannel) ───────────────────────────

    @Test
    fun `Generalistas maps to GENERAL`() {
        val result = tdtChannel().toChannel("Generalistas")!!
        assertEquals(ChannelCategory.GENERAL, result.category)
    }

    @Test
    fun `Informativos maps to NEWS`() {
        val result = tdtChannel().toChannel("Informativos")!!
        assertEquals(ChannelCategory.NEWS, result.category)
    }

    @Test
    fun `Deportivos maps to SPORTS`() {
        val result = tdtChannel().toChannel("Deportivos")!!
        assertEquals(ChannelCategory.SPORTS, result.category)
    }

    @Test
    fun `Infantiles maps to KIDS`() {
        val result = tdtChannel().toChannel("Infantiles")!!
        assertEquals(ChannelCategory.KIDS, result.category)
    }

    @Test
    fun `Eventuales maps to ENTERTAINMENT`() {
        val result = tdtChannel().toChannel("Eventuales")!!
        assertEquals(ChannelCategory.ENTERTAINMENT, result.category)
    }

    @Test
    fun `Streaming maps to ENTERTAINMENT`() {
        val result = tdtChannel().toChannel("Streaming")!!
        assertEquals(ChannelCategory.ENTERTAINMENT, result.category)
    }

    @Test
    fun `Musicales maps to MUSIC`() {
        val result = tdtChannel().toChannel("Musicales")!!
        assertEquals(ChannelCategory.MUSIC, result.category)
    }

    @Test
    fun `Populares maps to MUSIC`() {
        val result = tdtChannel().toChannel("Populares")!!
        assertEquals(ChannelCategory.MUSIC, result.category)
    }

    @Test
    fun `Madrid maps to REGIONAL`() {
        val result = tdtChannel().toChannel("Madrid")!!
        assertEquals(ChannelCategory.REGIONAL, result.category)
    }

    @Test
    fun `Cataluna maps to REGIONAL`() {
        val result = tdtChannel().toChannel("Cataluña")!!
        assertEquals(ChannelCategory.REGIONAL, result.category)
    }

    @Test
    fun `Andalucia maps to REGIONAL`() {
        val result = tdtChannel().toChannel("Andalucía")!!
        assertEquals(ChannelCategory.REGIONAL, result.category)
    }

    @Test
    fun `unknown ambit maps to OTHER`() {
        val result = tdtChannel().toChannel("SomethingUnknown")!!
        assertEquals(ChannelCategory.OTHER, result.category)
    }

    @Test
    fun `ambit mapping is case-insensitive`() {
        val result = tdtChannel().toChannel("generalistas")!!
        assertEquals(ChannelCategory.GENERAL, result.category)
    }
}
