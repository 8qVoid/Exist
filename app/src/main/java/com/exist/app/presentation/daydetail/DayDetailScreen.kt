package com.exist.app.presentation.daydetail

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.exist.app.core.util.DateUtils
import com.exist.app.presentation.common.ExistBackground

@Composable
fun DayDetailScreen(
    dayKey: String,
    viewModel: DayDetailViewModel,
    onBack: () -> Unit,
    onRecap: () -> Unit,
    onEditMemory: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        ExistBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(DateUtils.displayDate(dayKey), style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onBack) { Text("Back") }
                    Button(onClick = onRecap, enabled = state.photos.isNotEmpty()) { Text("Recap") }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.photos.isEmpty()) {
                item { Text("No memories yet for this day.") }
            }

            items(state.photos, key = { it.id }) { photo ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditMemory(photo.id) }
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AsyncImage(
                            model = photo.imagePath,
                            contentDescription = "Memory photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                        Text("${photo.emotionTag.displayName} • ${DateUtils.displayTime(photo.createdAt)}")
                        if (photo.caption.isNotBlank()) {
                            Text(photo.caption)
                        }
                        if (photo.isTemporary) {
                            Text("Temporary", color = MaterialTheme.colorScheme.tertiary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { onEditMemory(photo.id) }) { Text("Edit") }
                            Button(onClick = { viewModel.deleteMemory(photo.id) }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
}
