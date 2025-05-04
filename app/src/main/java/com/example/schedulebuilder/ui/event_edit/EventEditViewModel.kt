package com.example.schedulebuilder.ui.event_edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schedulebuilder.data.FullScheduleEvent
import com.example.schedulebuilder.data.LocationsRepositoryInterface
import com.example.schedulebuilder.data.ScheduleEventsRepositoryInterface
import com.example.schedulebuilder.data.SubjectsRepositoryInterface
import com.example.schedulebuilder.data.TeachersRepositoryInterface
import com.example.schedulebuilder.ui.event_entry.LocationsListState
import com.example.schedulebuilder.ui.event_entry.PredefinedSubjectsState
import com.example.schedulebuilder.ui.event_entry.ScheduleEventDetails
import com.example.schedulebuilder.ui.event_entry.ScheduleEventUiState
import com.example.schedulebuilder.ui.event_entry.TeachersListState
import com.example.schedulebuilder.ui.event_entry.toScheduleEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val scheduleEventsRepository: ScheduleEventsRepositoryInterface,
    private val subjectsRepository: SubjectsRepositoryInterface,
    private val teachersRepository: TeachersRepositoryInterface,
    private val locationsRepository: LocationsRepositoryInterface
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    private val allSubjectsStream = subjectsRepository.getAllSubjectsStream()
    private val allTeachersStream = teachersRepository.getAllTeachersStream()
    private val allLocationsStream = locationsRepository.getAllLocationsStream()

    private val _subjectQuery = MutableStateFlow("")
    private val _teacherQuery = MutableStateFlow("")
    private val _locationQuery = MutableStateFlow("")

    val filteredSubjectsState: StateFlow<PredefinedSubjectsState> =
//        source: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/combine.html
        allSubjectsStream.combine(_subjectQuery) { subjects, query ->
            PredefinedSubjectsState(subjects.filter {
                it.fullDisplayName.contains(
                    query, ignoreCase = true
                )
            })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = PredefinedSubjectsState()
        )

    val filteredTeachersState: StateFlow<TeachersListState> =
        allTeachersStream.combine(_teacherQuery) { teachers, query ->
            TeachersListState(teachers.filter { it.teacherName.contains(query, ignoreCase = true) })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = TeachersListState()
        )

    val filteredLocationsState: StateFlow<LocationsListState> =
        allLocationsStream.combine(_locationQuery) { locations, query ->
            LocationsListState(locations.filter { it.roomCode.contains(query, ignoreCase = true) })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = LocationsListState()
        )

    fun updateSubjectQuery(query: String) {
        _subjectQuery.value = query
    }

    fun updateTeacherQuery(query: String) {
        _teacherQuery.value = query
    }

    fun updateLocationQuery(query: String) {
        _locationQuery.value = query
    }

    var scheduleEventUiState by mutableStateOf(ScheduleEventUiState())
        private set

    private val scheduleEventId: Int =
        checkNotNull(savedStateHandle[EventEditDestination.eventIdArg])

    init {
        viewModelScope.launch {
            scheduleEventUiState =
                scheduleEventsRepository.getFullScheduleEventStream(scheduleEventId).filterNotNull()
                    .first().toScheduleEventUiState(true)
        }
    }

    private fun validateInput(uiState: ScheduleEventDetails): Boolean {
        return with(uiState) {
            subject.shortenedCode.isNotBlank() && teacher.teacherName.isNotBlank() && location.roomCode.isNotBlank() && startHour >= 7 && endHour <= 20 && startHour < endHour
        }
    }

    fun updateUiState(scheduleEventDetails: ScheduleEventDetails) {
        scheduleEventUiState = ScheduleEventUiState(
            scheduleEventDetails = scheduleEventDetails,
            isEntryValid = validateInput(scheduleEventDetails)
        )
    }

    suspend fun saveScheduleEventEdits() {
        if (validateInput(scheduleEventUiState.scheduleEventDetails)) {
            subjectsRepository.insertSubject(scheduleEventUiState.scheduleEventDetails.subject)
            teachersRepository.insertTeacher(scheduleEventUiState.scheduleEventDetails.teacher)
            locationsRepository.insertLocation(scheduleEventUiState.scheduleEventDetails.location)
            scheduleEventsRepository.updateScheduleEvent(scheduleEventUiState.scheduleEventDetails.toScheduleEvent())
        }
    }

    suspend fun removeScheduleEvent() {
        scheduleEventsRepository.deleteScheduleEvent(scheduleEventUiState.scheduleEventDetails.toScheduleEvent())
    }
}


fun FullScheduleEvent.toScheduleEventUiState(isEntryValid: Boolean = false): ScheduleEventUiState =
    ScheduleEventUiState(
        scheduleEventDetails = this.toScheduleEventDetails(), isEntryValid = isEntryValid
    )

fun FullScheduleEvent.toScheduleEventDetails(): ScheduleEventDetails = ScheduleEventDetails(
    id = scheduleEvent.id,
    subject = subject,
    teacher = teacher,
    location = location,
    obligation = scheduleEvent.obligation,
    day = scheduleEvent.day,
    startHour = scheduleEvent.startHour,
    endHour = scheduleEvent.endHour
)

