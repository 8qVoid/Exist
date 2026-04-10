package com.exist.app.presentation.archive

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.exist.app.core.util.DateUtils
import com.exist.app.presentation.common.ExistBackground

@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    onOpenDay: (String) -> Unit,
    onOpenRecap: (String) -> Unit,
    onBack: () -> Unit
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
                Spacer(modifier = Modifier.height(12.dp))
                Text("Archive", style = MaterialTheme.typography.displayLarge)
                Text("A quiet timeline of days.", color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack) { Text("Back") }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(state.days, key = { it.dayKey }) { day ->
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDay(day.dayKey) }
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(DateUtils.displayDate(day.dayKey), style = MaterialTheme.typography.headlineMedium)
                            Text(
                                text = if (day.hasProof) "Filled" else "Empty",
                                color = if (day.hasProof) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                            )
                        }

                        day.photos.firstOrNull()?.let { photo ->
                            AsyncImage(
                                model = photo.imagePath,
                                contentDescription = "Day preview",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            )
                            if (photo.caption.isNotBlank()) {
                                Text(photo.caption, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }

                        Text("${day.photos.size} moment(s)", color = MaterialTheme.colorScheme.tertiary)

                        if (day.photos.isNotEmpty()) {
                            Text(
                                "Play recap",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onOpenRecap(day.dayKey) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }
        }
    }
}
