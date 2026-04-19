package com.pedrogm.tdtflow.data.repository

import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory

private const val RTVE_ZTNR = "https://ztnr.rtve.es/ztnr"
private const val RTVE_LIVE = "https://rtvelivestream.rtve.es/rtvesec"

private fun ztnr(id: String) = "$RTVE_ZTNR/$id.m3u8"
private fun dvr(name: String) = "$RTVE_LIVE/$name/${name}_main_dvr.m3u8"
private fun rne(name: String) = "$RTVE_LIVE/rne/${name}_main.m3u8"
private fun fbLogo(id: String) = "https://graph.facebook.com/$id/picture?width=200&height=200"
private fun twLogo(id: String) = "https://pbs.twimg.com/profile_images/$id"

private class ChannelBuilder {

    private val channels = mutableListOf<Channel>()

    fun channel(
        name: String,
        url: String,
        category: ChannelCategory,
        logo: String = "",
        isRadio: Boolean = false
    ) {
        channels += Channel(
            name = name,
            url = url,
            logo = logo,
            category = category,
            isRadio = isRadio
        )
    }

    fun build(): List<Channel> = channels
}

private fun createChannels(block: ChannelBuilder.() -> Unit): List<Channel> =
    ChannelBuilder().apply(block).build()


/**
 * Provides hardcoded fallback channels organized by type:
 * - TV channels (RTVE, Regional)
 * - Radio Musical (30+ stations)
 * - Radio General (5 stations)
 */
internal fun fallbackChannels(): List<Channel> =
    tvChannels() + radioNewsChannels() + radioSportsChannels() + radioRegionalChannels() + radioMusicChannels()

/**
 * TV channels: RTVE and autonomous communities
 */
private fun tvChannels(): List<Channel> = createChannels {

    // ═══════════════════════════════════════════════════════════════════
    // TELEVISIÓN
    // ═══════════════════════════════════════════════════════════════════

    // RTVE
    channel(
        "La 1",
        ztnr("1688877"),
        ChannelCategory.GENERAL,
        twLogo("2008842210414915584/zIp_go25_200x200.jpg")
    )
    channel(
        "La 2",
        ztnr("1688885"),
        ChannelCategory.GENERAL,
        "https://yt3.googleusercontent.com/ytc/AIdro_kqgHWySi5xprs1VFCNCX0IKNT8yXBLZC43JMoB8j0JUto=s200"
    )
    channel(
        "24 Horas",
        ztnr("1694255"),
        ChannelCategory.NEWS,
        twLogo("1634293543987453954/mb1Rzmso_200x200.jpg")
    )
    channel(
        "Clan",
        dvr("clan"),
        ChannelCategory.KIDS,
        fbLogo("clantve")
    )
    channel(
        "Teledeporte",
        ztnr("1712295"),
        ChannelCategory.SPORTS,
        fbLogo("teledeporteRTVE")
    )

    // Entretenimiento
    channel(
        "TRECE",
        "https://vcp.api.video/13tv/13tv.m3u8",
        ChannelCategory.ENTERTAINMENT,
        fbLogo("TRECE.es")
    )

    // Autonómicos
    channel(
        "Canal Sur 2",
        "https://cdnlive.codev8.net/rtvalive/smil:channel22.smil/playlist.m3u8",
        ChannelCategory.REGIONAL,
        fbLogo("canalsurradioytv")
    )
    channel(
        "TV3 Cataluña",
        "https://directes-tv-cat.3catdirectes.cat/live-content/tv3-hls/master.m3u8",
        ChannelCategory.REGIONAL,
        fbLogo("tv3cat")
    )
    channel(
        "ETB 1",
        "https://multimedia.eitb.eus/live-content/etb1hd-hls/master.m3u8",
        ChannelCategory.REGIONAL,
        fbLogo("eitb")
    )
    channel(
        "Aragón TV",
        "https://cartv-streaming.aranova.es/hls/live/aragontv_canal1.m3u8",
        ChannelCategory.REGIONAL,
        fbLogo("aragontelevision")
    )
}

/**
 * Radio news and general interest
 */
