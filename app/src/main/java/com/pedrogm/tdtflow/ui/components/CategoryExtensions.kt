package com.pedrogm.tdtflow.ui.components

import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.*
import com.pedrogm.tdtflow.data.model.ChannelCategory

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
