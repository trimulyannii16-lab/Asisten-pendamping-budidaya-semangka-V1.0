package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WatermelonDao {
    // --- Planting Events ---
    @Query("SELECT * FROM planting_events ORDER BY plantingDate DESC")
    fun getAllPlantingEvents(): Flow<List<PlantingEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlantingEvent(event: PlantingEvent)

    @Update
    suspend fun updatePlantingEvent(event: PlantingEvent)

    @Delete
    suspend fun deletePlantingEvent(event: PlantingEvent)

    @Query("SELECT COUNT(*) FROM planting_events")
    suspend fun getPlantingEventCount(): Int

    // --- Scouting Logs ---
    @Query("SELECT * FROM scouting_logs ORDER BY date DESC")
    fun getAllScoutingLogs(): Flow<List<ScoutingLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScoutingLog(log: ScoutingLog)

    @Update
    suspend fun updateScoutingLog(log: ScoutingLog)

    @Delete
    suspend fun deleteScoutingLog(log: ScoutingLog)

    // --- Expense Items ---
    @Query("SELECT * FROM expense_items ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(item: ExpenseItem)

    @Update
    suspend fun updateExpense(item: ExpenseItem)

    @Delete
    suspend fun deleteExpense(item: ExpenseItem)

    @Query("SELECT SUM(cost * quantity) FROM expense_items")
    fun getTotalCostFlow(): Flow<Double?>
}
