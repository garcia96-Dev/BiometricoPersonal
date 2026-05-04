package com.biometrico.personal.data.repository

import androidx.lifecycle.LiveData
import com.biometrico.personal.data.database.BiometricoDatabase
import com.biometrico.personal.data.model.ConfiguracionHorario
import com.biometrico.personal.data.model.RegistroAsistencia
import com.biometrico.personal.data.model.getHorarioDia
import java.time.LocalDate
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class BiometricoRepository(database: BiometricoDatabase) {

    private val registroDao = database.registroDao()
    private val configuracionDao = database.configuracionDao()

    val todosRegistros: LiveData<List<RegistroAsistencia>> = registroDao.getTodosRegistros()
    val configuracion: LiveData<ConfiguracionHorario?> = configuracionDao.getConfiguracion()

    suspend fun getRegistroHoy(): RegistroAsistencia? {
        val hoy = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        return registroDao.getRegistroPorFecha(hoy)
    }

    suspend fun registrarEntrada(): RegistroAsistencia {
        val hoy = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val ahora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val existente = registroDao.getRegistroPorFecha(hoy)
        return if (existente != null) {
            existente
        } else {
            val nuevo = RegistroAsistencia(
                fecha = hoy,
                horaEntrada = ahora,
                horaSalida = null
            )
            val id = registroDao.insertarRegistro(nuevo)
            nuevo.copy(id = id)
        }
    }

    suspend fun registrarSalida(config: ConfiguracionHorario): RegistroAsistencia? {
        val hoy = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val ahora = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val registro = registroDao.getRegistroPorFecha(hoy) ?: return null
        if (registro.horaEntrada == null) return null

        val horasTrabajadas = calcularHorasTrabajadas(
            registro.horaEntrada, ahora, config.duracionAlmuerzo
        )

        // Obtener horas esperadas para el día de hoy
        val diaSemana = LocalDate.now().dayOfWeek.value // 1=Lunes, 7=Domingo
        val horarioDia = config.getHorarioDia(diaSemana)
        val horasExtra = maxOf(0f, horasTrabajadas - horarioDia.horasEsperadas)

        val actualizado = registro.copy(
            horaSalida = ahora,
            horasTrabajadas = horasTrabajadas,
            horasExtra = horasExtra
        )
        registroDao.actualizarRegistro(actualizado)
        return actualizado
    }

    // Calcula horas trabajadas dado entrada, salida y duración de almuerzo
    fun calcularHorasTrabajadas(
        horaEntrada: String,
        horaSalida: String,
        duracionAlmuerzo: Int
    ): Float {
        val fmt = DateTimeFormatter.ofPattern("HH:mm")
        val entrada = LocalTime.parse(horaEntrada, fmt)
        val salida = LocalTime.parse(horaSalida, fmt)
        var minutos = Duration.between(entrada, salida).toMinutes().toInt()
        minutos -= duracionAlmuerzo
        if (minutos < 0) minutos = 0
        return minutos / 60f
    }

    suspend fun insertarRegistro(registro: RegistroAsistencia): Long {
        return registroDao.insertarRegistro(registro)
    }

    suspend fun eliminarRegistro(registro: RegistroAsistencia) {
        registroDao.eliminarRegistro(registro)
    }

    suspend fun getResumenMes(mes: String): ResumenMes {
        val registros = registroDao.getRegistrosPorMes(mes)
        val totalHoras = registroDao.getTotalHorasMes(mes) ?: 0f
        val totalExtras = registroDao.getTotalExtras(mes) ?: 0f
        val diasAsistidos = registroDao.getDiasAsistidosMes(mes)
        return ResumenMes(registros, totalHoras, totalExtras, diasAsistidos)
    }

    suspend fun guardarConfiguracion(config: ConfiguracionHorario) {
        configuracionDao.guardarConfiguracion(config)
    }

    suspend fun getConfiguracionSync(): ConfiguracionHorario {
        return configuracionDao.getConfiguracionSync() ?: ConfiguracionHorario()
    }

    fun getRegistrosPorRango(inicio: String, fin: String): LiveData<List<RegistroAsistencia>> {
        return registroDao.getRegistrosPorRango(inicio, fin)
    }
}

data class ResumenMes(
    val registros: List<RegistroAsistencia>,
    val totalHoras: Float,
    val totalExtras: Float,
    val diasAsistidos: Int
)
