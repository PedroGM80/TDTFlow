package com.pedrogm.tdtflow.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ChannelEntity::class], version = 1, exportSchema = false)
abstract class TdtDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
}
