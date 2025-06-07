package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that provides insert, update and delete methods on [Location].
 */
interface LocationsRepositoryInterface {
    /**
     * Retrieve all the locations from the the given data source.
     */
    fun getAllLocationsStream(): Flow<List<Location>>

    /**
     * Insert a location in the data source.
     */
    suspend fun insertLocation(location: Location)

    /**
     * Delete a location from the data source.
     */
    suspend fun deleteLocation(location: Location)

    /**
     * Update a location in the data source.
     */
    suspend fun updateLocation(location: Location)
}