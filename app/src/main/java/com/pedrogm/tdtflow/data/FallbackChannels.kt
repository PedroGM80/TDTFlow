package com.pedrogm.tdtflow.data

import com.pedrogm.tdtflow.data.model.Channel
import com.pedrogm.tdtflow.data.model.ChannelCategory

internal fun fallbackChannels(): List<Channel> = listOf(
    Channel("La 1", "https://rtvelivestream.akamaized.net/rtvesec/la1/la1_main.m3u8", "https://www.tdtchannels.com/logos/tv/la1.png", ChannelCategory.GENERAL),
    Channel("La 2", "https://rtvelivestream.akamaized.net/rtvesec/la2/la2_main.m3u8", "https://www.tdtchannels.com/logos/tv/la2.png", ChannelCategory.GENERAL),
    Channel("Antena 3", "https://antena3-grp.akamaized.net/live/a3_hls/a3_main.m3u8", "https://www.tdtchannels.com/logos/tv/antena3.png", ChannelCategory.GENERAL),
    Channel("Cuatro", "https://mdslivehlsb-i.akamaihd.net/hls/live/623614/cuatro/bitrate_1.m3u8", "https://www.tdtchannels.com/logos/tv/cuatro.png", ChannelCategory.GENERAL),
    Channel("Telecinco", "https://mdslivehlsb-i.akamaihd.net/hls/live/623617/telecinco/bitrate_1.m3u8", "https://www.tdtchannels.com/logos/tv/telecinco.png", ChannelCategory.GENERAL),
    Channel("laSexta", "https://antena3-grp.akamaized.net/live/lasexta_hls/lasexta_main.m3u8", "https://www.tdtchannels.com/logos/tv/lasexta.png", ChannelCategory.GENERAL),
    Channel("24 Horas", "https://rtvelivestream.akamaized.net/rtvesec/24h/24h_main.m3u8", "https://www.tdtchannels.com/logos/tv/24h.png", ChannelCategory.NEWS),
    Channel("Teledeporte", "https://rtvelivestream.akamaized.net/rtvesec/tdp/tdp_main.m3u8", "https://www.tdtchannels.com/logos/tv/teledeporte.png", ChannelCategory.SPORTS),
    Channel("Clan TV", "https://rtvelivestream.akamaized.net/rtvesec/clan/clan_main.m3u8", "https://www.tdtchannels.com/logos/tv/clantve.png", ChannelCategory.KIDS),
    Channel("Neox", "https://antena3-grp.akamaized.net/live/neox_hls/neox_main.m3u8", "https://www.tdtchannels.com/logos/tv/neox.png", ChannelCategory.ENTERTAINMENT),
    Channel("Nova", "https://antena3-grp.akamaized.net/live/nova_hls/nova_main.m3u8", "https://www.tdtchannels.com/logos/tv/nova.png", ChannelCategory.ENTERTAINMENT),
    Channel("Mega", "https://antena3-grp.akamaized.net/live/mega_hls/mega_main.m3u8", "https://www.tdtchannels.com/logos/tv/mega.png", ChannelCategory.ENTERTAINMENT),
    Channel("FDF", "https://mdslivehlsb-i.akamaihd.net/hls/live/623625/fdf/bitrate_1.m3u8", "https://www.tdtchannels.com/logos/tv/fdf.png", ChannelCategory.ENTERTAINMENT),
    Channel("Energy", "https://mdslivehlsb-i.akamaihd.net/hls/live/623627/energy/bitrate_1.m3u8", "https://www.tdtchannels.com/logos/tv/energy.png", ChannelCategory.ENTERTAINMENT),
    Channel("Divinity", "https://mdslivehlsb-i.akamaihd.net/hls/live/623626/divinity/bitrate_1.m3u8", "https://www.tdtchannels.com/logos/tv/divinity.png", ChannelCategory.ENTERTAINMENT),
    Channel("BeMad", "https://mdslivehlsb-i.akamaihd.net/hls/live/623629/bemad/bitrate_1.m3u8", "https://www.tdtchannels.com/logos/tv/bemad.png", ChannelCategory.ENTERTAINMENT),
)
