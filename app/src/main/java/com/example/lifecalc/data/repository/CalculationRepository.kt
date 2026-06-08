package com.example.lifecalc.data.repository

import com.example.lifecalc.data.db.CalculationDao
import com.example.lifecalc.data.db.CalculationEntity
import com.example.lifecalc.domain.model.CalculationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class CalculationRepository(
    private val dao: CalculationDao
) {
    // Free-Limit: max 3 Einträge sichtbar
    fun getHistory(isPremium: Boolean): Flow<List<CalculationResult>> {
        val flow = if (isPremium) dao.getAllCalculations()
        else dao.getRecentCalculations(3)
        return flow.map { list -> list.map { it.toDomain() } }
    }

    suspend fun save(result: CalculationResult): Long =
        dao.insert(result.toEntity())

    suspend fun delete(result: CalculationResult) =
        dao.delete(result.toEntity())

    suspend fun getCount(): Int = dao.getCount()
}

// Extension-Mapper
private fun CalculationEntity.toDomain() = CalculationResult(
    id = id,
    targetAmount = targetAmount,
    targetLabel = targetLabel,
    hourlyRateNet = 0.0, // nicht gespeichert für Datensparsamkeit
    hoursRequired = hoursRequired,
    daysRequired = daysRequired,
    weeksRequired = weeksRequired,
    monthsRequired = monthsRequired,
    lifePercentage = lifePercentage,
    emotionalMessage = emotionalMessage,
    timestamp = Instant.ofEpochMilli(timestampMillis)
)

private fun CalculationResult.toEntity() = CalculationEntity(
    id = id,
    targetAmount = targetAmount,
    targetLabel = targetLabel,
    hoursRequired = hoursRequired,
    daysRequired = daysRequired,
    weeksRequired = weeksRequired,
    monthsRequired = monthsRequired,
    lifePercentage = lifePercentage,
    emotionalMessage = emotionalMessage,
    timestampMillis = timestamp.toEpochMilli()
)