package com.smarttracker.app

import android.app.Application
import com.smarttracker.app.data.local.AppDatabase

class SmartExpenseApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: SmartExpenseApp
            private set
    }
}
