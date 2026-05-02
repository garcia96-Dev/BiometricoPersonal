package com.biometrico.personal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "registros_asistencia")
data class RegistroAsistencia(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: String,           // "2025-07-15"
    val horaEntrada: String?,    // "08:00"
    val horaSalida: String?,     // "17:00"
    val horasTrabajadas: Float = 0f,
    val horasExtra: Float = 0f,
    val observaciones: String = "",
    val tipoJornada: String = "NORMAL" // NORMAL, NOCTURNA, DOMINICAL, FESTIVO
)
