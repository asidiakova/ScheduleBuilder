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
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
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
    val openErrorDialog = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    EventEditDialog(
        onDismissRequest = {
        openDismissDialog.value = true
    },
        onRemoveEventRequest = {
            openRemoveEventDialog.value = true
        },
        onError = { openErrorDialog.value = true },
        navigateBack = navigateBack,
        viewModel = viewModel,
        coroutineScope = coroutineScope
    )

    if (openDismissDialog.value) {
        ConfirmationDialog(
            onDismissRequest = { openDismissDialog.value = false },
            onConfirmation = {
                openDismissDialog.value = false
                navigateBack()
            },
            dialogTitle = stringResource(R.string.discard_changes),
            dialogText = stringResource(R.string.are_you_sure_discard),
        )
    }

    if (openRemoveEventDialog.value) {
        ConfirmationDialog(
            onDismissRequest = { openRemoveEventDialog.value = false },
            onConfirmation = {
                openRemoveEventDialog.value = false
                try {
                    coroutineScope.launch {
                        viewModel.removeScheduleEvent()
                        navigateBack()
                    }
                    navigateBack()
                } catch (e: Exception) {
                    openErrorDialog.value = true
                }
            },
            dialogTitle = stringResource(R.string.remove_event),
            dialogText = stringResource(R.string.are_you_sure_remove_event),
        )
    }

    if (openErrorDialog.value) {
        ErrorDialog(
            onDismissRequest = { openErrorDialog.value = false },
            onConfirmation = {
                openErrorDialog.value = false
            },
            dialogTitle = stringResource(R.string.error_completing_request),
            dialogText = stringResource(R.string.try_again),
        )
    }
}


@Composable
fun EventEditDialog(
    onDismissRequest: () -> Unit,
    onRemoveEventRequest: () -> Unit,
    onError: () -> Unit,
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
                    try {
                        coroutineScope.launch {
                            viewModel.saveScheduleEventEdits()
                            navigateBack()
                        }
                    } catch (e: Exception) {
                        onError()
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
            TopAppBar(title = { Text(stringResource(R.string.edit_event)) }, navigationIcon = {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                }
            }, actions = {
                TextButton(
                    onClick = onSaveEvent, enabled = eventUiState.isEntryValid
                ) {
                    Text(stringResource(R.string.save))
                }
            })
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(id = R.dimen.padding_medium))
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

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.height_medium)))
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
                Icon(icon, contentDescription = stringResource(R.string.dialog_icon))
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
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }) {
                Text(stringResource(R.string.dismiss))
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true),
    )
}

@Composable
fun ErrorDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
) {
    AlertDialog(
        icon = {
            Icon(Icons.Default.Warning, contentDescription = stringResource(R.string.dialog_icon))

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
                Text(stringResource(R.string.ok))
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

    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.height_medium)))

    SubjectSelection(
        predefinedSubjects = filteredSubjectsState.subjectsList,
        scheduleEventDetails = eventUiState.scheduleEventDetails,
        onValueChange = onScheduleEventValueChange,
        onQueryChange = viewModel::updateSubjectQuery,
    )

    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.height_medium)))

    TeacherSelection(
        teachersList = filteredTeachersState.teachersList,
        scheduleEventDetails = eventUiState.scheduleEventDetails,
        onValueChange = onScheduleEventValueChange,
        onQueryChange = viewModel::updateTeacherQuery
    )

    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.height_medium)))

    LocationSelection(
        locationsList = filteredLocationsState.locationsList,
        scheduleEventDetails = eventUiState.scheduleEventDetails,
        onValueChange = onScheduleEventValueChange,
        onQueryChange = viewModel::updateLocationQuery
    )

    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.height_medium)))

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

    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.height_medium)))

    RadioButtonObligationSelection(
        onValueChange = onScheduleEventValueChange,
        scheduleEventDetails = eventUiState.scheduleEventDetails
    )

    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.height_large)))

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
        Text(stringResource(R.string.remove_event))
    }
}