package com.pedrogm.tdtflow.service

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.domain.usecase.GetChannelsUseCase
import com.pedrogm.tdtflow.player.TdtPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Puente entre TDTFlow y Android Auto.
 *
 * Expone únicamente los canales de categoría [ChannelCategory.MUSIC] porque
 * Android Auto no puede mostrar vídeo en la pantalla del coche. Los canales
 * de música/radio sí tienen sentido durante la conducción.
 *
 * **Importante:** este servicio NO llama a `player.release()` en [onDestroy]
 * porque [TdtPlayer] es un singleton Hilt compartido con el resto de la app.
 */
@OptIn(UnstableApi::class)
@AndroidEntryPoint
class TdtMediaService : MediaLibraryService() {

    @Inject lateinit var tdtPlayer: TdtPlayer
    @Inject lateinit var getChannelsUseCase: GetChannelsUseCase

    private var mediaLibrarySession: MediaLibrarySession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var musicChannels: List<Channel> = emptyList()

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            musicChannels = getChannelsUseCase().first()
                .filter { it.category == ChannelCategory.MUSIC }
        }

        tdtPlayer.exoPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            /* handleAudioFocus = */ true
        )

        mediaLibrarySession = MediaLibrarySession.Builder(
            this,
            tdtPlayer.exoPlayer,
            LibraryCallback()
        ).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        mediaLibrarySession

    override fun onDestroy() {
        mediaLibrarySession?.release()
        mediaLibrarySession = null
        serviceScope.cancel()
        super.onDestroy()
    }

    private inner class LibraryCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val root = MediaItem.Builder()
                .setMediaId(ROOT_ID)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_RADIO_STATIONS)
                        .setTitle("TDTFlow Radio")
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(root, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: MediaLibraryService.LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            if (parentId != ROOT_ID) {
                return Futures.immediateFuture(LibraryResult.ofItemList(emptyList(), params))
            }
            val items = musicChannels.map { channel ->
                MediaItem.Builder()
                    .setMediaId(channel.url)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(channel.name)
                            .setSubtitle(channel.category.name.lowercase()
                                .replaceFirstChar { it.uppercase() })
                            .setArtworkUri(
                                channel.logo.takeIf { it.isNotEmpty() }
                                    ?.let { android.net.Uri.parse(it) }
                            )
                            .setIsBrowsable(false)
                            .setIsPlayable(true)
                            .setMediaType(MediaMetadata.MEDIA_TYPE_RADIO_STATION)
                            .build()
                    )
                    .build()
            }
            return Futures.immediateFuture(LibraryResult.ofItemList(items, params))
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            // Resuelve la URI (= mediaId) y enriquece con la metadata del canal
            // para que Android Auto muestre título, subtítulo y artwork correctamente.
            val resolved = mediaItems.map { item ->
                val channel = musicChannels.firstOrNull { it.url == item.mediaId }
                val meta = if (channel != null) {
                    item.mediaMetadata.buildUpon()
                        .setTitle(channel.name)
                        .setSubtitle(channel.category.name.lowercase()
                            .replaceFirstChar { it.uppercase() })
                        .setArtworkUri(
                            channel.logo.takeIf { it.isNotEmpty() }
                                ?.let { android.net.Uri.parse(it) }
                        )
                        .build()
                } else item.mediaMetadata
                item.buildUpon().setUri(item.mediaId).setMediaMetadata(meta).build()
            }.toMutableList()
            return Futures.immediateFuture(resolved)
        }
    }

    companion object {
        private const val ROOT_ID = "TDT_MUSIC_ROOT"
    }
}
