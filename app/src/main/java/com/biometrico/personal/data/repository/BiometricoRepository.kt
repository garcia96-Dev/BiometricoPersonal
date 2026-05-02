package com.biometrico.personal.data.repository

import androidx.lifecycle.LiveData
import com.biometrico.personal.data.database.BiometricoDatabase
import com.biometrico.personal.data.model.ConfiguracionHorario
import com.biometrico.personal.data.model.RegistroAsistencia
import java.time.LocalDate
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
        val ahora = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val existente = registroDao.getRegistroPorFecha(hoy)
        return if (existente != null) {
            existente // ya hay registro de hoy
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
        val ahora = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        val registro = registroDao.getRegistroPorFecha(hoy) ?: return null

        if (registro.horaEntrada == null) return null

        val entrada = java.time.LocalTime.parse(registro.horaEntrada, DateTimeFormatter.ofPattern("HH:mm"))
        val salida = java.time.LocalTime.parse(ahora, DateTimeFormatter.ofPattern("HH:mm"))

        var minutosTrabajados = java.time.Duration.between(entrada, salida).toMinutes().toInt()
        minutosTrabajados -= config.duracionAlmuerzo // restar almuerzo
        if (minutosTrabajados < 0) minutosTrabajados = 0

        val horasTrabajadas = minutosTrabajados / 60f

        // Calcular horas de jornada según configuración
        val horasJornada = if (config.usarJornadaPersonalizada) {
            config.horasSemanalesPersonalizadas / diasLaboralesSemana(config)
        } else {
            config.jornadaLey.toFloat() / diasLaboralesSemana(config)
        }

        val horasExtra = maxOf(0f, horasTrabajadas - horasJornada)

        val actualizado = registro.copy(
            horaSalida = ahora,
            horasTrabajadas = horasTrabajadas,
            horasExtra = horasExtra
        )
        registroDao.actualizarRegistro(actualizado)
        return actualizado
    }

    private fun diasLaboralesSemana(config: ConfiguracionHorario): Float {
        var dias = 0f
        if (config.trabajaLunes) dias++
        if (config.trabajaMartes) dias++
        if (config.trabajaMiercoles) dias++
        if (config.trabajaJueves) dias++
        if (config.trabajaViernes) dias++
        if (config.trabajaSabado) dias++
        if (config.trabajaDomingo) dias++
        return if (dias == 0f) 5f else dias
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