private fun radioNewsChannels(): List<Channel> = createChannels {
    channel(
        "Cadena SER",
        "https://playerservices.streamtheworld.com/api/livestream-redirect/CADENASER.mp3",
        ChannelCategory.NEWS,
        fbLogo("cadenaser"),
        isRadio = true
    )
    channel(
        "COPE",
        "https://flucast09-h-cloud.flumotion.com/cope/net1.mp3",
        ChannelCategory.NEWS,
        fbLogo("COPE"),
        isRadio = true
    )
    channel(
        "Onda Cero",
        "https://radio-atres-live.ondacero.es/api/livestream-redirect/OCAAC.aac",
        ChannelCategory.NEWS,
        fbLogo("ondacero"),
        isRadio = true
    )
    channel(
        "Radio Nacional",
        rne("rne_r1"),
        ChannelCategory.NEWS,
        fbLogo("radionacionalrne"),
        isRadio = true
    )
    channel(
        "esRadio",
        "https://libertaddigital-radio-live1.flumotion.com/libertaddigital/ld-live1-high.mp3",
        ChannelCategory.NEWS,
        fbLogo("esradio"),
        isRadio = true
    )
}

/**
 * Radio sports channels
 */
private fun radioSportsChannels(): List<Channel> = createChannels {
    channel(
        "Radio Marca",
        "https://radiomarca.unity-streaming.com/radiomarca.mp3",
        ChannelCategory.SPORTS,
        fbLogo("radiomarca"),
        isRadio = true
    )
}

/**
 * Regional radio stations
 */
private fun radioRegionalChannels(): List<Channel> = createChannels {
    channel(
        "Canal Sur Radio",
        "https://rtva-live-radio.flumotion.com/rtva/csr.mp3",
        ChannelCategory.REGIONAL,
        fbLogo("canalsurradioytv"),
        isRadio = true
    )
    channel(
        "Canal Fiesta Radio",
        "https://rtva-live-radio.flumotion.com/rtva/cfr.mp3",
        ChannelCategory.REGIONAL,
        twLogo("2014607361843793920/uKfZnuel_200x200.jpg"),
        isRadio = true
    )
    channel(
        "Flamenco Radio",
        "https://rtva-live-radio.flumotion.com/rtva/flamenco.mp3",
        ChannelCategory.REGIONAL,
        fbLogo("FlamencoRadio"),
        isRadio = true
    )
    channel(
        "Catalunya Ràdio",
        "https://directes-radio-cat.3catdirectes.cat/live-content/catradio-hls/master.m3u8",
        ChannelCategory.REGIONAL,
        fbLogo("catalunyaradio"),
        isRadio = true
    )
    channel(
        "Flaix FM",
        "https://stream.flaixfm.cat/icecast",
        ChannelCategory.REGIONAL,
        twLogo("1051761197127745541/whMnn4_K_200x200.jpg"),
        isRadio = true
    )
    channel(
        "RAC 1",
        "https://playerservices.streamtheworld.com/api/livestream-redirect/RAC1.mp3",
        ChannelCategory.REGIONAL,
        fbLogo("rac1oficial"),
        isRadio = true
    )
}

/**
 * Purely musical radio channels
 */
