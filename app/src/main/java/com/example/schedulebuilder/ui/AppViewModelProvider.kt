package com.example.schedulebuilder.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.schedulebuilder.ScheduleBuilderApplication
import com.example.schedulebuilder.ui.event_edit.EventEditViewModel
import com.example.schedulebuilder.ui.event_entry.EventEntryViewModel
import com.example.schedulebuilder.ui.schedule.ScheduleScreenViewModel

/**
 * Provides Factory objects which are needed to create the ViewModels
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {

        initializer {
            EventEntryViewModel(
                scheduleBuilderApplication().container.scheduleEventsRepository,
                scheduleBuilderApplication().container.subjectsRepository,
                scheduleBuilderApplication().container.teachersRepository,
                scheduleBuilderApplication().container.locationsRepository
            )
        }

        initializer {
            EventEditViewModel(
                this.createSavedStateHandle(),
                scheduleBuilderApplication().container.scheduleEventsRepository,
                scheduleBuilderApplication().container.subjectsRepository,
                scheduleBuilderApplication().container.teachersRepository,
                scheduleBuilderApplication().container.locationsRepository
            )
        }

        initializer {
            ScheduleScreenViewModel(
                scheduleBuilderApplication().container.scheduleEventsRepository
            )
        }
    }
}

fun CreationExtras.scheduleBuilderApplication(): ScheduleBuilderApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as ScheduleBuilderApplication)
