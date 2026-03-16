package com.pedrogm.tdtflow.ui.components

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.*
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.ChannelCategory

/** Mapea cada categoría a un icono Lucide. */
fun ChannelCategory.toLucideIcon(): ImageVector = when (this) {
    ChannelCategory.GENERAL -> Lucide.Tv
    ChannelCategory.NEWS -> Lucide.Newspaper
    ChannelCategory.SPORTS -> Lucide.Trophy
    ChannelCategory.KIDS -> Lucide.Baby
    ChannelCategory.ENTERTAINMENT -> Lucide.Clapperboard
    ChannelCategory.REGIONAL -> Lucide.MapPin
    ChannelCategory.MUSIC -> Lucide.Music
    ChannelCategory.OTHER -> Lucide.LayoutGrid
}

/** Mapea cada categoría a su recurso de string. */
fun ChannelCategory.toStringRes(): Int = when (this) {
    ChannelCategory.GENERAL -> R.string.category_general
    ChannelCategory.NEWS -> R.string.category_news
    ChannelCategory.SPORTS -> R.string.category_sports
    ChannelCategory.KIDS -> R.string.category_kids
    ChannelCategory.ENTERTAINMENT -> R.string.category_entertainment
    ChannelCategory.REGIONAL -> R.string.category_regional
    ChannelCategory.MUSIC -> R.string.category_music
    ChannelCategory.OTHER -> R.string.category_other
}
