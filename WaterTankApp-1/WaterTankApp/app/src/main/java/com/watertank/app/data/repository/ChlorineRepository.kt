package com.watertank.app.data.repository

import com.watertank.app.data.local.ChlorineDao
import com.watertank.app.data.model.BottleStatus
import com.watertank.app.data.model.ChlorineBottle
import kotlinx.coroutines.flow.Flow

class ChlorineRepository(private val dao: ChlorineDao) {
    fun observe(): Flow<List<ChlorineBottle>> = dao.observeAll()
    suspend fun add(label: String) = dao.insert(ChlorineBottle(label = label))
    suspend fun update(bottle: ChlorineBottle) = dao.update(bottle)
    suspend fun delete(bottle: ChlorineBottle) = dao.delete(bottle)

    suspend fun cycleStatus(bottle: ChlorineBottle) {
        val next = when (bottle.status) {
            BottleStatus.WORKING     -> BottleStatus.STANDBY
            BottleStatus.STANDBY     -> BottleStatus.EMPTY
            BottleStatus.EMPTY       -> BottleStatus.NOT_WORKING
            BottleStatus.NOT_WORKING -> BottleStatus.WORKING
        }
        dao.update(bottle.copy(status = next, lastChangedAt = System.currentTimeMillis()))
    }
}
