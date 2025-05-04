@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.schedulebuilder.ui.event_edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedulebuilder.R
import com.example.schedulebuilder.ui.AppViewModelProvider
import com.example.schedulebuilder.ui.navigation.NavDestination
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.schedulebuilder.ui.event_entry.DaySelection
import com.example.schedulebuilder.ui.event_entry.LocationSelection
import com.example.schedulebuilder.ui.event_entry.RadioButtonObligationSelection
import com.example.schedulebuilder.ui.event_entry.ScheduleEventDetails
import com.example.schedulebuilder.ui.event_entry.ScheduleEventUiState
import com.example.schedulebuilder.ui.event_entry.SubjectSelection
import com.example.schedulebuilder.ui.event_entry.TeacherSelection
import com.example.schedulebuilder.ui.event_entry.TimeSelection
import kotlinx.coroutines.CoroutineScope

object EventEditDestination : NavDestination {
    override val route = "edit_schedule_event"
    override val titleRes = R.string.app_name
    const val eventIdArg = "eventId"
    val routeWithArgs = "$route/{$eventIdArg}"
}


@Composable
fun EventEditScreen(
    navigateBack: () -> Unit,
    viewModel: EventEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val openDismissDialog = remember { mutableStateOf(false) }
    val openRemoveEventDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    EventEditDialog(
        onDismissRequest = {
            openDismissDialog.value = true
        }, onRemoveEventRequest = {
            openRemoveEventDialog.value = true
        }, navigateBack = navigateBack, viewModel = viewModel, coroutineScope = coroutineScope
    )

    if (openDismissDialog.value) {
        ConfirmationDialog(
            onDismissRequest = { openDismissDialog.value = false },
            onConfirmation = {
                openDismissDialog.value = false
                navigateBack()
            },
            dialogTitle = "Discard changes?",
            dialogText = "Are you sure you want to discard your changes and close the window?",
        )
    }

    if (openRemoveEventDialog.value) {
        ConfirmationDialog(
            onDismissRequest = { openRemoveEventDialog.value = false },
            onConfirmation = {
                openRemoveEventDialog.value = false
                coroutineScope.launch {
                    viewModel.removeScheduleEvent()
                    navigateBack()
                }
                navigateBack()
            },
            dialogTitle = "Remove event?",
            dialogText = "Are you sure you want to remove this event?",
        )
    }
}


@Composable
fun EventEditDialog(
    onDismissRequest: () -> Unit,
    onRemoveEventRequest: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: EventEditViewModel = viewModel(factory = AppViewModelProvider.Factory),
    coroutineScope: CoroutineScope
) {
    val eventUiState = viewModel.scheduleEventUiState

    Dialog(
        onDismissRequest = onDismissRequest, properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            EventEditDialogContent(
                navigateBack = onDismissRequest,
                eventUiState = eventUiState,
                onUpdateUiState = viewModel::updateUiState,
                onSaveEvent = {
                    coroutineScope.launch {
                        viewModel.saveScheduleEventEdits()
                        navigateBack()
                    }
                },
                onRemoveEvent = onRemoveEventRequest,
                viewModel = viewModel
            )
        }
    }
}


@Composable
fun EventEditDialogContent(
    navigateBack: () -> Unit,
    eventUiState: ScheduleEventUiState,
    onUpdateUiState: (ScheduleEventDetails) -> Unit,
    onSaveEvent: () -> Unit,
    onRemoveEvent: () -> Unit,
    viewModel: EventEditViewModel
) {
    val scrollState = rememberScrollState()

    val filteredSubjectsState by viewModel.filteredSubjectsState.collectAsState()
    val filteredTeachersState by viewModel.filteredTeachersState.collectAsState()
    val filteredLocationsState by viewModel.filteredLocationsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Subject") }, navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }, actions = {
                TextButton(
                    onClick = onSaveEvent, enabled = eventUiState.isEntryValid
                ) {
                    Text("Save")
                }
            })
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            ScheduleEventEntryForm(
                filteredSubjectsState = filteredSubjectsState,
                filteredTeachersState = filteredTeachersState,
                filteredLocationsState = filteredLocationsState,
                eventUiState = eventUiState,
                onScheduleEventValueChange = onUpdateUiState,
                onRemoveEvent = onRemoveEvent,
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector? = null
) {
    AlertDialog(
        icon = {
            if (icon != null) {
                Icon(icon, contentDescription = "Dialog Icon")
            }
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }) {
                Text("Dismiss")
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true),
    )
}


@Composable
fun ScheduleEventEntryForm(
    viewModel: EventEditViewModel,
    filteredSubjectsState: com.example.schedulebuilder.ui.event_entry.PredefinedSubjectsState,
    filteredTeachersState: com.example.schedulebuilder.ui.event_entry.TeachersListState,
    filteredLocationsState: com.example.schedulebuilder.ui.event_entry.LocationsListState,
    eventUiState: ScheduleEventUiState,
    onScheduleEventValueChange: (ScheduleEventDetails) -> Unit,
    onRemoveEvent: () -> Unit
) {

    Spacer(modifier = Modifier.height(16.dp))

    SubjectSelection(
        predefinedSubjects = filteredSubjectsState.subjectsList,
        scheduleEventDetails = eventUiState.scheduleEventDetails,
        onValueChange = onScheduleEventValueChange,
        onQueryChange = viewModel::updateSubjectQuery,
    )

    Spacer(modifier = Modifier.height(16.dp))

    TeacherSelection(
        teachersList = filteredTeachersState.teachersList,
        scheduleEventDetails = eventUiState.scheduleEventDetails,
        onValueChange = onScheduleEventValueChange,
        onQueryChange = viewModel::updateTeacherQuery
    )

    Spacer(modifier = Modifier.height(16.dp))

    LocationSelection(
        locationsList = filteredLocationsState.locationsList,
        scheduleEventDetails = eventUiState.scheduleEventDetails,
        onValueChange = onScheduleEventValueChange,
        onQueryChange = viewModel::updateLocationQuery
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        DaySelection(
            scheduleEventDetails = eventUiState.scheduleEventDetails,
            onValueChange = onScheduleEventValueChange,
            modifier = Modifier.weight(1f)
        )
    }
    TimeSelection(
        scheduleEventDetails = eventUiState.scheduleEventDetails,
        onValueChange = onScheduleEventValueChange,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    RadioButtonObligationSelection(
        onValueChange = onScheduleEventValueChange,
        scheduleEventDetails = eventUiState.scheduleEventDetails
    )

    Spacer(modifier = Modifier.height(24.dp))

    RemoveEventButton(onRemoveEvent = onRemoveEvent)

}


@Composable
fun RemoveEventButton(
    onRemoveEvent: () -> Unit
) {
    TextButton(
        colors = ButtonColors(
            containerColor = Color.Red,
            contentColor = Color.Black,
            disabledContainerColor = Color.Red,
            disabledContentColor = Color.Black
        ),
        onClick = onRemoveEvent,
    ) {
        Text("Remove event")
    }
}