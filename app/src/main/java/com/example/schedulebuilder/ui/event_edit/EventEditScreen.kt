@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.schedulebuilder.ui.event_edit


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.schedulebuilder.R
import com.example.schedulebuilder.data.Location
import com.example.schedulebuilder.data.Obligation
import com.example.schedulebuilder.data.Subject
import com.example.schedulebuilder.data.Teacher
import com.example.schedulebuilder.ui.AppViewModelProvider
import com.example.schedulebuilder.ui.navigation.NavDestination
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
        },
        onRemoveEventRequest = {
            openRemoveEventDialog.value = true
        },
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
    val predefinedSubjectsState by viewModel.predefinedSubjectsState.collectAsState()
    val teachersListState by viewModel.teachersListState.collectAsState()
    val locationsListState by viewModel.locationsListState.collectAsState()
    val eventUiState = viewModel.scheduleEventUiState

    val openSanityCheckDialog = remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
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
                predefinedSubjectsState = predefinedSubjectsState,
                teachersListState = teachersListState,
                locationsListState = locationsListState,
                eventUiState = eventUiState,
                onUpdateUiState = viewModel::updateUiState,
                onSaveEvent = {
                    coroutineScope.launch {
                        val success = viewModel.saveScheduleEventEdits()
                        if (success)
                        {
                            navigateBack()
                        }
                        else {
                            openSanityCheckDialog.value = true
                        }
                    }
                },
                onRemoveEvent = onRemoveEventRequest
            )
        }

        if (openSanityCheckDialog.value) {
            ConfirmationDialog(
                onDismissRequest = { openSanityCheckDialog.value = false },
                onConfirmation = {
                    openSanityCheckDialog.value = false
                },
                dialogTitle = "Entry is invalid",
                dialogText = "Please make sure you have filled out all the necessary fields",
            )
        }
    }
}

@Composable
fun EventEditDialogContent(
    navigateBack: () -> Unit,
    predefinedSubjectsState: PredefinedSubjectsState,
    teachersListState: TeachersListState,
    locationsListState: LocationsListState,
    eventUiState: ScheduleEventUiState,
    onUpdateUiState: (ScheduleEventDetails) -> Unit,
    onSaveEvent: () -> Unit,
    onRemoveEvent: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Subject") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(onClick = onSaveEvent,
                        enabled = eventUiState.isEntryValid) {
                        Text("Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            ScheduleEventEntryForm(
                predefinedSubjectsState = predefinedSubjectsState,
                teachersListState = teachersListState,
                locationsListState = locationsListState,
                eventUiState = eventUiState,
                onScheduleEventValueChange = onUpdateUiState,
                onRemoveEvent = onRemoveEvent
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
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true),
    )
}

@Composable
fun ScheduleEventEntryForm(
    predefinedSubjectsState: PredefinedSubjectsState,
    teachersListState: TeachersListState,
    locationsListState: LocationsListState,
    eventUiState: ScheduleEventUiState,
    onScheduleEventValueChange: (ScheduleEventDetails) -> Unit,
    onRemoveEvent: () -> Unit
) {

    Spacer(modifier = Modifier.height(16.dp))

    PredefinedSubjectSelection(
        predefinedSubjects = predefinedSubjectsState.subjectsList,
        scheduleEventDetails = eventUiState.scheduleEventDetails,
        onValueChange = onScheduleEventValueChange
    )

    Spacer(modifier = Modifier.height(16.dp))

    CustomSubjectTextField(
        scheduleEventDetails = eventUiState.scheduleEventDetails,
        onValueChange = onScheduleEventValueChange
    )

    Spacer(modifier = Modifier.height(16.dp))

    TeacherSelection(
        teachersList = teachersListState.teachersList,
        scheduleEventDetails = eventUiState.scheduleEventDetails,
        onValueChange = onScheduleEventValueChange
    )

    Spacer(modifier = Modifier.height(16.dp))

    LocationSelection(
        locationsList = locationsListState.locationsList,
        scheduleEventDetails = eventUiState.scheduleEventDetails,
        onValueChange = onScheduleEventValueChange
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
fun PredefinedSubjectSelection(
    predefinedSubjects: List<Subject>,
    scheduleEventDetails: ScheduleEventDetails,
    onValueChange: (ScheduleEventDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    var subjectsDropdownExpanded by remember { mutableStateOf(false) }

    Text("Select from predefined")
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = if (scheduleEventDetails.subject.shortenedCode.isEmpty())
                "Select a subject..."
            else
                "${scheduleEventDetails.subject.fullDisplayName} (${scheduleEventDetails.subject.shortenedCode})",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { subjectsDropdownExpanded = true })

        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { subjectsDropdownExpanded = true }
                .background(Color.Transparent)
        )

        DropdownMenu(
            expanded = subjectsDropdownExpanded,
            onDismissRequest = { subjectsDropdownExpanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            predefinedSubjects.forEach { subject ->
                DropdownMenuItem(
                    text = { Text(subject.fullDisplayName + " (" + subject.shortenedCode + ")") },
                    onClick = {
                        onValueChange(scheduleEventDetails.copy(subject = subject))
                        subjectsDropdownExpanded = false
                    })
            }
        }
    }

}

@Composable
fun CustomSubjectTextField(
    scheduleEventDetails: ScheduleEventDetails,
    onValueChange: (ScheduleEventDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    var customSubjectName by remember {
        mutableStateOf(
            if (scheduleEventDetails.isCustomSubject)
                scheduleEventDetails.subject.fullDisplayName
            else
                ""
        )
    }

    Text("Subject title")
    OutlinedTextField(
        value = customSubjectName,
        onValueChange = { newValue ->
            customSubjectName = newValue

            if (scheduleEventDetails.isCustomSubject || customSubjectName.isNotEmpty()) {
                val tempSubject = Subject(shortenedCode = "", fullDisplayName = newValue)
                val updatedSubject =
                    scheduleEventDetails.copy(subject = tempSubject).toCustomSubject()
                onValueChange(scheduleEventDetails.copy(subject = updatedSubject))
            }
        },
        keyboardOptions = KeyboardOptions.Default.copy(
            autoCorrectEnabled = false,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        enabled = true,
        placeholder = { Text("Enter custom subject name") },
        modifier = Modifier.fillMaxWidth(1f)
    )
}

@Composable
fun TeacherSelection(
    teachersList: List<Teacher>,
    scheduleEventDetails: ScheduleEventDetails,
    onValueChange: (ScheduleEventDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    var teachersDropdownExpanded by remember { mutableStateOf(false) }
    Text("Teacher")
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = scheduleEventDetails.teacher.teacherName,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { teachersDropdownExpanded = true })

        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { teachersDropdownExpanded = true }
                .background(Color.Transparent)
        )

        DropdownMenu(
            expanded = teachersDropdownExpanded,
            onDismissRequest = { teachersDropdownExpanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            teachersList.forEach { teacher ->
                DropdownMenuItem(text = { Text(teacher.teacherName) }, onClick = {
                    onValueChange(scheduleEventDetails.copy(teacher = teacher))
                    teachersDropdownExpanded = false
                })
            }
        }
    }
}

@Composable
fun LocationSelection(
    locationsList: List<Location>,
    scheduleEventDetails: ScheduleEventDetails,
    onValueChange: (ScheduleEventDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    var locationDropdownExpanded by remember { mutableStateOf(false) }

    Text("Room")
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = scheduleEventDetails.location.roomCode,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { locationDropdownExpanded = true })

        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { locationDropdownExpanded = true }
                .background(Color.Transparent)
        )

        DropdownMenu(
            expanded = locationDropdownExpanded,
            onDismissRequest = { locationDropdownExpanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            locationsList.forEach { location ->
                DropdownMenuItem(text = { Text(location.roomCode) }, onClick = {
                    onValueChange(scheduleEventDetails.copy(location = location))
                    locationDropdownExpanded = false
                })
            }
        }
    }
}

@Composable
fun DaySelection(
    scheduleEventDetails: ScheduleEventDetails,
    onValueChange: (ScheduleEventDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    var dayDropdownExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value = days[scheduleEventDetails.day - 1],
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                modifier = Modifier
                    .clickable { dayDropdownExpanded = true })

            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { dayDropdownExpanded = true }
                    .background(Color.Transparent)
            )

            DropdownMenu(
                expanded = dayDropdownExpanded,
                onDismissRequest = { dayDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                days.forEach { day ->
                    DropdownMenuItem(text = { Text(day) }, onClick = {
                        onValueChange(scheduleEventDetails.copy(day = days.indexOf(day) + 1))
                        dayDropdownExpanded = false
                    })
                }
            }
        }
    }
}

