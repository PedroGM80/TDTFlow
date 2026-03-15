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


    channel(
        "Canal Sur 2",
        "https://cdnlive.codev8.net/rtvalive/smil:channel22.smil/playlist.m3u8",
        ChannelCategory.GENERAL,
        fbLogo("canalsurradioytv")
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


    // Aragón
    channel(
        "Aragón TV",
        "https://cartv.streaming.aranova.es/hls/live/aragontv_canal1.m3u8",
        ChannelCategory.GENERAL,
        fbLogo("aragontelevision")
    )

    // RADIO / MÚSICA - Servidores estables 2025
    channel("LOS40", "https://playerservices.streamtheworld.com/api/livestream-redirect/LOS40.mp3", ChannelCategory.MUSIC, fbLogo("los40"))
    channel("Rock FM", "https://rockfm-cope-rrcast.flumotion.com/cope/net1.mp3", ChannelCategory.MUSIC, fbLogo("rockfm"))
    channel("Cadena SER", "https://playerservices.streamtheworld.com/api/livestream-redirect/CADENASER.mp3", ChannelCategory.MUSIC, fbLogo("cadenaser"))
    channel("Kiss FM", "https://kissfm.kissfmradio.cires21.com/kissfm.mp3", ChannelCategory.MUSIC, fbLogo("KissFMSpain"))
}