private fun radioMusicChannels(): List<Channel> = createChannels {

    // ═══════════════════════════════════════════════════════════════════
    // RADIO MUSICAL - Fuente: TDTChannels radio.json
    // ═══════════════════════════════════════════════════════════════════

    // LOS40 y variantes
    channel(
        "LOS40",
        "https://playerservices.streamtheworld.com/api/livestream-redirect/Los40.mp3",
        ChannelCategory.MUSIC,
        fbLogo("los40"),
        isRadio = true
    )
    channel(
        "LOS40 Classic",
        "https://playerservices.streamtheworld.com/api/livestream-redirect/LOS40_CLASSIC.mp3",
        ChannelCategory.MUSIC,
        fbLogo("Los40Classic.Oficial"),
        isRadio = true
    )
    channel(
        "LOS40 Urban",
        "https://playerservices.streamtheworld.com/api/livestream-redirect/LOS40_URBAN.mp3",
        ChannelCategory.MUSIC,
        fbLogo("los40urban"),
        isRadio = true
    )
    channel(
        "LOS40 Dance",
        "https://playerservices.streamtheworld.com/api/livestream-redirect/LOS40_DANCE.mp3",
        ChannelCategory.MUSIC,
        fbLogo("los40dance"),
        isRadio = true
    )

    // Cadena Dial y variantes
    channel(
        "Cadena Dial",
        "https://playerservices.streamtheworld.com/api/livestream-redirect/CADENADIAL.mp3",
        ChannelCategory.MUSIC,
        fbLogo("cadenadial"),
        isRadio = true
    )
    channel(
        "Dial Baladas",
        "https://playerservices.streamtheworld.com/api/livestream-redirect/CADENADIAL_03.mp3",
        ChannelCategory.MUSIC,
        "https://recursosweb.prisaradio.com/fotos/dest/010002743853.jpg",
        isRadio = true
    )
    channel(
        "Dial Latino",
        "https://playerservices.streamtheworld.com/api/livestream-redirect/CADENADIAL_02.mp3",
        ChannelCategory.MUSIC,
        "https://recursosweb.prisaradio.com/fotos/dest/010002743851.jpg",
        isRadio = true
    )

    // COPE musicales
    channel(
        "Cadena 100",
        "https://cadena100-cope.flumotion.com/chunks.m3u8",
        ChannelCategory.MUSIC,
        fbLogo("CADENA100"),
        isRadio = true
    )
    channel(
        "Rock FM",
        "https://rockfm-cope.flumotion.com/playlist.m3u8",
        ChannelCategory.MUSIC,
        fbLogo("RockFM"),
        isRadio = true
    )
    channel(
        "MegaStar FM",
        "https://megastar-cope.flumotion.com/playlist.m3u8",
        ChannelCategory.MUSIC,
        fbLogo("MegaStarFM"),
        isRadio = true
    )

    // Atresmedia musicales
    channel(
        "Europa FM",
        "https://radio-atres-live.ondacero.es/api/livestream-redirect/EFMAAC.aac",
        ChannelCategory.MUSIC,
        fbLogo("tueuropafm"),
        isRadio = true
    )
    channel(
        "Melodía FM",
        "https://radio-atres-live.ondacero.es/api/livestream-redirect/MELODIA_FMAAC.aac",
        ChannelCategory.MUSIC,
        fbLogo("tumelodiafm"),
        isRadio = true
    )

    // RTVE Radio
    channel(
        "Radio 3 RNE",
        rne("rne_r3"),
        ChannelCategory.MUSIC,
        fbLogo("radio3"),
        isRadio = true
    )
    channel(
        "Radio Clásica RNE",
        rne("rne_r2"),
        ChannelCategory.MUSIC,
        fbLogo("radioclasicartve"),
        isRadio = true
    )

    // Kiss FM
    channel(
        "Kiss FM",
        "https://kissfm.kissfmradio.cires21.com/kissfm.mp3",
        ChannelCategory.MUSIC,
        fbLogo("kissfm.es"),
        isRadio = true
    )
    channel(
        "Hit FM",
        "https://bbhitfm.kissfmradio.cires21.com/bbhitfm.mp3",
        ChannelCategory.MUSIC,
        fbLogo("hitfm.es"),
        isRadio = true
    )

    // Radiolé
    channel(
        "Radiolé",
        "https://playerservices.streamtheworld.com/api/livestream-redirect/RADIOLE.mp3",
        ChannelCategory.MUSIC,
        fbLogo("radiole"),
        isRadio = true
    )

    // Loca FM y variantes
    channel(
        "Loca FM",
        "https://s3.we4stream.com:2020/stream/locafm",
        ChannelCategory.MUSIC,
        fbLogo("LocaFmOficial"),
        isRadio = true
    )
    channel(
        "Loca FM Dance",
        "https://s2.we4stream.com/listen/loca_dance/live",
        ChannelCategory.MUSIC,
        fbLogo("LocaFmOficial"),
        isRadio = true
    )
    channel(
        "Loca FM Remember",
        "https://s2.we4stream.com/listen/loca_remember/live",
        ChannelCategory.MUSIC,
        fbLogo("LocaFmOficial"),
        isRadio = true
    )
    channel(
        "Loca FM House",
        "https://s2.we4stream.com/listen/loca_house/live",
        ChannelCategory.MUSIC,
        fbLogo("LocaFmOficial"),
        isRadio = true
    )
    channel(
        "Loca FM Techno",
        "https://s2.we4stream.com/listen/loca_techo/live",
        ChannelCategory.MUSIC,
        fbLogo("LocaFmOficial"),
        isRadio = true
    )
    channel(
        "Loca FM Chill Out",
        "https://s2.we4stream.com/listen/loca_chill_out/live",
        ChannelCategory.MUSIC,
        fbLogo("LocaFmOficial"),
        isRadio = true
    )
    channel(
        "Loca FM 80s",
        "https://s2.we4stream.com/listen/loca_80s/live",
        ChannelCategory.MUSIC,
        fbLogo("LocaFmOficial"),
        isRadio = true
    )
    channel(
        "Loca FM 90s",
        "https://s2.we4stream.com/listen/loca_90s_/live",
        ChannelCategory.MUSIC,
        fbLogo("LocaFmOficial"),
        isRadio = true
    )

    // MDT Radio
    channel(
        "MDT Radio Remember",
        "https://streams1.mdtradio.com:8443/mdtweb",
        ChannelCategory.MUSIC,
        fbLogo("mdtradio"),
        isRadio = true
    )
    channel(
        "MDT Radio 80s",
        "https://streams1.mdtradio.com:8443/MDTradio80",
        ChannelCategory.MUSIC,
        fbLogo("mdtradio"),
        isRadio = true
    )
    channel(
        "MDT Radio 90s",
        "https://streams1.mdtradio.com:8443/MDTradio90",
        ChannelCategory.MUSIC,
        fbLogo("mdtradio"),
        isRadio = true
    )
    channel(
        "MDT Radio 2000",
        "https://streams1.mdtradio.com:8443/MDTradio2000",
        ChannelCategory.MUSIC,
        fbLogo("mdtradio"),
        isRadio = true
    )

    // Élite Radio variantes
    channel(
        "Élite Love",
        "https://streaming2.elitecomunicacion.es/proxy/elitelove/stream",
        ChannelCategory.MUSIC,
        fbLogo("cadena.elitegranada"),
        isRadio = true
    )
    channel(
        "Élite Dance",
        "https://streaming2.elitecomunicacion.es/proxy/elitedance/stream",
        ChannelCategory.MUSIC,
        fbLogo("cadena.elitegranada"),
        isRadio = true
    )
    channel(
        "Élite Oldies",
        "https://streaming2.elitecomunicacion.es/proxy/eliteoldies/stream",
        ChannelCategory.MUSIC,
        fbLogo("cadena.elitegranada"),
        isRadio = true
    )

    // Cataluña
    channel(
        "Flaix FM",
        "https://stream.flaixfm.cat/icecast",
        ChannelCategory.MUSIC,
        twLogo("1051761197127745541/whMnn4_K_200x200.jpg"),
        isRadio = true
    )
    channel(
        "Flaixbac",
        "https://stream.flaixbac.cat/icecast",
        ChannelCategory.MUSIC,
        twLogo("1164926188307001344/PtDeZDOO_200x200.jpg"),
        isRadio = true
    )
    channel(
        "RAC 105",
        "https://playerservices.streamtheworld.com/api/livestream-redirect/RAC105.mp3",
        ChannelCategory.MUSIC,
        fbLogo("rac105"),
        isRadio = true
    )

    // Andalucía
    channel(
        "Flamenco Radio",
        "https://rtva-live-radio.flumotion.com/rtva/flamenco.mp3",
        ChannelCategory.MUSIC,
        fbLogo("FlamencoRadio"),
        isRadio = true
    )
    channel(
        "Canal Fiesta Radio",
        "https://rtva-live-radio.flumotion.com/rtva/cfr.mp3",
        ChannelCategory.MUSIC,
        twLogo("2014607361843793920/uKfZnuel_200x200.jpg"),
        isRadio = true
    )

    // Otras
    channel(
        "MariskalRock Radio",
        "https://media.profesionalhosting.com:8047/stream",
        ChannelCategory.OTHER,
        fbLogo("mariskalrock"),
        isRadio = true
    )
    channel(
        "La Urban Radio",
        "https://st1.urbanrevolution.es:8443/laurbanfm.mp3",
        ChannelCategory.OTHER,
        fbLogo("urbanrevolution.es"),
        isRadio = true
    )
    channel(
        "digitalHits FM",
        "https://dhits.frilab.com:8443/dhits",
        ChannelCategory.OTHER,
        fbLogo("digitalhits"),
        isRadio = true
    )
}

