package com.pedrogm.tdtflow.data

import com.pedrogm.tdtflow.data.model.Channel
import com.pedrogm.tdtflow.data.model.ChannelCategory

private const val RTVE_ZTNR = "https://ztnr.rtve.es/ztnr"
private const val RTVE_LIVE = "https://rtvelivestream.rtve.es/rtvesec"

private fun ztnr(id: String) = "$RTVE_ZTNR/$id.m3u8"
private fun dvr(name: String) = "$RTVE_LIVE/$name/${name}_main_dvr.m3u8"

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
    channel("La 1", ztnr("1688877"), ChannelCategory.GENERAL, "https://pbs.twimg.com/profile_images/2008842210414915584/zIp_go25_200x200.jpg")
    channel("La 2", ztnr("1688885"), ChannelCategory.GENERAL, "https://pbs.twimg.com/profile_images/2008842527873335296/F57R-E9t_200x200.jpg")
    channel("24 Horas", ztnr("1694255"), ChannelCategory.NEWS, "https://pbs.twimg.com/profile_images/1699066668742213632/H-R-6-U6_200x200.jpg")
    channel("Clan", dvr("clan"), ChannelCategory.KIDS, "https://pbs.twimg.com/profile_images/1420719815048474626/UuWv3fT7_200x200.png")
    channel("Teledeporte", dvr("tdp"), ChannelCategory.SPORTS, "https://pbs.twimg.com/profile_images/1699067342611681280/Dk7J9R3f_200x200.jpg")

    // Nacionales libres
    channel("Trece", "https://trecetv.vnet.es/index.m3u8", ChannelCategory.GENERAL, "https://pbs.twimg.com/profile_images/1447814282385387521/v7lVv6C6_200x200.jpg")
    channel("Ten", "https://ten.vnet.es/index.m3u8", ChannelCategory.GENERAL, "https://pbs.twimg.com/profile_images/1151433282216501248/9_Qf9-K0_200x200.png")
    channel("Real Madrid TV", "https://realmadrid-3-es.akamaized.net/master.m3u8", ChannelCategory.SPORTS, "https://graph.facebook.com/RealMadrid/picture?width=200&height=200")

    // Andalucía
    channel(
        "Canal Sur Andalucía",
        "https://d35x6iaiw8f75z.cloudfront.net/v1/master/3722c60a815c199d9c0ef36c5b73da68a62b09d1/cc-kbwsl0jk1bvoo/canal_sur_andalucia_es.m3u8",
        ChannelCategory.GENERAL,
        "https://pbs.twimg.com/profile_images/1676878954785816576/RkU_1_1V_200x200.jpg"
    )

    // Madrid
    channel(
        "Telemadrid",
        "https://telemadridhls2-live-hls.secure2.footprint.net/egress/chandler/telemadrid/telemadrid/index.m3u8",
        ChannelCategory.GENERAL,
        "https://pbs.twimg.com/profile_images/1447820257211158528/v9_U6y9S_200x200.jpg"
    )

    // Cataluña
    channel(
        "TV3 Cataluña",
        "https://directes-tv-cat.3catdirectes.cat/live-content/tv3-hls/master.m3u8",
        ChannelCategory.GENERAL,
        "https://pbs.twimg.com/profile_images/1706642784742662144/f9R9E_K7_200x200.jpg"
    )

    // País Vasco
    channel(
        "ETB 1",
        "https://multimedia.eitb.eus/live-content/etb1hd-hls/master.m3u8",
        ChannelCategory.GENERAL,
        "https://pbs.twimg.com/profile_images/1447816045143851010/8yK_M6zU_200x200.jpg"
    )

    // Galicia
    channel(
        "TVG Galicia",
        "https://06-03.streaming.crtvg.es/live/tvg_europa/index.m3u8",
        ChannelCategory.GENERAL,
        "https://pbs.twimg.com/profile_images/1447819129753767936/FfH_KkKk_200x200.jpg"
    )

    // Aragón
    channel(
        "Aragón TV",
        "https://cartv-streaming.aranova.es/hls/live/aragontv_canal1.m3u8",
        ChannelCategory.GENERAL,
        "https://pbs.twimg.com/profile_images/1447812984180412416/mXp_m6m6_200x200.jpg"
    )

    // Baleares
    channel(
        "IB3 Baleares",
        "http://ibsatiphone.ib3tv.com/iphoneliveIB3/IB3/bitrate_3.m3u8",
        ChannelCategory.GENERAL,
        "https://pbs.twimg.com/profile_images/1447814674754564101/Y_U9U_U9_200x200.jpg"
    )
}
