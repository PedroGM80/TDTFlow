package com.pedrogm.tdtflow.ui.options

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.ui.options.components.BrokenChannelsSectionTv
import com.pedrogm.tdtflow.ui.options.components.BufferSectionTv
import com.pedrogm.tdtflow.ui.options.components.LanguageSectionTv
import com.pedrogm.tdtflow.ui.options.components.OptionsMenuContent
import com.pedrogm.tdtflow.ui.options.components.ThemeSectionTv

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsMenuScreen(
    viewModel: OptionsMenuViewModel,
    onDismiss: () -> Unit,
    showBrokenChannels: Boolean,
    onToggleBroken: () -> Unit,
    isTv: Boolean = false
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (isTv) {
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(state.isOpen) {
            if (state.isOpen) {
                focusRequester.requestFocus()
            }
        }

        BackHandler(enabled = state.isOpen) {
            viewModel.onIntent(OptionsMenuIntent.Dismiss)
        }

        AnimatedVisibility(
            visible = state.isOpen,
            enter = fadeIn() + slideInHorizontally { it },
            exit = fadeOut() + slideOutHorizontally { it }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { viewModel.onIntent(OptionsMenuIntent.Dismiss) },
                contentAlignment = Alignment.CenterEnd
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(dimensionResource(R.dimen.options_panel_width_tv))
                        .clickable(enabled = false) {},
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = dimensionResource(R.dimen.elevation_high)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(dimensionResource(R.dimen.padding_extra_large))
                    ) {
                        Text(
                            text = stringResource(R.string.options_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_large))
                        )

                        ThemeSectionTv(
                            selectedTheme = state.selectedTheme,
                            onThemeSelected = { viewModel.onIntent(OptionsMenuIntent.SelectTheme(it)) },
                            focusRequester = focusRequester
                        )

                        LanguageSectionTv(
                            selectedLanguage = state.language,
                            onLanguageSelected = { viewModel.onIntent(OptionsMenuIntent.SelectLanguage(it)) }
                        )

                        BufferSectionTv(
                            selectedBuffer = state.buffer,
                            onBufferSelected = { viewModel.onIntent(OptionsMenuIntent.SelectBuffer(it)) }
                        )

                        BrokenChannelsSectionTv(
                            showBroken = showBrokenChannels,
                            onToggle = onToggleBroken
                        )
                    }
                }
            }
        }
    } else {
        if (state.isOpen) {
            ModalBottomSheet(
                onDismissRequest = {
                    viewModel.onIntent(OptionsMenuIntent.Dismiss)
                    onDismiss()
                },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(
                    topStart = dimensionResource(R.dimen.radius_extra_large),
                    topEnd = dimensionResource(R.dimen.radius_extra_large)
                )
            ) {
                OptionsMenuContent(
                    state = state,
                    onIntent = { viewModel.onIntent(it) },
                    showBrokenChannels = showBrokenChannels,
                    onToggleBroken = onToggleBroken
                )
            }
        }
    }
}
