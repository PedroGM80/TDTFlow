package com.pedrogm.tdtflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val url: String,
    val name: String,
    val logo: String,
    val category: String,
    val ambit: String? = null
)

fun ChannelEntity.toDomain(): Channel = Channel(
    name = name,
    url = url,
    logo = logo,
    category = ChannelCategory.valueOf(category)
)

fun Channel.toEntity(ambit: String? = null): ChannelEntity = ChannelEntity(
    name = name,
    url = url,
    logo = logo,
    category = category.name,
    ambit = ambit
)
