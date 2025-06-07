package com.example.schedulebuilder.glance

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll

/**
 * Implementation of App Widget functionality.
 */
class AppWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = AppWidget()
}

suspend fun updateScheduleWidget(context: Context) {
    AppWidget().updateAll(context)
}

