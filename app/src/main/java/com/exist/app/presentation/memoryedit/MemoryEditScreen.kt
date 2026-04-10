package com.exist.app.presentation.memoryedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.exist.app.domain.model.EmotionTag
import com.exist.app.presentation.common.ExistBackground

@Composable
fun MemoryEditScreen(
    viewModel: MemoryEditViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBack()
    }

    LaunchedEffect(state.saveCompleted) {
        if (state.saveCompleted) {
            viewModel.consumeSaveComplete()
            onBack()
        }
    }

    val memory = state.memory

    Box(modifier = Modifier.fillMaxSize()) {
        ExistBackground()

        if (memory == null) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Memory not found")
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = onBack) { Text("Back") }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Edit Memory", style = MaterialTheme.typography.displayLarge)

                AsyncImage(
                    model = memory.imagePath,
                    contentDescription = "Memory image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )

                OutlinedTextField(
                    value = state.caption,
                    onValueChange = viewModel::onCaptionChanged,
                    label = { Text("Caption") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EmotionTag.entries) { emotion ->
                        AssistChip(
                            onClick = { viewModel.onEmotionSelected(emotion) },
                            label = { Text(emotion.displayName) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Temporary memory")
                    Switch(checked = state.isTemporary, onCheckedChange = viewModel::onTemporaryChanged)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = onBack) { Text("Cancel") }
                    Button(onClick = { viewModel.saveChanges() }, enabled = !state.isSaving) { Text("Save") }
                    Button(onClick = { viewModel.deleteMemory() }) { Text("Delete") }
                }
            }
        }
    }
}
