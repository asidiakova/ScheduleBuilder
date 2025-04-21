@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.schedulebuilder.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedulebuilder.R
import com.example.schedulebuilder.data.FullScheduleEvent
import com.example.schedulebuilder.data.Obligation
import com.example.schedulebuilder.ui.AppViewModelProvider
import com.example.schedulebuilder.ui.event_entry.AlertDialogExample
import com.example.schedulebuilder.ui.navigation.NavDestination

//TODO: animations when click on event https://m3.material.io/styles/motion/transitions/transition-patterns#b67cba74-6240-4663-a423-d537b6d21187

object ScheduleDestination : NavDestination {
    override val route = "schedule"
    override val titleRes = R.string.app_name
}

val START_HOUR = 7
val TIMESLOTS_COUNT = 13

@Composable
fun ScheduleScreen(
    editSchedule: () -> Unit,
    addEvent: () -> Unit,
    viewModel: ScheduleScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {

    var presses by remember { mutableIntStateOf(0) }

    val scheduleUiState by viewModel.scheduleUiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    var selectedEvent by remember { mutableStateOf<FullScheduleEvent?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(R.color.uniza_dark),
                    titleContentColor = colorResource(R.color.uniza_light)
                ),
                title = {
                    Text(
                        text = stringResource(R.string.my_schedule),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                actions = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            tint = colorResource(R.color.uniza_light),
                            contentDescription = "Localized description",
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = colorResource(R.color.uniza_light), actions = {
                IconButton(onClick = editSchedule) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Localized description",
                    )
                }
            }, floatingActionButton = {
                FloatingActionButton(
                    onClick = addEvent,
                    containerColor = colorResource(R.color.uniza_light_accent),
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(Icons.Filled.Add, "Localized description")
                }
            }

            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CenteredScheduleGrid(
                events = scheduleUiState.eventsList, modifier = Modifier.fillMaxWidth(), onEventClick = { selectedEvent = it }
            )
            selectedEvent?.let { event ->
                EventDetailsDialog(
                    event = event,
                    onDismissRequest = { selectedEvent = null },
                    onConfirmation = { selectedEvent = null }
                )
            }
        }
    }



}


@Composable
fun CenteredScheduleGrid(
    events: List<FullScheduleEvent> = emptyList<FullScheduleEvent>(), modifier: Modifier,
    onEventClick: (FullScheduleEvent) -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        ScheduleTable(
            modifier = Modifier.fillMaxWidth(0.98f), events = events, onEventClick = {onEventClick}
        )
    }


}

@Composable
fun ScheduleTable(
    events: List<FullScheduleEvent> = emptyList<FullScheduleEvent>(), modifier: Modifier,
    onEventClick: (FullScheduleEvent) -> Unit
) {
    val timeslots = (7..19).map { "$it:00" }
    val weekdays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val headerHeight = 30.dp
    val rowHeight = 80.dp
    val dayLabelWidth = 20.dp




    Column(
        modifier = modifier.border(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
        ) {
            Box(
                modifier = Modifier
                    .width(dayLabelWidth)
                    .border(0.5.dp, Color.LightGray)
            )

            timeslots.forEach { time ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(0.5.dp, Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = time,
                        fontSize = 10.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        weekdays.forEachIndexed { dayIndex, day ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(0.5.dp, Color.LightGray)
                ) {
                    Box(
                        modifier = Modifier
                            .width(dayLabelWidth)
                            .fillMaxSize()
                            .border(0.5.dp, Color.LightGray), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            maxLines = 1,
                            color = Color.Black,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.rotate(270f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(timeslots.size.toFloat())
                            .fillMaxHeight()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            repeat(timeslots.size) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .border(0.5.dp, Color.LightGray)
                                        .background(Color(0xFFF0F0F0))
                                )
                            }
                        }

                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val cellWidthPx =
                                with(LocalDensity.current) { (maxWidth / timeslots.size).toPx() }


                            events.filter { it.scheduleEvent.day - 1 == dayIndex }
                                .forEach { event ->
                                    val startOffsetPx =
                                        (event.scheduleEvent.startHour - START_HOUR) * cellWidthPx
                                    val eventWidthPx =
                                        (event.scheduleEvent.endHour - event.scheduleEvent.startHour) * cellWidthPx


                                    EventCard(
                                        event = event,
                                        modifier = Modifier
                                            .offset {
                                                IntOffset(
                                                    startOffsetPx.toInt(), 0
                                                )
                                            }
                                            .width(with(LocalDensity.current) { eventWidthPx.toDp() })
                                            .fillMaxHeight()
                                            .padding(1.dp),
                                        onclick = {onEventClick(event)}
                                    )

                                }
                        }


                    }
                }
            }
        }
    }
}

@Composable
fun BoxWithConstraintsScope.EventCard(
    event: FullScheduleEvent, modifier: Modifier = Modifier, onclick: (FullScheduleEvent) -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxSize(), colors = CardDefaults.cardColors(
            containerColor = colorResource(
                when (event.scheduleEvent.obligation) {
                    Obligation.P -> R.color.schedule_obligatory
                    Obligation.PV -> R.color.schedule_half_obligatory
                    Obligation.V -> R.color.schedule_selective
                }
            )
        ), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = {
            onclick
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = event.scheduleEvent.obligation.name,
                fontSize = 8.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = event.teacher.teacherName,
                fontSize = 8.sp,
                lineHeight = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = event.location.roomCode,
                fontSize = 8.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = event.subject.fullDisplayName,
                fontSize = 8.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EventDetailsDialog(
    event: FullScheduleEvent,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = event.scheduleEvent.obligation.name,
                    fontSize = 8.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.teacher.teacherName,
                    fontSize = 8.sp,
                    lineHeight = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.location.roomCode,
                    fontSize = 8.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.subject.fullDisplayName,
                    fontSize = 8.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = { onConfirmation() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
