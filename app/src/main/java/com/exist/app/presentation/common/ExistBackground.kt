package com.exist.app.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset

@Composable
fun ExistBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
                        Color(0xFF16210E),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1200f, 2400f)
                )
            )
    )
}
