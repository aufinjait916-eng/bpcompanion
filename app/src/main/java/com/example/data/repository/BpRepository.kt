package com.example.data.repository

import com.example.data.local.BpDao
import com.example.data.model.BpReading
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class BpRepository(private val dao: BpDao) {

    val allReadingsDesc: Flow<List<BpReading>> = dao.getAllReadings()
    val allReadingsAsc: Flow<List<BpReading>> = dao.getAllReadingsAsc()

    fun getReadingsSince(since: Long): Flow<List<BpReading>> = dao.getReadingsSince(since)

    suspend fun insertReading(reading: BpReading): Long = dao.insertReading(reading)

    suspend fun updateReading(reading: BpReading) = dao.updateReading(reading)

    suspend fun deleteReading(reading: BpReading) = dao.deleteReading(reading)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun prepopulateIfEmpty() {
        if (dao.count() == 0) {
            val now = System.currentTimeMillis()
            val dayMillis = TimeUnit.DAYS.toMillis(1)
            val demoReadings = listOf(
                BpReading(
                    systolic = 118,
                    diastolic = 78,
                    pulse = 68,
                    timestamp = now - (dayMillis * 6) - (4 * 3600 * 1000),
                    arm = "Left",
                    notes = "Morning after 3 min calm breathing"
                ),
                BpReading(
                    systolic = 124,
                    diastolic = 82,
                    pulse = 74,
                    timestamp = now - (dayMillis * 5) - (2 * 3600 * 1000),
                    arm = "Left",
                    notes = "Evening reading, rested seated"
                ),
                BpReading(
                    systolic = 134,
                    diastolic = 88,
                    pulse = 82,
                    timestamp = now - (dayMillis * 4) - (1 * 3600 * 1000),
                    arm = "Right",
                    notes = "After a busy work commute"
                ),
                BpReading(
                    systolic = 122,
                    diastolic = 79,
                    pulse = 71,
                    timestamp = now - (dayMillis * 3) - (5 * 3600 * 1000),
                    arm = "Left",
                    notes = "Morning calm baseline"
                ),
                BpReading(
                    systolic = 115,
                    diastolic = 74,
                    pulse = 66,
                    timestamp = now - (dayMillis * 2) - (3 * 3600 * 1000),
                    arm = "Left",
                    notes = "Post-walk and 5 min meditation"
                ),
                BpReading(
                    systolic = 128,
                    diastolic = 84,
                    pulse = 76,
                    timestamp = now - (dayMillis * 1) - (6 * 3600 * 1000),
                    arm = "Right",
                    notes = "Afternoon check"
                ),
                BpReading(
                    systolic = 120,
                    diastolic = 80,
                    pulse = 70,
                    timestamp = now - (4 * 3600 * 1000),
                    arm = "Left",
                    notes = "Standard baseline target achieved"
                )
            )
            dao.insertAll(demoReadings)
        }
    }
}
