@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.schedulebuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.schedulebuilder.data.FullScheduleEvent
import com.example.schedulebuilder.data.Location
import com.example.schedulebuilder.ui.theme.ScheduleBuilderTheme
import com.example.schedulebuilder.data.Obligation
import com.example.schedulebuilder.data.ScheduleEvent
import com.example.schedulebuilder.data.Subject
import com.example.schedulebuilder.data.Teacher
import com.example.schedulebuilder.ui.schedule.ScheduleScreen



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ScheduleBuilderApp()
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainPreview() {
    ScheduleBuilderTheme {
        ScheduleBuilderApp()
    }
}


