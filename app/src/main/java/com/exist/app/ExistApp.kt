package com.exist.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.exist.app.core.AppContainer
import com.exist.app.notifications.RandomPromptScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ExistApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        createNotificationChannel()

        CoroutineScope(Dispatchers.IO).launch {
            container.memoryRepository.deleteExpiredPhotos()
            val settings = container.memoryRepository.settings.first()
            RandomPromptScheduler.sync(this@ExistApp, settings.randomPromptEnabled)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RandomPromptScheduler.CHANNEL_ID,
                "Exist prompts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Poetic reminders to capture proof of your day"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
