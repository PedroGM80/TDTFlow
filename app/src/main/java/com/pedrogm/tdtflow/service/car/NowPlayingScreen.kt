package com.pedrogm.tdtflow.service.car

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.Header
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.di.CarEntryPoint
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.player.PlayerState
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Pantalla "Reproduciendo ahora" para Automotive OS.
 *
 * Observa el estado del [TdtPlayer] en tiempo real y refleja buffering,
 * reproducción activa y errores. El botón Detener para la reproducción
 * y vuelve automáticamente a la lista de canales.
 * El logo del canal se carga con [CarArtworkLoader], igual que en TV y móvil.
 */
class NowPlayingScreen(
    carContext: CarContext,
    private val channel: Channel
) : Screen(carContext), DefaultLifecycleObserver {

    private val tdtPlayer = EntryPointAccessors
        .fromApplication(carContext.applicationContext, CarEntryPoint::class.java)
        .getTdtPlayer()

    private var playerState: PlayerState = PlayerState.BUFFERING
    private val screenScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val artworkLoader = CarArtworkLoader(
        context = carContext.applicationContext,
        scope = screenScope,
        onLoaded = { invalidate() }
    )

    init {
        lifecycle.addObserver(this)
        artworkLoader.load(channel.logo, channel.name)
        screenScope.launch {
            tdtPlayer.playerState.collect { state ->
                playerState = state
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        val statusText = when (playerState) {
            PlayerState.PLAYING -> carContext.getString(R.string.car_now_playing)
            PlayerState.BUFFERING -> carContext.getString(R.string.car_buffering)
            PlayerState.ERROR -> carContext.getString(R.string.car_error)
            else -> ""
        }

        val rowBuilder = Row.Builder().setTitle(channel.name)
        if (statusText.isNotEmpty()) rowBuilder.addText(statusText)
        artworkLoader.get(channel.logo, channel.name)?.let { rowBuilder.setImage(it) }

        val stopAction = Action.Builder()
            .setTitle(carContext.getString(R.string.car_stop))
            .setOnClickListener {
                tdtPlayer.stop()
                screenManager.pop()
            }
            .build()

        return PaneTemplate.Builder(
            Pane.Builder()
                .addRow(rowBuilder.build())
                .addAction(stopAction)
                .build()
        )
            .setHeader(
                Header.Builder()
                    .setTitle(carContext.getString(R.string.car_now_playing_title))
                    .setStartHeaderAction(Action.BACK)
                    .build()
            )
            .build()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        screenScope.cancel()
    }
}
