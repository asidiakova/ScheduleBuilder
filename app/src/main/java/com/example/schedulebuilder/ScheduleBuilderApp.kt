package com.example.schedulebuilder

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.schedulebuilder.ui.navigation.ScheduleNavHost

@Composable
fun ScheduleBuilderApp(navController: NavHostController = rememberNavController()) {
    ScheduleNavHost(navController = navController)
}
