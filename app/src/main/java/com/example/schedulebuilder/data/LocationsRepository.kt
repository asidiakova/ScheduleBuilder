package com.example.schedulebuilder.data

import kotlinx.coroutines.flow.Flow

class LocationsRepository(private val locationDao: LocationDao): LocationsRepositoryInterface {
    override fun getAllLocationsStream(): Flow<List<Location>> = locationDao.getAllLocations()

    override suspend fun insertLocation(location: Location) = locationDao.insert(location)

    override suspend fun deleteLocation(location: Location) = locationDao.delete(location)

    override suspend fun updateLocation(location: Location) = locationDao.update(location)
}