@Composable
fun TimeSelection(
    scheduleEventDetails: ScheduleEventDetails,
    onValueChange: (ScheduleEventDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTimeValid = scheduleEventDetails.startHour < scheduleEventDetails.endHour

    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        HourSelection(
            scheduleEventDetails = scheduleEventDetails,
            isStartHour = true,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f)
        )
        HourSelection(
            scheduleEventDetails = scheduleEventDetails,
            isStartHour = false,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f)
        )
    }

    if (!isTimeValid) {
        Text(
            "End time must be after start time",
            color = Color.Red,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun HourSelection(
    scheduleEventDetails: ScheduleEventDetails,
    isStartHour: Boolean,
    onValueChange: (ScheduleEventDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val hours = (7..20).toList()
    var hourDropdownExpanded by remember { mutableStateOf(false) }
    val selectedHour =
        if (isStartHour) scheduleEventDetails.startHour else scheduleEventDetails.endHour
    val label = if (isStartHour) "Start Hour" else "End Hour"
    Column(modifier = modifier) {
        Text(label)
        Box {
            OutlinedTextField(
                value = "$selectedHour:00",
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, "dropdown") },
                modifier = Modifier
                    .clickable { hourDropdownExpanded = true })
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { hourDropdownExpanded = true }
                    .background(Color.Transparent)
            )
            DropdownMenu(
                expanded = hourDropdownExpanded,
                onDismissRequest = { hourDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                hours.forEach { hour ->
                    DropdownMenuItem(text = { Text("$hour:00") }, onClick = {
                        if (isStartHour) {
                            onValueChange(scheduleEventDetails.copy(startHour = hour))
                        } else {
                            onValueChange(scheduleEventDetails.copy(endHour = hour))
                        }
                        hourDropdownExpanded = false
                    })
                }
            }
        }
    }
}

@Composable
fun RadioButtonObligationSelection(
    scheduleEventDetails: ScheduleEventDetails,
    onValueChange: (ScheduleEventDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val radioOptions = listOf(Obligation.P, Obligation.PV, Obligation.V)
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
    Column(modifier.selectableGroup()) {
        radioOptions.forEach { obligation ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (obligation == selectedOption),
                        onClick = { onOptionSelected(obligation) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (obligation == selectedOption), onClick = {
                        onOptionSelected(obligation)
                        onValueChange(scheduleEventDetails.copy(obligation = obligation))
                    })
                Text(
                    text = obligation.name, modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun RemoveEventButton(
    onRemoveEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton (
        colors = ButtonColors( containerColor = Color.Red,
            contentColor = Color.Black,
            disabledContainerColor = Color.Red,
            disabledContentColor = Color.Black),
        onClick = onRemoveEvent,
    ) {
        Text("Remove event")
    }
}