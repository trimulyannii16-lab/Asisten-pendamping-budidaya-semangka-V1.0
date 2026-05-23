package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planting_events")
data class PlantingEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val blockName: String,
    val plantCount: Int,
    val plantingDate: String, // YYYY-MM-DD
    val targetHarvestDate: String, // YYYY-MM-DD
    val seedCount: Int,
    val rowSpacing: Double, // in meters
    val holeSpacing: Double, // in meters
    val notes: String = ""
)

@Entity(tableName = "scouting_logs")
data class ScoutingLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val areaName: String,
    val pestHamaObserved: String,
    val severityLevel: String, // Ringan, Sedang, Parah
    val notes: String,
    val statusTindakan: String // Belum Ditangani, Sudah Disemprot, dsb.
)

@Entity(tableName = "expense_items")
data class ExpenseItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // Benih, Pupuk Kimia, Pupuk Organik, Pestisida, Mulsa/Drip, Alat, Lainnya
    val cost: Double,
    val quantity: Double,
    val unit: String, // kg, karung, sachet, botol, roll, dsb.
    val date: String // YYYY-MM-DD
)
