package com.watertank.app

import android.app.Application
import com.watertank.app.data.local.AppDatabase
import com.watertank.app.data.preferences.PreferencesManager
import com.watertank.app.data.repository.ChlorineRepository
import com.watertank.app.data.repository.TankRepository
import com.watertank.app.notification.NotificationHelper

/**
 * Simple manual DI container. Replace with Hilt/Koin if the project grows.
 */
class WaterTankApplication : Application() {

    private val database by lazy { AppDatabase.get(this) }

    val prefs by lazy { PreferencesManager(this) }
    val tankRepo by lazy { TankRepository(database.tankDao(), database.readingDao()) }
    val chlorineRepo by lazy { ChlorineRepository(database.chlorineDao()) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        NotificationHelper.createChannels(this)
    }

    companion object {
        lateinit var instance: WaterTankApplication
            private set
    }
}
