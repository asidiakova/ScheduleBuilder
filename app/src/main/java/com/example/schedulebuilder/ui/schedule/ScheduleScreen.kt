@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.schedulebuilder.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.glance.text.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedulebuilder.R
import com.example.schedulebuilder.data.FullScheduleEvent
import com.example.schedulebuilder.data.Location
import com.example.schedulebuilder.data.Obligation
import com.example.schedulebuilder.data.ScheduleEvent
import com.example.schedulebuilder.data.Subject
import com.example.schedulebuilder.data.Teacher
import com.example.schedulebuilder.ui.AppViewModelProvider
import com.example.schedulebuilder.ui.event_edit.ConfirmationDialog
import com.example.schedulebuilder.ui.navigation.NavDestination
import kotlinx.coroutines.launch


object ScheduleDestination : NavDestination {
    override val route = "schedule"
}

const val START_HOUR = 7
const val END_HOUR = 19

@Composable
fun ScheduleScreen(
    addEvent: () -> Unit,
    onClickEdit: (Int) -> Unit,
    viewModel: ScheduleScreenViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {

    val scheduleUiState by viewModel.scheduleUiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val sheetState = rememberModalBottomSheetState()

    var selectedEvent by remember { mutableStateOf<FullScheduleEvent?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val openAlertDialog = remember { mutableStateOf(false) }

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
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = colorResource(R.color.uniza_light), actions = {
                IconButton(onClick = { showBottomSheet = true }) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.icon_description),
                    )
                }
            }, floatingActionButton = {
                FloatingActionButton(
                    onClick = addEvent,
                    containerColor = colorResource(R.color.uniza_light_accent),
                    elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                ) {
                    Icon(Icons.Filled.Add, stringResource(R.string.icon_description))
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
                events = scheduleUiState.eventsList,
                modifier = Modifier.fillMaxWidth(),
                onEventClick = {
                    selectedEvent = it
                })
            selectedEvent?.let { event ->
                EventDetailsDialog(
                    event = event,
                    onDismissRequest = { selectedEvent = null },
                    onClickEdit = {
                        onClickEdit(event.scheduleEvent.id)
                        selectedEvent = null
                    })
            }
        }
    }

    if (showBottomSheet) {
        EditScheduleBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            onAddEvent = addEvent,
            onClearSchedule = {
                openAlertDialog.value = true
            },
        )
    }

    if (openAlertDialog.value) {
        ConfirmationDialog(
            onDismissRequest = { openAlertDialog.value = false },
            onConfirmation = {
                openAlertDialog.value = false
                coroutineScope.launch {
                    viewModel.clearSchedule()
                    showBottomSheet = false
                }
            },
            dialogTitle = stringResource(R.string.clear_schedule),
            dialogText = stringResource(R.string.are_you_sure_delete_all),
            icon = Icons.Default.Warning
        )
    }
}


@Composable
fun CenteredScheduleGrid(
    events: List<FullScheduleEvent> = emptyList<FullScheduleEvent>(),
    modifier: Modifier,
    onEventClick: (FullScheduleEvent) -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        ScheduleTable(
            modifier = Modifier.fillMaxWidth(0.98f).verticalScroll(rememberScrollState()), events = events, onEventClick = onEventClick
        )
    }
}

