package com.exist.app.presentation.recap

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.exist.app.core.util.DateUtils
import com.exist.app.presentation.common.ExistBackground
import kotlinx.coroutines.delay

@Composable
fun RecapScreen(
    dayKey: String,
    viewModel: RecapViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var frame by remember(dayKey) { mutableIntStateOf(0) }

    LaunchedEffect(state.photos.size) {
        if (state.photos.isEmpty()) return@LaunchedEffect
        while (true) {
            delay(2400)
            frame = (frame + 1) % state.photos.size
        }
    }

    val current = state.photos.getOrNull(frame)

    Box(modifier = Modifier.fillMaxSize()) {
        ExistBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Day Recap", style = MaterialTheme.typography.displayLarge)
                Text(DateUtils.displayDate(dayKey), color = MaterialTheme.colorScheme.tertiary)
            }

            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (current == null) {
                        Text("No moments to recap.")
                    } else {
                        Crossfade(targetState = current.imagePath, label = "recap") { path ->
                            AsyncImage(
                                model = path,
                                contentDescription = "Recap image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(360.dp)
                            )
                        }
                        Text("${current.emotionTag.displayName} • ${DateUtils.displayTime(current.createdAt)}")
                        if (current.caption.isNotBlank()) {
                            Text(current.caption)
                        }
                        Text("Frame ${frame + 1} / ${state.photos.size}", color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }

            Column {
                Text(
                    "You captured ${state.photos.size} real moment${if (state.photos.size == 1) "" else "s"} today.",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Back")
                }
            }
        }
    }
}
