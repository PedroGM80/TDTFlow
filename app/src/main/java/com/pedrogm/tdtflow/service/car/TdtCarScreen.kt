package com.pedrogm.tdtflow.service.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
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
 * Muestra únicamente [ChannelCategory.MUSIC]. Al seleccionar un canal
 * inicia la reproducción y navega a [NowPlayingScreen].
 * Los logos se cargan de forma asíncrona con [CarArtworkLoader] para
 * mantener la misma coherencia visual que el móvil y la TV.
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

    private val artworkLoader = CarArtworkLoader(
        context = carContext.applicationContext,
        scope = screenScope,
        onLoaded = { invalidate() }
    )

    init {
        lifecycle.addObserver(this)
        loadMusicChannels()
        observePlayerState()
    }

    private fun loadMusicChannels() {
        screenScope.launch {
            musicChannels = getChannelsUseCase().first()
                .filter { it.category == ChannelCategory.MUSIC }
            musicChannels.forEach { artworkLoader.load(it.logo, it.name) }
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
                .setHeader(header(carContext.getString(R.string.car_channels_title), Action.APP_ICON))
                .build()
        }

        val listBuilder = ItemList.Builder()
        musicChannels.forEach { channel ->
            val isActive = channel.url == currentUrl && playerState == PlayerState.PLAYING
            val icon = artworkLoader.get(channel.logo, channel.name)

            val rowBuilder = Row.Builder()
                .setTitle(channel.name)
                .setOnClickListener {
                    tdtPlayer.play(channel.url)
                    screenManager.push(NowPlayingScreen(carContext, channel))
                }

            icon?.let { rowBuilder.setImage(it) }
            if (isActive) rowBuilder.addText(carContext.getString(R.string.car_now_playing))

            listBuilder.addItem(rowBuilder.build())
        }

        return ListTemplate.Builder()
            .setSingleList(listBuilder.build())
            .setHeader(header(carContext.getString(R.string.car_channels_title), Action.APP_ICON))
            .build()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        screenScope.cancel()
    }

    private fun header(title: String, startAction: Action) =
        Header.Builder().setTitle(title).setStartHeaderAction(startAction).build()
}
