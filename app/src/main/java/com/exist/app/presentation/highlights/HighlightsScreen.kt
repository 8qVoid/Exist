package com.exist.app.presentation.highlights

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.exist.app.core.util.DateUtils
import com.exist.app.presentation.common.ExistBackground

@Composable
fun HighlightsScreen(
    viewModel: HighlightsViewModel,
    onOpenDay: (String) -> Unit,
    onOpenRecap: (String) -> Unit,
    onOpenArchive: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        ExistBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text("Highlights", style = MaterialTheme.typography.displayLarge)
                Text("A reflective stream of your real moments.", color = MaterialTheme.colorScheme.tertiary)
            }

            if (state.latestMoments.isNotEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Today", style = MaterialTheme.typography.headlineMedium)
                            val latest = state.latestMoments.first()
                            AsyncImage(
                                model = latest.imagePath,
                                contentDescription = "Latest moment",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                            Text("${latest.emotionTag.displayName} • ${DateUtils.displayTime(latest.createdAt)}")
                            if (latest.caption.isNotBlank()) {
                                Text(
                                    latest.caption,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(onClick = { onOpenRecap(latest.dayKey) }) { Text("Play recap") }
                                Button(onClick = { onOpenDay(latest.dayKey) }) { Text("Open day") }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Recent days", style = MaterialTheme.typography.headlineMedium)
                    Text(
                        "Open timeline",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onOpenArchive)
                    )
                }
            }

            items(state.activeDays, key = { it.dayKey }) { day ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDay(day.dayKey) }
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(DateUtils.displayDate(day.dayKey), style = MaterialTheme.typography.titleLarge)
                        day.photos.firstOrNull()?.let { photo ->
                            AsyncImage(
                                model = photo.imagePath,
                                contentDescription = "Preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )
                            Text(
                                text = "${day.photos.size} moment(s) • ${photo.emotionTag.displayName}",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            if (photo.caption.isNotBlank()) {
                                Text(
                                    text = photo.caption,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
