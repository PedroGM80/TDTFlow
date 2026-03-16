package com.pedrogm.tdtflow.data.di

import com.google.gson.Gson
import com.pedrogm.tdtflow.data.remote.TdtChannelsService
import com.pedrogm.tdtflow.domain.repository.ChannelRepository
import com.pedrogm.tdtflow.data.repository.ChannelRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    private const val BASE_URL = "https://listadocanalestdt.firebaseio.com"

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideTdtChannelsService(retrofit: Retrofit): TdtChannelsService =
        retrofit.create(TdtChannelsService::class.java)

    @Provides
    @Singleton
    fun provideChannelRepository(impl: ChannelRepositoryImpl): ChannelRepository = impl
}
