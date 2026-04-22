package com.pedrogm.tdtflow.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pedrogm.tdtflow.data.BrokenChannelTrackerImpl
import com.pedrogm.tdtflow.data.IOptionsPreferences
import com.pedrogm.tdtflow.data.OptionsDataStore
import com.pedrogm.tdtflow.data.repository.ChannelRepositoryImpl
import com.pedrogm.tdtflow.data.repository.FavoritesRepositoryImpl
import com.pedrogm.tdtflow.data.repository.MockEpgRepositoryImpl
import com.pedrogm.tdtflow.domain.repository.EpgRepository
import com.pedrogm.tdtflow.domain.usecase.GetNowPlayingUseCase
import com.pedrogm.tdtflow.domain.repository.ChannelRepository
import com.pedrogm.tdtflow.domain.repository.FavoritesRepository
import com.pedrogm.tdtflow.domain.tracker.BrokenChannelTracker
import com.pedrogm.tdtflow.domain.usecase.AddFavoriteUseCase
import com.pedrogm.tdtflow.domain.usecase.ClearFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.GetChannelsUseCase
import com.pedrogm.tdtflow.domain.usecase.GetFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.ImportFavoritesUseCase
import com.pedrogm.tdtflow.domain.usecase.RemoveFavoriteUseCase
import com.pedrogm.tdtflow.player.TdtPlayer
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
    fun provideChannelRepository(channelDao: com.pedrogm.tdtflow.data.local.ChannelDao): ChannelRepository = ChannelRepositoryImpl(
        channelDao = channelDao,
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
    fun provideImportFavoritesUseCase(repo: FavoritesRepository): ImportFavoritesUseCase =
        ImportFavoritesUseCase(repo)

    @Provides
    fun provideClearFavoritesUseCase(repo: FavoritesRepository): ClearFavoritesUseCase =
        ClearFavoritesUseCase(repo)

    @Provides
    @Singleton
    fun provideEpgRepository(): EpgRepository = MockEpgRepositoryImpl()

    @Provides
    fun provideGetNowPlayingUseCase(repo: EpgRepository): GetNowPlayingUseCase =
        GetNowPlayingUseCase(repo)

    @Provides
    @Singleton
    fun provideOptionsPreferences(@ApplicationContext ctx: Context): IOptionsPreferences =
        OptionsDataStore(ctx)

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideTdtPlayer(
        @ApplicationContext context: Context,
        prefs: IOptionsPreferences
    ): TdtPlayer = TdtPlayer(context, prefs)
}
