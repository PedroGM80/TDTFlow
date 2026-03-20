package com.pedrogm.tdtflow.di

import android.content.Context
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pedrogm.tdtflow.data.IOptionsPreferences
import com.pedrogm.tdtflow.data.OptionsPreferences
import com.pedrogm.tdtflow.data.BrokenChannelTrackerImpl
import com.pedrogm.tdtflow.data.repository.ChannelRepositoryImpl
import com.pedrogm.tdtflow.data.repository.FavoritesRepositoryImpl
import com.pedrogm.tdtflow.domain.repository.ChannelRepository
import com.pedrogm.tdtflow.domain.repository.FavoritesRepository
import com.pedrogm.tdtflow.domain.tracker.BrokenChannelTracker
import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.GetChannelsUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideChannelRepository(): ChannelRepository = ChannelRepositoryImpl(
        onError = { FirebaseCrashlytics.getInstance().recordException(it) }
    )

    @Provides
    @Singleton
    fun provideGetChannelsUseCase(repo: ChannelRepository): GetChannelsUseCase =
        GetChannelsUseCase(repo)

    @Provides
    @Singleton
    fun provideBrokenChannelTracker(@ApplicationContext ctx: Context): BrokenChannelTracker =
        BrokenChannelTrackerImpl(ctx)

    @Provides
    @Singleton
    fun provideFavoritesRepository(@ApplicationContext ctx: Context): FavoritesRepository =
        FavoritesRepositoryImpl(ctx)

    @Provides
    fun provideAddFavoriteUseCase(repo: FavoritesRepository): AddFavoriteUseCase =
        AddFavoriteUseCase(repo)

    @Provides
    fun provideRemoveFavoriteUseCase(repo: FavoritesRepository): RemoveFavoriteUseCase =
        RemoveFavoriteUseCase(repo)

    @Provides
    fun provideGetFavoritesUseCase(repo: FavoritesRepository): GetFavoritesUseCase =
        GetFavoritesUseCase(repo)

    @Provides
    @Singleton
    fun provideOptionsPreferences(@ApplicationContext ctx: Context): IOptionsPreferences =
        OptionsPreferences(ctx)
}
