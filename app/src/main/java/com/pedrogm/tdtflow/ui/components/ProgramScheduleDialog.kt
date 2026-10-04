package com.pedrogm.tdtflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.pedrogm.tdtflow.R
import com.pedrogm.tdtflow.domain.model.Program
import com.pedrogm.tdtflow.domain.model.isNow
import com.pedrogm.tdtflow.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Guía de programación de un canal: lista de programas del día, con el que está
 * "ahora" resaltado. Se usa tanto desde el reproductor móvil como el de TV.
 */
@Composable
fun ProgramScheduleDialog(
    channelName: String,
    isLoading: Boolean,
    schedule: List<Program>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.padding_large)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.program_schedule_title, channelName),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Lucide.X,
                        contentDescription = stringResource(R.string.close),
                        tint = Color.White
                    )
                }
            }

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }

                schedule.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.program_schedule_empty),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                else -> {
                    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = dimensionResource(R.dimen.padding_large),
                            vertical = dimensionResource(R.dimen.spacing_small)
                        ),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                    ) {
                        items(schedule, key = { it.startTime }) { program ->
                            val isCurrent = program.isNow()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (isCurrent) Color.White.copy(alpha = 0.12f) else Color.Transparent,
                                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))
                                    )
                                    .padding(dimensionResource(R.dimen.spacing_medium)),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = timeFormat.format(Date(program.startTime)),
                                    color = if (isCurrent) AppColors.liveIndicator else Color.White.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold
                                )
                                Box(modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_medium))) {
                                    Column {
                                        Text(
                                            text = program.title,
                                            color = Color.White,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        program.description?.takeIf { it.isNotBlank() }?.let { description ->
                                            Text(
                                                text = description,
                                                color = Color.White.copy(alpha = 0.6f),
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
