package com.biometrico.personal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracion_horario")
data class ConfiguracionHorario(
    @PrimaryKey
    val id: Int = 1,

    // === Jornada Laboral (Ley 2101 de 2021) ===
    val jornadaLey: String = "44",       // "48","47","46","44","42" (horas semanales según año)
    val horasSemanalesPersonalizadas: Float = 44f,
    val usarJornadaPersonalizada: Boolean = false,

    // === Horario diario ===
    val horaEntrada: String = "08:00",
    val horaSalida: String = "17:00",
    val duracionAlmuerzo: Int = 60,       // minutos

    // === Días laborales ===
    val trabajaLunes: Boolean = true,
    val trabajaMartes: Boolean = true,
    val trabajaMiercoles: Boolean = true,
    val trabajaJueves: Boolean = true,
    val trabajaViernes: Boolean = true,
    val trabajaSabado: Boolean = false,
    val trabajaDomingo: Boolean = false,

    // === Tolerancias ===
    val toleranciaEntradaMin: Int = 5,    // minutos de gracia llegada tarde
    val toleranciaSalidaMin: Int = 5,     // minutos antes de salida permitido

    // === Recargos Colombia ===
    val inicioJornadaNocturna: String = "19:00",  // Reforma 2024: 7pm
    val finJornadaNocturna: String = "06:00",
    val recargoNocturno: Float = 0.35f,           // 35%
    val recargoDominical: Float = 0.75f,           // 75%
    val recargoFestivo: Float = 0.75f,             // 75%
    val recargoExtraDiurna: Float = 0.25f,         // 25%
    val recargoExtraNocturna: Float = 0.75f,       // 75%

    // === Notificaciones ===
    val notificarEntrada: Boolean = true,
    val notificarSalida: Boolean = true,
    val minutosAntesNotificacion: Int = 10,

    // === Biométrico ===
    val usarHuella: Boolean = true,
    val usarPin: Boolean = false,
    val pinSeguridad: String = "",

    // === General ===
    val nombreTrabajador: String = "",
    val empresaNombre: String = "",
    val cargo: String = ""
)
