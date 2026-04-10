package com.exist.app.core.navigation

sealed class ExistRoute(val route: String) {
    data object Auth : ExistRoute("auth")
    data object Home : ExistRoute("home")
    data object Highlights : ExistRoute("highlights")
    data object Analytics : ExistRoute("analytics")
    data object Profile : ExistRoute("profile")
    data object Camera : ExistRoute("camera")
    data object Archive : ExistRoute("archive")
    data object Settings : ExistRoute("settings")
    data object DayDetail : ExistRoute("day/{dayKey}") {
        fun create(dayKey: String): String = "day/$dayKey"
    }
    data object Recap : ExistRoute("recap/{dayKey}") {
        fun create(dayKey: String): String = "recap/$dayKey"
    }
    data object MemoryEdit : ExistRoute("memory/{memoryId}") {
        fun create(memoryId: Long): String = "memory/$memoryId"
    }
}
