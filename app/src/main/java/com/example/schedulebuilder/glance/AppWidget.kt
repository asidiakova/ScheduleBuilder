package com.example.schedulebuilder.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.schedulebuilder.R
import com.example.schedulebuilder.ScheduleBuilderApplication
import com.example.schedulebuilder.data.FullScheduleEvent
import com.example.schedulebuilder.data.Obligation
import com.example.schedulebuilder.data.ScheduleEventsRepositoryInterface
import kotlinx.coroutines.flow.first
import java.util.Locale

/**
 * Application widget that displays the schedule.
 */
class AppWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val scheduleRepository = getScheduleRepository(context)
        val events = scheduleRepository.getAllFullScheduleEventsStream().first()
        provideContent {
            GlanceTheme {
                VerticalListSchedule(events)
            }
        }
    }

    private fun getScheduleRepository(context: Context): ScheduleEventsRepositoryInterface {
        return (context.applicationContext as ScheduleBuilderApplication).container.scheduleEventsRepository
    }


    @Composable
    private fun VerticalListSchedule(events: List<FullScheduleEvent>) {
        val backgroundColor = Color.White
        val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

        val eventsByDay = weekdays.indices.map { dayIndex ->
            dayIndex to events.filter { it.scheduleEvent.day - 1 == dayIndex }
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(8.dp)
        ) {
            LazyColumn(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                items(eventsByDay.size) {
                    val dayIndex = eventsByDay[it].first
                    val dayEvents = eventsByDay[it].second

                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = weekdays[dayIndex], style = TextStyle(
                                fontSize = 12.sp, fontWeight = FontWeight.Bold
                            )
                        )

                        dayEvents.forEach { event ->

                            val cardColor = when (event.scheduleEvent.obligation) {
                                Obligation.P -> R.color.schedule_obligatory
                                Obligation.PV -> R.color.schedule_half_obligatory
                                Obligation.V -> R.color.schedule_selective
                            }

                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%-20s %-20s %-10s",
                                    event.scheduleEvent.subjectShortenedCode,
                                    String.format(
                                        Locale.US,
                                        "[%02d:00 - %02d:00]",
                                        event.scheduleEvent.startHour,
                                        event.scheduleEvent.endHour
                                    ),
                                    event.scheduleEvent.roomCode
                                ),
                                style = TextStyle(
                                    fontSize = 10.sp
                                ),
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .padding(2.dp)
                                    .background(cardColor)
                                    .cornerRadius(4.dp)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )

                        }
                        Spacer(modifier = GlanceModifier.height(16.dp))
                    }
                }
            }
        }
    }
}
