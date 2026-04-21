package com.watertank.app.data.repository

import com.watertank.app.data.local.ReadingDao
import com.watertank.app.data.local.TankDao
import com.watertank.app.data.model.Tank
import com.watertank.app.data.model.TankReading
import kotlinx.coroutines.flow.Flow

class TankRepository(
    private val tankDao: TankDao,
    private val readingDao: ReadingDao
) {
    fun observeTanks(): Flow<List<Tank>> = tankDao.observeAll()
    suspend fun getFirstTank(): Tank? = tankDao.getFirst()
    suspend fun updateTank(tank: Tank) = tankDao.update(tank)
    suspend fun insertTank(tank: Tank) = tankDao.insert(tank)

    fun observeRecentReadings(tankId: Long): Flow<List<TankReading>> = readingDao.observeRecent(tankId)
    suspend fun latestReading(tankId: Long): TankReading? = readingDao.latest(tankId)
    suspend fun saveReading(reading: TankReading) = readingDao.insert(reading)
    suspend fun clearReadings(tankId: Long) = readingDao.clear(tankId)
}
