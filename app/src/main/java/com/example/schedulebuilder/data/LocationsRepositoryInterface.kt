package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

interface LocationsRepositoryInterface {
    fun getAllLocationsStream(): Flow<List<Location>>

    suspend fun insertLocation(location: Location)

    suspend fun deleteLocation(location: Location)

    suspend fun updateLocation(location: Location)
}