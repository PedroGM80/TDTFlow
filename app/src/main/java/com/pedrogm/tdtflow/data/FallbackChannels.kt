package com.pedrogm.tdtflow.data

import com.pedrogm.tdtflow.data.model.Channel
import com.pedrogm.tdtflow.data.model.ChannelCategory

private const val RTVE_ZTNR = "https://ztnr.rtve.es/ztnr"
private const val RTVE_LIVE = "https://rtvelivestream.rtve.es/rtvesec"

private fun ztnr(id: String) = "$RTVE_ZTNR/$id.m3u8"
private fun dvr(name: String) = "$RTVE_LIVE/$name/${name}_main_dvr.m3u8"
private fun fbLogo(id: String) = "https://graph.facebook.com/$id/picture?width=200&height=200"

private class ChannelBuilder {

    private val channels = mutableListOf<Channel>()

    fun channel(
        name: String,
        url: String,
        category: ChannelCategory,
        logo: String = ""
    ) {
        channels += Channel(
            name = name,
            url = url,
            logo = logo,
            category = category
        )
    }

    fun build(): List<Channel> = channels
}

private fun createChannels(block: ChannelBuilder.() -> Unit): List<Channel> =
    ChannelBuilder().apply(block).build()


internal fun fallbackChannels(): List<Channel> = createChannels {

    // RTVE
    channel("La 1", ztnr("1688877"), ChannelCategory.GENERAL, fbLogo("la1detve"))
    channel("La 2", ztnr("1688885"), ChannelCategory.GENERAL, fbLogo("la2detve"))
    channel("24 Horas", ztnr("1694255"), ChannelCategory.NEWS, fbLogo("24h_tve"))
    channel("Clan", dvr("clan"), ChannelCategory.KIDS, fbLogo("clantve"))
    channel("Teledeporte", dvr("tdp"), ChannelCategory.SPORTS, fbLogo("teledeporteRTVE"))

    // Nacionales libres
    channel("Trece", "http://176.65.146.237:8401/play/a0a9/index.m3u8", ChannelCategory.GENERAL, fbLogo("TRECEtves"))
    channel("Ten", "https://unlimited1-us.dps.live/ten/ten.smil/playlist.m3u8", ChannelCategory.GENERAL, fbLogo("TenTV"))
    channel("Real Madrid TV", "https://rmtv.akamaized.net/hls/live/2043153/rmtv-es-web/master.m3u8", ChannelCategory.SPORTS, fbLogo("RealMadrid"))

    // Andalucía
    channel(
        "Canal Sur Andalucía",
        "https://d35x6iaiw8f75z.cloudfront.net/v1/master/3722c60a815c199d9c0ef36c5b73da68a62b09d1/cc-kbwsl0jk1bvoo/canal_sur_andalucia_es.m3u8",
        ChannelCategory.GENERAL,
        fbLogo("canalsurradioytv")
    )
    channel(
        "Canal Sur 2",
        "https://cdnlive.codev8.net/rtvalive/smil:channel22.smil/playlist.m3u8",
        ChannelCategory.GENERAL,
        fbLogo("canalsurradioytv")
    )

    // Madrid
    channel(
        "Telemadrid",
        "https://telemadrid-23-secure2.akamaized.net/master.m3u8",
        ChannelCategory.GENERAL,
        fbLogo("telemadrid")
    )

    // Cataluña
    channel(
        "TV3 Cataluña",
        "https://directes-tv-cat.3catdirectes.cat/live-content/tv3-hls/master.m3u8",
        ChannelCategory.GENERAL,
        fbLogo("tv3")
    )

    // País Vasco
    channel(
        "ETB 1",
        "https://multimedia.eitb.eus/live-content/etb1hd-hls/master.m3u8",
        ChannelCategory.GENERAL,
        fbLogo("eitb")
    )

    // Galicia
    channel(
        "TVG Galicia",
        "https://crtvg-galicia-schlive.flumotion.cloud/crtvglive/smil:channel1PRG.smil/playlist.m3u8",
        ChannelCategory.GENERAL,
        fbLogo("TelevisionGalicia")
    )

    // Aragón
    channel(
        "Aragón TV",
        "https://cartv.streaming.aranova.es/hls/live/aragontv_canal1.m3u8",
        ChannelCategory.GENERAL,
        fbLogo("aragontelevision")
    )

    // Baleares
    channel(
        "IB3 Baleares",
        "http://ibsatiphone.ib3tv.com/iphoneliveIB3/IB3/bitrate_2.m3u8",
        ChannelCategory.GENERAL,
        fbLogo("IB3TV")
    )

    // RADIO / MÚSICA
    channel("LOS40", "https://live.los40.com/stream/LOS40/48_aac/index.m3u8", ChannelCategory.MUSIC, fbLogo("los40"))
    channel("Rock FM", "https://shoutcast.cope.es/rockfm-free.mp3", ChannelCategory.MUSIC, fbLogo("rockfm"))
    channel("Cadena SER", "https://playerservices.streamtheworld.com/api/livestream-redirect/CADENASER.mp3", ChannelCategory.MUSIC, fbLogo("cadenaser"))
    channel("Radio 3", "https://rtvelivestream.rtve.es/rtvesec/rne/rne3_main.m3u8", ChannelCategory.MUSIC, fbLogo("radio3tve"))
    channel("Kiss FM", "https://kissfm.kissfmradio.es/stream.mp3", ChannelCategory.MUSIC, fbLogo("KissFMSpain"))
}
