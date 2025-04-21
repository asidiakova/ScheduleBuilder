package com.example.schedulebuilder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.schedulebuilder.ui.event_edit.EventEditDestination
import com.example.schedulebuilder.ui.event_edit.EventEditScreen
import com.example.schedulebuilder.ui.event_entry.EventEntryDestination
import com.example.schedulebuilder.ui.event_entry.EventEntryScreen
import com.example.schedulebuilder.ui.schedule.ScheduleDestination
import com.example.schedulebuilder.ui.schedule.ScheduleScreen

@Composable
fun ScheduleNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = ScheduleDestination.route,
        modifier = modifier
    ) {
        composable(route = ScheduleDestination.route) {
            ScheduleScreen(
                editSchedule = { navController.navigate(EventEditDestination.route) },
                addEvent = { navController.navigate(EventEntryDestination.route) }
            )
        }

        composable(route = EventEntryDestination.route) {
            EventEntryScreen(
                navigateBack = { navController.navigate(ScheduleDestination.route) },
            )
        }

        composable(route = EventEditDestination.route) {
            EventEditScreen(
                navigateBack = { navController.navigate(ScheduleDestination.route) },
            )
        }
    }
}