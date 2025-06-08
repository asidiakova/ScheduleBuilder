package com.example.schedulebuilder.ui.event_entry

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schedulebuilder.data.Location
import com.example.schedulebuilder.data.LocationsRepositoryInterface
import com.example.schedulebuilder.data.Obligation
import com.example.schedulebuilder.data.ScheduleEvent
import com.example.schedulebuilder.data.ScheduleEventsRepositoryInterface
import com.example.schedulebuilder.data.Subject
import com.example.schedulebuilder.data.SubjectsRepositoryInterface
import com.example.schedulebuilder.data.Teacher
import com.example.schedulebuilder.data.TeachersRepositoryInterface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel to retrieve and update an event in the event entry screen.
 */
class EventEntryViewModel(
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

    var eventUiState by mutableStateOf(ScheduleEventUiState())
        private set

    private fun validateInput(uiState: ScheduleEventDetails = eventUiState.scheduleEventDetails): Boolean {
        return with(uiState) {
            subject.shortenedCode.isNotBlank() && teacher.teacherName.isNotBlank() && location.roomCode.isNotBlank() && startHour >= 7 && endHour <= 20 && startHour < endHour
        }
    }

    fun updateUiState(scheduleEventDetails: ScheduleEventDetails) {
        eventUiState = ScheduleEventUiState(
            scheduleEventDetails = scheduleEventDetails,
            isEntryValid = validateInput(scheduleEventDetails)
        )
    }

    suspend fun saveScheduleEvent() {
        if (validateInput()) {
            subjectsRepository.insertSubject(eventUiState.scheduleEventDetails.subject)
            teachersRepository.insertTeacher(eventUiState.scheduleEventDetails.teacher)
            locationsRepository.insertLocation(eventUiState.scheduleEventDetails.location)
            scheduleEventsRepository.insertScheduleEvent(eventUiState.scheduleEventDetails.toScheduleEvent())
        }
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
        shortenedCode = code, fullDisplayName = name
    )
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
