package com.pedrogm.tdtflow.data.di

import android.content.Context
import androidx.room.Room
import com.pedrogm.tdtflow.data.local.ChannelDao
import com.pedrogm.tdtflow.data.local.TdtDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TdtDatabase =
        Room.databaseBuilder(context, TdtDatabase::class.java, "tdtflow.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideChannelDao(db: TdtDatabase): ChannelDao = db.channelDao()
}
