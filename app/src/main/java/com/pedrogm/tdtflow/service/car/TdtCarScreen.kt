package com.pedrogm.tdtflow.service.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarIcon
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.di.CarEntryPoint
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.model.ChannelCategory
import com.pedrogm.tdtflow.player.PlayerState
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Lista de canales de música para Automotive OS.
 *
 * Muestra únicamente [ChannelCategory.MUSIC] porque Android Auto no puede
 * renderizar vídeo. Al seleccionar un canal navega a [NowPlayingScreen].
 */
class TdtCarScreen(carContext: CarContext) : Screen(carContext), DefaultLifecycleObserver {

    private val entryPoint = EntryPointAccessors.fromApplication(
        carContext.applicationContext,
        CarEntryPoint::class.java
    )
    private val getChannelsUseCase = entryPoint.getChannelsUseCase()
    private val tdtPlayer = entryPoint.getTdtPlayer()

    private var musicChannels: List<Channel> = emptyList()
    private var currentUrl: String? = null
    private var playerState: PlayerState = PlayerState.IDLE
    private val screenScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        lifecycle.addObserver(this)
        loadMusicChannels()
        observePlayerState()
    }

    private fun loadMusicChannels() {
        screenScope.launch {
            musicChannels = getChannelsUseCase().first()
                .filter { it.category == ChannelCategory.MUSIC }
            invalidate()
        }
    }

    private fun observePlayerState() {
        screenScope.launch {
            tdtPlayer.playerState.collect { state ->
                playerState = state
                currentUrl = tdtPlayer.getCurrentStreamUrl()
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        if (musicChannels.isEmpty()) {
            return PaneTemplate.Builder(
                Pane.Builder()
                    .addRow(Row.Builder().setTitle(carContext.getString(R.string.car_loading_channels)).build())
                    .build()
            )
                .setHeader(
                    Header.Builder()
                        .setTitle(carContext.getString(R.string.car_channels_title))
                        .setStartHeaderAction(Action.APP_ICON)
                        .build()
                )
                .build()
        }

        val appIcon = CarIcon.Builder(
            IconCompat.createWithResource(carContext, R.mipmap.ic_launcher)
        ).build()

        val listBuilder = ItemList.Builder()
        musicChannels.forEach { channel ->
            val isActive = channel.url == currentUrl && playerState == PlayerState.PLAYING
            val subtitle = if (isActive)
                carContext.getString(R.string.car_now_playing)
            else
                ""

            val rowBuilder = Row.Builder()
                .setTitle(channel.name)
                .setImage(appIcon)
                .setOnClickListener {
                    tdtPlayer.play(channel.url)
                    screenManager.push(NowPlayingScreen(carContext, channel))
                }

            if (subtitle.isNotEmpty()) rowBuilder.addText(subtitle)

            listBuilder.addItem(rowBuilder.build())
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setHeader(
                Header.Builder()
                    .setTitle(carContext.getString(R.string.car_channels_title))
                    .setStartHeaderAction(Action.APP_ICON)
                    .build()
            )
            .build()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        screenScope.cancel()
    }
}
