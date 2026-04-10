package com.exist.app.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exist.app.core.util.DateUtils
import com.exist.app.domain.model.DaySummary
import com.exist.app.domain.model.EmotionTag
import com.exist.app.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class AnalyticsUiState(
    val totalMoments: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val activeDaysLast30: Int = 0,
    val emotionCounts: Map<EmotionTag, Int> = emptyMap(),
    val dailyMomentsLast7: List<Pair<String, Int>> = emptyList(),
    val dailyMomentsLast30: List<Pair<String, Int>> = emptyList()
)

class AnalyticsViewModel(repository: MemoryRepository) : ViewModel() {
    val uiState: StateFlow<AnalyticsUiState> = repository.observeTimeline()
        .map { timeline -> timeline.toAnalytics() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AnalyticsUiState())
}

private fun List<DaySummary>.toAnalytics(): AnalyticsUiState {
    val today = LocalDate.parse(DateUtils.todayDayKey())
    val byDate = associateBy { LocalDate.parse(it.dayKey) }
    val allPhotos = flatMap { it.photos }
    val activeDates = filter { it.photos.isNotEmpty() }.map { LocalDate.parse(it.dayKey) }.toSet()

    var currentStreak = 0
    var cursor = today
    while (activeDates.contains(cursor)) {
        currentStreak += 1
        cursor = cursor.minusDays(1)
    }

    val sortedActive = activeDates.sorted()
    var longest = 0
    var run = 0
    var previous: LocalDate? = null
    for (date in sortedActive) {
        run = if (previous != null && previous!!.plusDays(1) == date) run + 1 else 1
        if (run > longest) longest = run
        previous = date
    }

    val last30 = (0L until 30L).count { offset ->
        byDate[today.minusDays(offset)]?.photos?.isNotEmpty() == true
    }

    val last7 = (6L downTo 0L).map { offset ->
        val date = today.minusDays(offset)
        val count = byDate[date]?.photos?.size ?: 0
        date.monthValue.toString().padStart(2, '0') + "/" +
            date.dayOfMonth.toString().padStart(2, '0') to count
    }

    val last30Series = (29L downTo 0L).map { offset ->
        val date = today.minusDays(offset)
        val count = byDate[date]?.photos?.size ?: 0
        date.monthValue.toString().padStart(2, '0') + "/" +
            date.dayOfMonth.toString().padStart(2, '0') to count
    }

    val emotionCounts = EmotionTag.entries.associateWith { tag ->
        allPhotos.count { it.emotionTag == tag }
    }.filterValues { it > 0 }

    return AnalyticsUiState(
        totalMoments = allPhotos.size,
        currentStreak = currentStreak,
        longestStreak = longest,
        activeDaysLast30 = last30,
        emotionCounts = emotionCounts,
        dailyMomentsLast7 = last7,
        dailyMomentsLast30 = last30Series
    )
}
