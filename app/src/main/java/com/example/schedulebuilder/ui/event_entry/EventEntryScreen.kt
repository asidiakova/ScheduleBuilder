@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.schedulebuilder.ui.event_entry

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.schedulebuilder.ui.event_edit.ConfirmationDialog
import kotlinx.coroutines.CoroutineScope

object EventEntryDestination : NavDestination {
    override val route = "add_schedule_event"
    override val titleRes = R.string.app_name
}


@Composable
fun EventEntryScreen(
    navigateBack: () -> Unit,
    viewModel: EventEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val coroutineScope = rememberCoroutineScope()
    val openAlertDialog = remember { mutableStateOf(false) }

    EventEntryDialog(
        onDismissRequest = {
            openAlertDialog.value = true
        }, navigateBack = navigateBack, viewModel = viewModel, coroutineScope = coroutineScope
    )

    if (openAlertDialog.value) {
        ConfirmationDialog(
            onDismissRequest = { openAlertDialog.value = false },
            onConfirmation = {
                openAlertDialog.value = false
                navigateBack()
            },
            dialogTitle = "Discard changes?",
            dialogText = "Are you sure you want to discard your changes and close the window?",
        )
    }
}


@Composable
fun EventEntryDialog(
    onDismissRequest: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: EventEntryViewModel = viewModel(factory = AppViewModelProvider.Factory),
    coroutineScope: CoroutineScope
) {

    val eventUiState = viewModel.eventUiState

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
            EventEntryDialogContent(
                navigateBack = onDismissRequest,
                eventUiState = eventUiState,
                onUpdateUiState = viewModel::updateUiState,
                onSaveEvent = {
                    coroutineScope.launch {
                        viewModel.saveScheduleEvent()
                        navigateBack()
                    }
                },
                viewModel = viewModel
            )
        }
    }
}


@Composable
fun EventEntryDialogContent(
    navigateBack: () -> Unit,
    eventUiState: ScheduleEventUiState,
    onUpdateUiState: (ScheduleEventDetails) -> Unit,
    onSaveEvent: () -> Unit,
    viewModel: EventEntryViewModel
) {
    val scrollState = rememberScrollState()

    val filteredSubjectsState by viewModel.filteredSubjectsState.collectAsState()
    val filteredTeachersState by viewModel.filteredTeachersState.collectAsState()
    val filteredLocationsState by viewModel.filteredLocationsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add Event") }, navigationIcon = {
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
                eventUiState = eventUiState,
                onScheduleEventValueChange = onUpdateUiState,
                viewModel = viewModel,
                filteredSubjectsState = filteredSubjectsState,
                filteredTeachersState = filteredTeachersState,
                filteredLocationsState = filteredLocationsState
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}


@Composable
fun ScheduleEventEntryForm(
    viewModel: EventEntryViewModel,
    filteredSubjectsState: PredefinedSubjectsState,
    filteredTeachersState: TeachersListState,
    filteredLocationsState: LocationsListState,
    eventUiState: ScheduleEventUiState,
    onScheduleEventValueChange: (ScheduleEventDetails) -> Unit,
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
}


@Composable
fun SubjectSelection(
    predefinedSubjects: List<Subject>,
    scheduleEventDetails: ScheduleEventDetails,
    onValueChange: (ScheduleEventDetails) -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var wasInitialized by rememberSaveable { mutableStateOf(false) }
    if (!wasInitialized && scheduleEventDetails.subject.fullDisplayName.isNotBlank()) {
        query = scheduleEventDetails.subject.fullDisplayName
        wasInitialized = true
    }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    val focusManager = LocalFocusManager.current
    val filteredSubjects = predefinedSubjects

    LaunchedEffect(selectedSubject) {
        selectedSubject?.let {
            query = it.fullDisplayName
            onValueChange(scheduleEventDetails.copy(subject = it))
        }
    }

    Text("Select a subject")
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        TextField(
            value = query,
            onValueChange = {
                query = it
                onQueryChange(it)
                expanded = true
                val matched = predefinedSubjects.find { subj ->
                    subj.fullDisplayName.equals(it, ignoreCase = true)
                }
                if (matched != null) {
                    selectedSubject = matched
                } else {
                    val customSubject = (scheduleEventDetails.subject.copy(fullDisplayName = it))
                    onValueChange(
                        scheduleEventDetails.copy(
                            subject = scheduleEventDetails.copy(
                                subject = customSubject
                            ).toCustomSubject()
                        )
                    )
                }
            },
            label = { Text("Subject") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                .fillMaxWidth(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            if (filteredSubjects.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No matching subjects") },
                    onClick = {},
                    enabled = false
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .height(50.dp * filteredSubjects.size.coerceAtMost(5))
                        .width(500.dp)
                ) {
                    items(filteredSubjects.size) { subject ->
                        DropdownMenuItem(
                            text = { Text("${filteredSubjects[subject].fullDisplayName} (${filteredSubjects[subject].shortenedCode})") },
                            onClick = {
                                expanded = false
                                selectedSubject = filteredSubjects[subject]
                                focusManager.clearFocus()
                            })
                    }
                }
            }
        }
    }
}


