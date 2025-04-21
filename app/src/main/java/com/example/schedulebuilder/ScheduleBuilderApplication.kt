package com.example.schedulebuilder

import android.app.Application
import com.example.schedulebuilder.data.AppContainer
import com.example.schedulebuilder.data.AppDataContainer

class ScheduleBuilderApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}