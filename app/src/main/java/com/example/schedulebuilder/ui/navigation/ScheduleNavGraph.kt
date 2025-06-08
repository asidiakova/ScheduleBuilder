package com.example.schedulebuilder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
                addEvent = { navController.navigate(EventEntryDestination.route) },
                onClickEdit = { navController.navigate("${EventEditDestination.route}/${it}") })
        }

        composable(route = EventEntryDestination.route) {
            EventEntryScreen(
                navigateBack = { navController.navigate(ScheduleDestination.route) },
            )
        }

        composable(
            route = EventEditDestination.routeWithArgs,
            arguments = listOf(navArgument(EventEditDestination.EVENT_ID_ARG) {
                type = NavType.IntType
            })
        ) {
            EventEditScreen(
                navigateBack = { navController.navigate(ScheduleDestination.route) },
            )
        }
    }
}