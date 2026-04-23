package com.pedrogm.tdtflow.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Channels : Route

    @Serializable
    data object Favorites : Route

    @Serializable
    data object TvChannels : Route

    @Serializable
    data object TvFavorites : Route
}
