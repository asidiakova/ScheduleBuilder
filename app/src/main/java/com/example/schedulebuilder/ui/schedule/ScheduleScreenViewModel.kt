package com.example.schedulebuilder.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.schedulebuilder.data.FullScheduleEvent
import com.example.schedulebuilder.data.ScheduleEventsRepositoryInterface
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
class ScheduleScreenViewModel(private val scheduleEventsRepository: ScheduleEventsRepositoryInterface) : ViewModel() {

    val scheduleUiState: StateFlow<ScheduleUiState> = scheduleEventsRepository.getAllFullScheduleEventsStream().map { ScheduleUiState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = ScheduleUiState()
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

    suspend fun clearSchedule() {
        scheduleEventsRepository.deleteAllEvents()
    }

}

data class ScheduleUiState(val eventsList: List<FullScheduleEvent> = listOf())