/**
 * Radio general channels: National news and general programming
 */
private fun radioGeneralChannels(): List<Channel> = createChannels {

    // ═══════════════════════════════════════════════════════════════════
    // RADIO GENERALISTA
    // ═══════════════════════════════════════════════════════════════════

    channel(
        "Cadena SER",
        "https://playerservices.streamtheworld.com/api/livestream-redirect/CADENASER.mp3",
        ChannelCategory.NEWS,
        fbLogo("cadenaser"),
        isRadio = true
    )
    channel(
        "COPE",
        "https://flucast09-h-cloud.flumotion.com/cope/net1.mp3",
        ChannelCategory.NEWS,
        fbLogo("COPE"),
        isRadio = true
    )
    channel(
        "Onda Cero",
        "https://radio-atres-live.ondacero.es/api/livestream-redirect/OCAAC.aac",
        ChannelCategory.NEWS,
        fbLogo("ondacero"),
        isRadio = true
    )
    channel(
        "Radio Nacional",
        rne("rne_r1"),
        ChannelCategory.NEWS,
        fbLogo("radionacionalrne"),
        isRadio = true
    )
    channel(
        "esRadio",
        "https://libertaddigital-radio-live1.flumotion.com/libertaddigital/ld-live1-high.mp3",
        ChannelCategory.NEWS,
        fbLogo("esradio"),
        isRadio = true
    )
}