@Composable
fun ScheduleTable(
    events: List<FullScheduleEvent> = emptyList<FullScheduleEvent>(),
    modifier: Modifier,
    onEventClick: (FullScheduleEvent) -> Unit
) {
    val timeslots = (START_HOUR..END_HOUR).map { "$it:00" }
    val weekdays = listOf(
        stringResource(R.string.monday),
        stringResource(R.string.tuesday),
        stringResource(R.string.wednesday),
        stringResource(R.string.thursday),
        stringResource(R.string.friday)
    )

    Column(
        modifier = modifier.border(dimensionResource(id = R.dimen.border_full), Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.table_header_height))
        ) {
            Box(
                modifier = Modifier
                    .width(dimensionResource(id = R.dimen.day_label_width))
                    .border(dimensionResource(id = R.dimen.border_half), Color.LightGray)
            )

            timeslots.forEach { time ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(dimensionResource(id = R.dimen.border_half), Color.LightGray),
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
                    .height(dimensionResource(id = R.dimen.table_row_height))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(dimensionResource(id = R.dimen.border_half), Color.LightGray)
                ) {
                    Box(
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.day_label_width))
                            .fillMaxSize()
                            .border(dimensionResource(id = R.dimen.border_half), Color.LightGray),
                        contentAlignment = Alignment.Center
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
                                        .border(
                                            dimensionResource(id = R.dimen.border_half),
                                            Color.LightGray
                                        )
                                        .background(colorResource(id = R.color.schedule_empty_cell))
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
                                            .padding(dimensionResource(id = R.dimen.padding_mini)),
                                        onClick = { onEventClick(event) })

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
    event: FullScheduleEvent,
    modifier: Modifier = Modifier,
    onClick: (FullScheduleEvent) -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = colorResource(
                when (event.scheduleEvent.obligation) {
                    Obligation.P -> R.color.schedule_obligatory
                    Obligation.PV -> R.color.schedule_half_obligatory
                    Obligation.V -> R.color.schedule_selective
                }
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.card_elevation)),
        onClick = {
            onClick(event)
        }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.padding_extra_small)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
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
                text = event.subject.shortenedCode,
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
    onClickEdit: (Int) -> Unit,
) {
    val weekdays = listOf(
        stringResource(R.string.monday),
        stringResource(R.string.tuesday),
        stringResource(R.string.wednesday),
        stringResource(R.string.thursday),
        stringResource(R.string.friday)
    )

    Dialog(
        onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.event_details_height))
                .padding(dimensionResource(id = R.dimen.padding_medium)),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.rounded_corner_shape)),
            colors = CardDefaults.cardColors(
                containerColor = colorResource(
                    when (event.scheduleEvent.obligation) {
                        Obligation.P -> R.color.schedule_obligatory
                        Obligation.PV -> R.color.schedule_half_obligatory
                        Obligation.V -> R.color.schedule_selective
                    }
                )
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = event.subject.fullDisplayName,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = event.location.roomCode,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = weekdays[event.scheduleEvent.day - 1] + " ${event.scheduleEvent.startHour}:00 - ${event.scheduleEvent.endHour}:00",
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.teacher.teacherName,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.event_details_spacer)))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    TextButton(
                        onClick = {
                            onClickEdit(event.scheduleEvent.id)
                        },
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
                    ) {
                        Text(stringResource(R.string.edit_event))
                    }
                }
            }
        }
    }
}

@Composable
fun EditScheduleBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    onAddEvent: () -> Unit,
    onClearSchedule: () -> Unit,
) {

    ModalBottomSheet(
        onDismissRequest = onDismissRequest, sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.padding_large)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            BottomSheetActionItem(
                icon = Icons.Default.Add,
                label = stringResource(R.string.add_event),
                onClick = onAddEvent
            )

            BottomSheetActionItem(
                icon = Icons.Default.Delete,
                label = stringResource(R.string.clear_schedule),
                onClick = onClearSchedule
            )

            BottomSheetActionItem(
                icon = Icons.Default.Close,
                label = stringResource(R.string.cancel),
                onClick = onDismissRequest
            )
        }
    }
}

@Composable
fun BottomSheetActionItem(
    icon: ImageVector, label: String, onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(vertical = dimensionResource(id = R.dimen.padding_extra_small))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(dimensionResource(id = R.dimen.bottom_sheet_box_size))
                .background(
                    Color(0xFFDDE3FF),
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.bottom_sheet_rounded_corner))
                )
                .padding(dimensionResource(id = R.dimen.padding_small)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon, contentDescription = label, tint = Color(0xFF2F3E7C)
            )
        }
        Text(
            text = label,
            color = Color(0xFF2F3E7C),
            fontSize = 16.sp,
            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
        )
    }
}

@Preview
@Composable
fun EventDetailsDialogPreview() {
    EventDetailsDialog(
        event = FullScheduleEvent(
        scheduleEvent = ScheduleEvent(
            id = 1,
            day = 1,
            startHour = 8,
            endHour = 10,
            teacherName = "John Doe",
            roomCode = "A101",
            subjectShortenedCode = "Vyvoj aplikacci pre mobilne zariadenie",
            obligation = Obligation.P
        ), subject = Subject(
            shortenedCode = "MAT101", fullDisplayName = "Vyvoj aplikacci pre mobilne zariadenie wefubewf weofnsdf wfuksdf"
        ), teacher = Teacher(
            teacherName = "John Doe"
        ), location = Location(
            roomCode = "A101"
        )
    ), onDismissRequest = {}, onClickEdit = {})
}
