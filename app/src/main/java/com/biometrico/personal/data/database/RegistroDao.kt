package com.biometrico.personal.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.biometrico.personal.data.model.ConfiguracionHorario
import com.biometrico.personal.data.model.RegistroAsistencia

@Dao
interface RegistroDao {

    @Query("SELECT * FROM registros_asistencia ORDER BY fecha DESC, id DESC")
    fun getTodosRegistros(): LiveData<List<RegistroAsistencia>>

    @Query("SELECT * FROM registros_asistencia WHERE fecha = :fecha LIMIT 1")
    suspend fun getRegistroPorFecha(fecha: String): RegistroAsistencia?

    @Query("SELECT * FROM registros_asistencia WHERE fecha BETWEEN :inicio AND :fin ORDER BY fecha DESC")
    fun getRegistrosPorRango(inicio: String, fin: String): LiveData<List<RegistroAsistencia>>

    @Query("SELECT * FROM registros_asistencia WHERE fecha LIKE :mes || '%' ORDER BY fecha ASC")
    suspend fun getRegistrosPorMes(mes: String): List<RegistroAsistencia>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistro(registro: RegistroAsistencia): Long

    @Update
    suspend fun actualizarRegistro(registro: RegistroAsistencia)

    @Delete
    suspend fun eliminarRegistro(registro: RegistroAsistencia)

    @Query("SELECT SUM(horasTrabajadas) FROM registros_asistencia WHERE fecha LIKE :mes || '%'")
    suspend fun getTotalHorasMes(mes: String): Float?

    @Query("SELECT SUM(horasExtra) FROM registros_asistencia WHERE fecha LIKE :mes || '%'")
    suspend fun getTotalExtras(mes: String): Float?

    @Query("SELECT COUNT(*) FROM registros_asistencia WHERE fecha LIKE :mes || '%' AND horaEntrada IS NOT NULL")
    suspend fun getDiasAsistidosMes(mes: String): Int
}

@Dao
interface ConfiguracionDao {
    @Query("SELECT * FROM configuracion_horario WHERE id = 1")
    fun getConfiguracion(): LiveData<ConfiguracionHorario?>

    @Query("SELECT * FROM configuracion_horario WHERE id = 1")
    suspend fun getConfiguracionSync(): ConfiguracionHorario?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarConfiguracion(config: ConfiguracionHorario)
}
