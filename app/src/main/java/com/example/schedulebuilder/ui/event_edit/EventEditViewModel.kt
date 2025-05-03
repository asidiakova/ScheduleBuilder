package com.example.schedulebuilder.ui.event_edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schedulebuilder.data.FullScheduleEvent
import com.example.schedulebuilder.data.Location
import com.example.schedulebuilder.data.LocationsRepositoryInterface
import com.example.schedulebuilder.data.Obligation
import com.example.schedulebuilder.data.ScheduleEvent
import com.example.schedulebuilder.data.ScheduleEventsRepositoryInterface
import com.example.schedulebuilder.data.Subject
import com.example.schedulebuilder.data.SubjectsRepositoryInterface
import com.example.schedulebuilder.data.Teacher
import com.example.schedulebuilder.data.TeachersRepositoryInterface
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EventEditViewModel(
    savedStateHandle: SavedStateHandle,
    private val scheduleEventsRepository: ScheduleEventsRepositoryInterface,
    private val subjectsRepository: SubjectsRepositoryInterface,
    private val teachersRepository: TeachersRepositoryInterface,
    private val locationsRepository: LocationsRepositoryInterface
) : ViewModel() {

    var scheduleEventUiState by mutableStateOf(ScheduleEventUiState())
        private set

    private val scheduleEventId: Int = checkNotNull(savedStateHandle[EventEditDestination.eventIdArg])

    init {
        viewModelScope.launch {
            scheduleEventUiState = scheduleEventsRepository.getFullScheduleEventStream(scheduleEventId)
                .filterNotNull()
                .first()
                .toScheduleEventUiState(true)
        }
    }

    private fun validateInput(uiState: ScheduleEventDetails): Boolean {
        return with(uiState) {
            subject.shortenedCode.isNotBlank() && teacher.teacherName.isNotBlank() && location.roomCode.isNotBlank() && startHour >= 7 && endHour <= 20 && startHour < endHour
        }
    }

    fun updateUiState(scheduleEventDetails: ScheduleEventDetails) {
        scheduleEventUiState =
            ScheduleEventUiState(scheduleEventDetails = scheduleEventDetails, isEntryValid = validateInput(scheduleEventDetails))
    }

    suspend fun saveScheduleEventEdits() : Boolean {
        return if (validateInput(scheduleEventUiState.scheduleEventDetails)) {
            subjectsRepository.insertSubject(scheduleEventUiState.scheduleEventDetails.subject)
            teachersRepository.insertTeacher(scheduleEventUiState.scheduleEventDetails.teacher)
            locationsRepository.insertLocation(scheduleEventUiState.scheduleEventDetails.location)
            scheduleEventsRepository.updateScheduleEvent(scheduleEventUiState.scheduleEventDetails.toScheduleEvent())
            true
        } else {
            false
        }

    }

    suspend fun removeScheduleEvent() {
        scheduleEventsRepository.deleteScheduleEvent(scheduleEventUiState.scheduleEventDetails.toScheduleEvent())
    }


    val predefinedSubjectsState: StateFlow<PredefinedSubjectsState> = subjectsRepository.getAllSubjectsStream().map { PredefinedSubjectsState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = PredefinedSubjectsState()
        )

    val teachersListState: StateFlow<TeachersListState> = teachersRepository.getAllTeachersStream().map { TeachersListState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = TeachersListState()
        )

    val locationsListState: StateFlow<LocationsListState> = locationsRepository.getAllLocationsStream().map { LocationsListState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = LocationsListState()
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

data class PredefinedSubjectsState(val subjectsList: List<Subject> = listOf())

data class TeachersListState(val teachersList: List<Teacher> = listOf())

data class LocationsListState(val locationsList: List<Location> = listOf())

data class ScheduleEventUiState(
    val scheduleEventDetails: ScheduleEventDetails = ScheduleEventDetails(),
    val isEntryValid: Boolean = false
)

data class ScheduleEventDetails(
    val id: Int = 0,
    val subject: Subject = Subject("", ""),
    val teacher: Teacher = Teacher(""),
    val location: Location = Location(""),
    val obligation: Obligation = Obligation.P,
    val day: Int = 1,
    val startHour: Int = 7,
    val endHour: Int = 9,

    val isCustomSubject: Boolean = subject.shortenedCode.isEmpty()
)

fun ScheduleEventDetails.toCustomSubject(): Subject {
    val name = this.subject.fullDisplayName.trim()

    val code = if (name.isNotBlank()) {
        val baseCode = if (name.length >= 3) name.substring(0, 3) else name
        baseCode.uppercase()
    } else {
        ""
    }

    return Subject(
    shortenedCode = code,
    fullDisplayName = name)

}


fun ScheduleEventDetails.toScheduleEvent(): ScheduleEvent = ScheduleEvent(
    id = id,
    teacherName = teacher.teacherName,
    roomCode = location.roomCode,
    subjectShortenedCode = subject.shortenedCode,
    obligation = obligation,
    day = day,
    startHour = startHour,
    endHour = endHour
)


fun FullScheduleEvent.toScheduleEventUiState(isEntryValid: Boolean = false): ScheduleEventUiState = ScheduleEventUiState(
    scheduleEventDetails = this.toScheduleEventDetails(),
    isEntryValid = isEntryValid
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

