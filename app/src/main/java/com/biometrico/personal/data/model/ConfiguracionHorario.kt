package com.biometrico.personal.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracion_horario")
data class ConfiguracionHorario(
    @PrimaryKey
    val id: Int = 1,

    // === Jornada Laboral (Ley 2101 de 2021) ===
    val jornadaLey: String = "44",
    val horasSemanalesPersonalizadas: Float = 44f,
    val usarJornadaPersonalizada: Boolean = false,

    // === Horario general (respaldo) ===
    val horaEntrada: String = "08:00",
    val horaSalida: String = "17:00",
    val duracionAlmuerzo: Int = 60,

    // === Horario LUNES ===
    val trabajaLunes: Boolean = true,
    val lunesEntrada: String = "08:00",
    val lunesSalida: String = "17:00",
    val lunesHoras: Float = 8f,

    // === Horario MARTES ===
    val trabajaMartes: Boolean = true,
    val martesEntrada: String = "08:00",
    val martesSalida: String = "17:00",
    val martesHoras: Float = 8f,

    // === Horario MIÉRCOLES ===
    val trabajaMiercoles: Boolean = true,
    val miercolesEntrada: String = "08:00",
    val miercolesSalida: String = "17:00",
    val miercolesHoras: Float = 8f,

    // === Horario JUEVES ===
    val trabajaJueves: Boolean = true,
    val juevesEntrada: String = "08:00",
    val juevesSalida: String = "17:00",
    val juevesHoras: Float = 8f,

    // === Horario VIERNES ===
    val trabajaViernes: Boolean = true,
    val viernesEntrada: String = "08:00",
    val viernesSalida: String = "17:00",
    val viernesHoras: Float = 8f,

    // === Horario SÁBADO ===
    val trabajaSabado: Boolean = false,
    val sabadoEntrada: String = "08:00",
    val sabadoSalida: String = "13:00",
    val sabadoHoras: Float = 4f,

    // === Horario DOMINGO ===
    val trabajaDomingo: Boolean = false,
    val domingoEntrada: String = "08:00",
    val domingoSalida: String = "13:00",
    val domingoHoras: Float = 4f,

    // === Tolerancias ===
    val toleranciaEntradaMin: Int = 5,
    val toleranciaSalidaMin: Int = 5,

    // === Recargos Colombia ===
    val inicioJornadaNocturna: String = "19:00",
    val finJornadaNocturna: String = "06:00",
    val recargoNocturno: Float = 0.35f,
    val recargoDominical: Float = 0.75f,
    val recargoFestivo: Float = 0.75f,
    val recargoExtraDiurna: Float = 0.25f,
    val recargoExtraNocturna: Float = 0.75f,

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

// Clase auxiliar para manejar el horario de un día
data class HorarioDia(
    val nombre: String,
    val trabaja: Boolean,
    val horaEntrada: String,
    val horaSalida: String,
    val horasEsperadas: Float
)

// Extensión para obtener el horario de un día específico (1=Lunes ... 7=Domingo)
fun ConfiguracionHorario.getHorarioDia(diaSemana: Int): HorarioDia {
    return when (diaSemana) {
        1 -> HorarioDia("Lunes", trabajaLunes, lunesEntrada, lunesSalida, lunesHoras)
        2 -> HorarioDia("Martes", trabajaMartes, martesEntrada, martesSalida, martesHoras)
        3 -> HorarioDia("Miércoles", trabajaMiercoles, miercolesEntrada, miercolesSalida, miercolesHoras)
        4 -> HorarioDia("Jueves", trabajaJueves, juevesEntrada, juevesSalida, juevesHoras)
        5 -> HorarioDia("Viernes", trabajaViernes, viernesEntrada, viernesSalida, viernesHoras)
        6 -> HorarioDia("Sábado", trabajaSabado, sabadoEntrada, sabadoSalida, sabadoHoras)
        7 -> HorarioDia("Domingo", trabajaDomingo, domingoEntrada, domingoSalida, domingoHoras)
        else -> HorarioDia("Lunes", trabajaLunes, lunesEntrada, lunesSalida, lunesHoras)
    }
}
