package com.example.data

import kotlinx.coroutines.flow.Flow

class WatermelonRepository(private val dao: WatermelonDao) {
    val allEvents: Flow<List<PlantingEvent>> = dao.getAllPlantingEvents()
    val allLogs: Flow<List<ScoutingLog>> = dao.getAllScoutingLogs()
    val allExpenses: Flow<List<ExpenseItem>> = dao.getAllExpenses()
    val totalCost: Flow<Double?> = dao.getTotalCostFlow()

    suspend fun insertEvent(event: PlantingEvent) = dao.insertPlantingEvent(event)
    suspend fun updateEvent(event: PlantingEvent) = dao.updatePlantingEvent(event)
    suspend fun deleteEvent(event: PlantingEvent) = dao.deletePlantingEvent(event)

    suspend fun insertLog(log: ScoutingLog) = dao.insertScoutingLog(log)
    suspend fun updateLog(log: ScoutingLog) = dao.updateScoutingLog(log)
    suspend fun deleteLog(log: ScoutingLog) = dao.deleteScoutingLog(log)

    suspend fun insertExpense(expense: ExpenseItem) = dao.insertExpense(expense)
    suspend fun updateExpense(expense: ExpenseItem) = dao.updateExpense(expense)
    suspend fun deleteExpense(expense: ExpenseItem) = dao.deleteExpense(expense)
}
