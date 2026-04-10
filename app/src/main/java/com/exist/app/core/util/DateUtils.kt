package com.exist.app.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateUtils {
    private val dayKeyFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val displayFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")

    fun nowMillis(): Long = System.currentTimeMillis()

    fun todayDayKey(zoneId: ZoneId = ZoneId.systemDefault()): String {
        return LocalDate.now(zoneId).format(dayKeyFormatter)
    }

    fun dayKeyFromMillis(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        return Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate().format(dayKeyFormatter)
    }

    fun parseDayKey(dayKey: String): LocalDate = LocalDate.parse(dayKey, dayKeyFormatter)

    fun displayDate(dayKey: String): String {
        return parseDayKey(dayKey).format(displayFormatter)
    }

    fun displayTime(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val localTime = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalTime().withSecond(0).withNano(0)
        return localTime.toString()
    }
}
