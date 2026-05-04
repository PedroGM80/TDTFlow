package com.pedrogm.tdtflow.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChannelDaoTest {

    private lateinit var database: TdtDatabase
    private lateinit var channelDao: ChannelDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TdtDatabase::class.java
        ).allowMainThreadQueries().build()
        channelDao = database.channelDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAllChannels() = runTest {
        val channels = listOf(
            ChannelEntity(url = "url1", name = "Channel 1", logo = "logo1", category = "GENERAL"),
            ChannelEntity(url = "url2", name = "Channel 2", logo = "logo2", category = "NEWS")
        )
        
        channelDao.insertChannels(channels)
        
        val result = channelDao.getAllChannels()
        assertEquals(2, result.size)
        assertEquals("Channel 1", result[0].name)
    }

    @Test
    fun deleteAllChannels() = runTest {
        val channels = listOf(
            ChannelEntity(url = "url1", name = "Channel 1", logo = "logo1", category = "GENERAL")
        )
        channelDao.insertChannels(channels)
        
        channelDao.deleteAll()
        
        val result = channelDao.getAllChannels()
        assertEquals(0, result.size)
    }
}
