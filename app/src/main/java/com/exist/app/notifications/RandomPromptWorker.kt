package com.exist.app.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.exist.app.MainActivity
import com.exist.app.R
import com.exist.app.data.preferences.SettingsPreferences
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import kotlin.random.Random

class RandomPromptWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val settings = SettingsPreferences(context).settings.first()
        if (!settings.randomPromptEnabled) return Result.success()

        val canNotify = android.os.Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        if (canNotify) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_START_ROUTE, MainActivity.ROUTE_CAMERA)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                1101,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val lines = listOf(
                "Right now counts.",
                "Proof of today?",
                "This moment won't happen again."
            )
            val line = lines.random()

            val notification = NotificationCompat.Builder(context, RandomPromptScheduler.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(line)
                .setStyle(NotificationCompat.BigTextStyle().bigText(line))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(1101, notification)
        }

        RandomPromptScheduler.scheduleNext(context)
        return Result.success()
    }
}

object RandomPromptScheduler {
    const val CHANNEL_ID = "exist_prompt_channel"
    private const val UNIQUE_WORK = "exist_random_prompt"

    fun sync(context: Context, enabled: Boolean) {
        if (enabled) scheduleNext(context) else WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK)
    }

    fun scheduleNext(context: Context) {
        val request = OneTimeWorkRequestBuilder<RandomPromptWorker>()
            .setInitialDelay(randomDelay())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun randomDelay(): Duration {
        val now = LocalDateTime.now()
        val todayStart = now.withHour(9).withMinute(0).withSecond(0).withNano(0)
        val baseDay = if (now.isAfter(todayStart)) now.plusDays(1) else now

        val target = baseDay
            .withHour(Random.nextInt(9, 22))
            .withMinute(Random.nextInt(0, 60))
            .withSecond(0)
            .withNano(0)

        return Duration.between(now, target).coerceAtLeast(Duration.ofMinutes(5))
    }
}
