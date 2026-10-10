package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BpReading
import kotlinx.coroutines.flow.Flow

@Dao
interface BpDao {
    @Query("SELECT * FROM bp_readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<BpReading>>

    @Query("SELECT * FROM bp_readings WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun getReadingsSince(since: Long): Flow<List<BpReading>>

    @Query("SELECT * FROM bp_readings ORDER BY timestamp ASC")
    fun getAllReadingsAsc(): Flow<List<BpReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: BpReading): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<BpReading>)

    @Update
    suspend fun updateReading(reading: BpReading)

    @Delete
    suspend fun deleteReading(reading: BpReading)

    @Query("DELETE FROM bp_readings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bp_readings")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM bp_readings")
    suspend fun count(): Int
}
