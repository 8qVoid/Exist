package com.exist.app.presentation.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exist.app.domain.model.EmotionTag
import com.exist.app.presentation.common.ExistBackground
import kotlin.math.max

private enum class RangeMode { DAYS_7, DAYS_30 }

@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val emotionItems = state.emotionCounts.entries.sortedByDescending { it.value }
    var rangeMode by remember { mutableStateOf(RangeMode.DAYS_7) }
    val trendData = if (rangeMode == RangeMode.DAYS_7) state.dailyMomentsLast7 else state.dailyMomentsLast30
    val trendLabels = if (rangeMode == RangeMode.DAYS_7) {
        trendData.map { it.first.takeLast(2) }
    } else {
        trendData.mapIndexedNotNull { index, pair -> if (index % 5 == 0) pair.first.takeLast(2) else null }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ExistBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Analytics", style = MaterialTheme.typography.displayLarge)
            Text("Your patterns at a glance.", color = MaterialTheme.colorScheme.tertiary)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Moments", state.totalMoments.toString(), Modifier.weight(1f))
                MetricCard("Current Streak", "${state.currentStreak}d", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                MetricCard("Longest", "${state.longestStreak}d", Modifier.weight(1f))
                MetricCard("Active (30d)", "${state.activeDaysLast30}", Modifier.weight(1f))
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Emotion Pie", style = MaterialTheme.typography.headlineMedium)
                    if (emotionItems.isEmpty()) {
                        Text("Capture more moments to generate emotion distribution.", color = MaterialTheme.colorScheme.tertiary)
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EmotionPieChart(
                                data = emotionItems.associate { it.key to it.value },
                                modifier = Modifier.size(170.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                val total = emotionItems.sumOf { it.value }.toFloat().coerceAtLeast(1f)
                                emotionItems.take(6).forEach { (emotion, count) ->
                                    EmotionLegend(
                                        emotion = emotion,
                                        count = count,
                                        ratio = (count / total * 100f).toInt()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Trend", style = MaterialTheme.typography.headlineMedium)
                    RangeSelector(
                        mode = rangeMode,
                        onModeChanged = { rangeMode = it }
                    )
                    TrendLineChart(
                        points = trendData.map { it.second },
                        labels = trendLabels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    )
                    BarChart(
                        points = trendData.map { it.second },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RangeSelector(mode: RangeMode, onModeChanged: (RangeMode) -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RangeChip(
                text = "7d",
                selected = mode == RangeMode.DAYS_7,
                onClick = { onModeChanged(RangeMode.DAYS_7) }
            )
            RangeChip(
                text = "30d",
                selected = mode == RangeMode.DAYS_30,
                onClick = { onModeChanged(RangeMode.DAYS_30) }
            )
        }
    }
}

@Composable
private fun RangeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.26f) else Color.Transparent
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun EmotionPieChart(data: Map<EmotionTag, Int>, modifier: Modifier = Modifier) {
    val total = data.values.sum().toFloat().coerceAtLeast(1f)
    Canvas(modifier = modifier) {
        var start = -90f
        data.entries.forEach { (emotion, count) ->
            val sweep = (count / total) * 360f
            drawArc(
                color = emotionColor(emotion),
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = size.minDimension * 0.22f, cap = StrokeCap.Butt),
                topLeft = Offset(0f, 0f),
                size = Size(size.width, size.height)
            )
            start += sweep
        }
    }
}

@Composable
private fun EmotionLegend(emotion: EmotionTag, count: Int, ratio: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape = CircleShape,
            color = emotionColor(emotion)
        ) {}
        Text("${emotion.displayName}: $count ($ratio%)", color = MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun TrendLineChart(points: List<Int>, labels: List<String>, modifier: Modifier = Modifier) {
    val safePoints = if (points.isEmpty()) listOf(0) else points
    val maxY = max(1, safePoints.maxOrNull() ?: 1).toFloat()
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val leftPad = 14.dp.toPx()
        val bottomPad = 20.dp.toPx()
        val topPad = 10.dp.toPx()
        val graphWidth = width - leftPad
        val graphHeight = height - bottomPad - topPad

        for (i in 0..3) {
            val y = topPad + (graphHeight / 3f) * i
            drawLine(
                color = Color.White.copy(alpha = 0.1f),
                start = Offset(leftPad, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (safePoints.size == 1) return@Canvas

        val stepX = graphWidth / (safePoints.size - 1).coerceAtLeast(1)
        val path = Path()
        safePoints.forEachIndexed { index, value ->
            val x = leftPad + stepX * index
            val y = topPad + graphHeight - (value / maxY) * graphHeight
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = primaryColor, style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round))
    }

    if (labels.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(label, color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun BarChart(points: List<Int>, modifier: Modifier = Modifier) {
    val safe = if (points.isEmpty()) listOf(0) else points
    val maxY = max(1, safe.maxOrNull() ?: 1).toFloat()
    val barColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val gap = 2.dp.toPx()
        val barCount = safe.size.coerceAtLeast(1)
        val barWidth = (width - (barCount - 1) * gap) / barCount
        safe.forEachIndexed { index, value ->
            val ratio = value / maxY
            val barHeight = ratio * (height - 4.dp.toPx())
            val x = index * (barWidth + gap)
            val y = height - barHeight
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = MaterialTheme.colorScheme.tertiary)
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

private fun emotionColor(emotion: EmotionTag): Color {
    return when (emotion) {
        EmotionTag.CALM -> Color(0xFF70D6FF)
        EmotionTag.HAPPY -> Color(0xFFFFD166)
        EmotionTag.SAD -> Color(0xFF5B8DEF)
        EmotionTag.ANGRY -> Color(0xFFFF5A5F)
        EmotionTag.TIRED -> Color(0xFF9E9E9E)
        EmotionTag.OVERWHELMED -> Color(0xFFB084F5)
        EmotionTag.GRATEFUL -> Color(0xFF8BC34A)
        EmotionTag.NEUTRAL -> Color(0xFFB0BEC5)
    }
}
