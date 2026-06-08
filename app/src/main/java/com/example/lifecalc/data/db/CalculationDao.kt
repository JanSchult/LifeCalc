package com.example.lifecalc.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {

    @Query("SELECT * FROM calculations ORDER BY timestampMillis DESC")
    fun getAllCalculations(): Flow<List<CalculationEntity>>

    @Query("SELECT * FROM calculations ORDER BY timestampMillis DESC LIMIT :limit")
    fun getRecentCalculations(limit: Int): Flow<List<CalculationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CalculationEntity): Long

    @Delete
    suspend fun delete(entity: CalculationEntity)

    @Query("DELETE FROM calculations")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM calculations")
    suspend fun getCount(): Int
}