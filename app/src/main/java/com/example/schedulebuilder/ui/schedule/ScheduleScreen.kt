@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.schedulebuilder.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ButtonColors
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedulebuilder.R
import com.example.schedulebuilder.data.FullScheduleEvent
import com.example.schedulebuilder.data.Obligation
import com.example.schedulebuilder.ui.AppViewModelProvider
import com.example.schedulebuilder.ui.event_edit.ConfirmationDialog
import com.example.schedulebuilder.ui.navigation.NavDestination
import com.example.schedulebuilder.ui.theme.LightGray
import com.example.schedulebuilder.ui.theme.ScheduleEmptyCell
import com.example.schedulebuilder.ui.theme.ScheduleHalfObligatory
import com.example.schedulebuilder.ui.theme.ScheduleObligatory
import com.example.schedulebuilder.ui.theme.ScheduleSelective
import com.example.schedulebuilder.ui.theme.Typography
import com.example.schedulebuilder.ui.theme.UnizaDark
import com.example.schedulebuilder.ui.theme.UnizaLight
import com.example.schedulebuilder.ui.theme.UnizaLightAccent
import com.example.schedulebuilder.ui.theme.WarmBlack
import kotlinx.coroutines.launch

/**
 * Navigation destination for the schedule screen.
 */
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
                    containerColor = UnizaDark, titleContentColor = UnizaLight
                ),
                title = {
                    Text(
                        text = stringResource(R.string.my_schedule),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.padding_medium))
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            BottomAppBar(containerColor = UnizaLight, actions = {
                IconButton(onClick = { showBottomSheet = true }) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = stringResource(R.string.icon_description),
                    )
                }
            }, floatingActionButton = {
                FloatingActionButton(
                    onClick = addEvent,
                    containerColor = UnizaLightAccent,
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
                events = scheduleUiState.eventsList, onEventClick = {
                    selectedEvent = it
                }, modifier = Modifier.fillMaxWidth()
            )
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
    modifier: Modifier = Modifier,
    events: List<FullScheduleEvent> = emptyList<FullScheduleEvent>(),
    onEventClick: (FullScheduleEvent) -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        ScheduleTable(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(id = R.dimen.padding_small))
                .verticalScroll(rememberScrollState()), events = events, onEventClick = onEventClick
        )
    }
}

@Composable
fun ScheduleTable(
    modifier: Modifier = Modifier,
    events: List<FullScheduleEvent> = emptyList<FullScheduleEvent>(),
    onEventClick: (FullScheduleEvent) -> Unit
) {
    val timeslots = (START_HOUR..END_HOUR).map { "$it:00" }
    val weekdays = listOf(
        stringResource(R.string.mon),
        stringResource(R.string.tue),
        stringResource(R.string.wed),
        stringResource(R.string.thu),
        stringResource(R.string.fri)
    )

    Column(
        modifier = modifier.border(dimensionResource(id = R.dimen.border_full), LightGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen.table_header_height))
        ) {
            Box(
                modifier = Modifier
                    .width(dimensionResource(id = R.dimen.day_label_width))
                    .border(dimensionResource(id = R.dimen.border_half), LightGray)
            )

            timeslots.forEach { time ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .border(dimensionResource(id = R.dimen.border_half), LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = time,
                        fontSize = 9.sp,
                        color = WarmBlack,
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
                        .border(dimensionResource(id = R.dimen.border_half), LightGray)
                ) {
                    Box(
                        modifier = Modifier
                            .width(dimensionResource(id = R.dimen.day_label_width))
                            .fillMaxSize()
                            .border(dimensionResource(id = R.dimen.border_half), LightGray),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .rotate(270f)
                                .fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = day,
                                fontSize = 9.sp,
                                maxLines = 1,
                                fontWeight = FontWeight.Bold,
                                softWrap = false,
                            )
                        }
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
                                            dimensionResource(id = R.dimen.border_half), LightGray
                                        )
                                        .background(ScheduleEmptyCell)
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
            containerColor = when (event.scheduleEvent.obligation) {
                Obligation.P -> ScheduleObligatory
                Obligation.PV -> ScheduleHalfObligatory
                Obligation.V -> ScheduleSelective
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen.card_elevation)),
        onClick = {
            onClick(event)
        }) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(id = R.dimen.padding_extra_small)),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = event.teacher.teacherName,
                style = Typography.displaySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = event.location.roomCode,
                style = Typography.displaySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = event.subject.shortenedCode,
                style = Typography.displaySmall,
                fontWeight = FontWeight.SemiBold,
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
                containerColor = when (event.scheduleEvent.obligation) {
                    Obligation.P -> ScheduleObligatory
                    Obligation.PV -> ScheduleHalfObligatory
                    Obligation.V -> ScheduleSelective
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_small)),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = event.subject.fullDisplayName,
                    style = Typography.headlineMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.event_details_spacer)))

                Text(
                    text = event.location.roomCode,
                    style = Typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = weekdays[event.scheduleEvent.day - 1] + " ${event.scheduleEvent.startHour}:00 - ${event.scheduleEvent.endHour}:00",
                    style = Typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.teacher.teacherName,
                    style = Typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
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
                        colors = ButtonColors(
                            contentColor = UnizaDark,
                            containerColor = Color.Transparent,
                            disabledContentColor = UnizaDark,
                            disabledContainerColor = Color.Transparent
                        ),
                        onClick = {
                            onClickEdit(event.scheduleEvent.id)
                        },
                        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_small)),
                    ) {
                        Text(stringResource(R.string.edit_event), fontWeight = FontWeight.Bold)
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
                    UnizaLightAccent,
                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.bottom_sheet_rounded_corner))
                )
                .padding(dimensionResource(id = R.dimen.padding_small)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon, contentDescription = label, tint = UnizaDark
            )
        }
        Text(
            text = label,
            color = UnizaDark,
            style = Typography.bodyLarge,
            modifier = Modifier.padding(start = dimensionResource(id = R.dimen.padding_medium))
        )
    }
}
