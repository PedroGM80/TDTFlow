package com.pedrogm.tdtflow.data

import com.pedrogm.tdtflow.data.model.Channel
import com.pedrogm.tdtflow.data.model.ChannelCategory

internal fun fallbackChannels(): List<Channel> = listOf(
    // RTVE - URLs actualizadas desde TDTChannels (ztnr.rtve.es)
    Channel("La 1", "https://ztnr.rtve.es/ztnr/1688877.m3u8", "https://pbs.twimg.com/profile_images/2008842210414915584/zIp_go25_200x200.jpg", ChannelCategory.GENERAL),
    Channel("La 2", "https://ztnr.rtve.es/ztnr/1688885.m3u8", "https://yt3.googleusercontent.com/ytc/AIdro_kqgHWySi5xprs1VFCNCX0IKNT8yXBLZC43JMoB8j0JUto=s200", ChannelCategory.GENERAL),
    Channel("24 Horas", "https://ztnr.rtve.es/ztnr/1694255.m3u8", "https://pbs.twimg.com/profile_images/1634293543987453954/mb1Rzmso_200x200.jpg", ChannelCategory.NEWS),
    Channel("Clan TV", "https://rtvelivestream.rtve.es/rtvesec/clan/clan_main_dvr.m3u8", "", ChannelCategory.KIDS),
    Channel("Teledeporte", "https://rtvelivestream.rtve.es/rtvesec/tdp/tdp_main_dvr.m3u8", "", ChannelCategory.SPORTS),
    // RTVE alternativas (CloudFront)
    Channel("La 1 HD", "https://d2jws8nf49xuk0.cloudfront.net/v1/master/3722c60a815c199d9c0ef36c5b73da68a62b09d1/cc-3ikffw175xm6u/La1ES.m3u8", "", ChannelCategory.GENERAL),
    Channel("La 2 HD", "https://d4g9wh8d4wiaw.cloudfront.net/v1/master/3722c60a815c199d9c0ef36c5b73da68a62b09d1/cc-crbrakk0yedqb/La2ES.m3u8", "", ChannelCategory.GENERAL),
    Channel("24H HD", "https://d32rw80ytx9uxs.cloudfront.net/v1/master/3722c60a815c199d9c0ef36c5b73da68a62b09d1/cc-vlldndmow4yre/24HES.m3u8", "", ChannelCategory.NEWS),
)