@Composable
fun TeacherSelection(
    teachersList: List<Teacher>,
    scheduleEventDetails: ScheduleEventDetails,
    onValueChange: (ScheduleEventDetails) -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var wasInitialized by rememberSaveable { mutableStateOf(false) }
    if (!wasInitialized && scheduleEventDetails.teacher.teacherName.isNotBlank()) {
        query = scheduleEventDetails.teacher.teacherName
        wasInitialized = true
    }
    val focusManager = LocalFocusManager.current
    val filteredTeachers = teachersList

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier
    ) {
        TextField(
            value = query,
            onValueChange = {
                query = it
                onQueryChange(it)
                expanded = true
            },
            label = { Text("Select teacher") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            if (filteredTeachers.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No matching teachers") },
                    onClick = {},
                    enabled = false
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .height(50.dp * filteredTeachers.size.coerceAtMost(5))
                        .width(500.dp)
                ) {
                    items(filteredTeachers.size) { index ->
                        val teacher = filteredTeachers[index]
                        DropdownMenuItem(text = { Text(teacher.teacherName) }, onClick = {
                            expanded = false
                            onValueChange(scheduleEventDetails.copy(teacher = teacher))
                            query = teacher.teacherName
                            focusManager.clearFocus()
                        })
                    }
                }
            }
        }
    }
}


@Composable
fun LocationSelection(
    locationsList: List<Location>,
    scheduleEventDetails: ScheduleEventDetails,
    onValueChange: (ScheduleEventDetails) -> Unit,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var wasInitialized by rememberSaveable { mutableStateOf(false) }
    if (!wasInitialized && scheduleEventDetails.location.roomCode.isNotBlank()) {
        query = scheduleEventDetails.location.roomCode
        wasInitialized = true
    }
    val focusManager = LocalFocusManager.current

    val filteredLocations = locationsList

    ExposedDropdownMenuBox(
        expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier
    ) {
        TextField(
            value = query,
            onValueChange = {
                query = it
                onQueryChange(it)
                expanded = true
            },
            label = { Text("Select location") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryEditable, enabled = true)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { expanded = false }) {
            if (filteredLocations.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No matching locations") },
                    onClick = {},
                    enabled = false
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .height(50.dp * filteredLocations.size.coerceAtMost(5))
                        .width(500.dp)
                ) {
                    items(filteredLocations.size) { location ->
                        DropdownMenuItem(
                            text = { Text(filteredLocations[location].roomCode) },
                            onClick = {
                                expanded = false
                                onValueChange(scheduleEventDetails.copy(location = filteredLocations[location]))
                                query = filteredLocations[location].roomCode
                                focusManager.clearFocus()
                            })
                    }
                }
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
                modifier = Modifier.clickable { dayDropdownExpanded = true })

            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { dayDropdownExpanded = true }
                    .background(Color.Transparent))

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
                modifier = Modifier.clickable { hourDropdownExpanded = true })
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { hourDropdownExpanded = true }
                    .background(Color.Transparent))
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
    var selectedOption by rememberSaveable { mutableStateOf(radioOptions[0]) }
    var wasInitialized by rememberSaveable { mutableStateOf(false) }
    if (!wasInitialized) {
        selectedOption = scheduleEventDetails.obligation
        wasInitialized = true
    }
    Column(modifier.selectableGroup()) {
        radioOptions.forEach { obligation ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (obligation == selectedOption),
                        onClick = { },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (obligation == selectedOption), onClick = {
                        selectedOption = obligation
                        onValueChange(scheduleEventDetails.copy(obligation = obligation))
                    })
                Text(
                    text = obligation.name, modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
