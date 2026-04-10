package com.exist.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun HomeScreen(
    viewModel: HomeViewModel,
    onTakeProof: () -> Unit,
    onOpenToday: (String) -> Unit,
    onOpenArchive: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenRecap: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val todayKey = DateUtils.todayDayKey()

    Box(modifier = Modifier.fillMaxSize()) {
        ExistBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("Exist", style = MaterialTheme.typography.displayLarge)
                Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
                Text("Welcome, ${state.displayName}", color = MaterialTheme.colorScheme.tertiary)
                if (state.profileTagline.isNotBlank()) {
                    Text(state.profileTagline, color = MaterialTheme.colorScheme.tertiary)
                }
                Text(DateUtils.displayDate(todayKey), color = MaterialTheme.colorScheme.tertiary)

                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (state.todayHasProof) "Today has proof." else "Today is still empty.",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = if (state.todayHasProof) {
                                "You marked this day. Add more moments anytime."
                            } else {
                                "One honest photo is enough to say: I was here."
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                state.todayPhotos.firstOrNull()?.let { latest ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            AsyncImage(
                                model = latest.imagePath,
                                contentDescription = "Latest proof",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(190.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "${latest.emotionTag.displayName} at ${DateUtils.displayTime(latest.createdAt)}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            if (latest.caption.isNotBlank()) {
                                Text(
                                    latest.caption,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onTakeProof,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text("Take Today's Proof")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { onOpenToday(todayKey) }, modifier = Modifier.weight(1f), enabled = state.todayPhotos.isNotEmpty()) {
                        Text("Today")
                    }
                    Button(onClick = onOpenArchive, modifier = Modifier.weight(1f)) {
                        Text("Archive")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { onOpenRecap(todayKey) }, modifier = Modifier.weight(1f), enabled = state.todayPhotos.isNotEmpty()) {
                        Text("Recap")
                    }
                    Button(onClick = onOpenProfile, modifier = Modifier.weight(1f)) {
                        Text("Profile")
                    }
                }
            }
        }
    }
}
