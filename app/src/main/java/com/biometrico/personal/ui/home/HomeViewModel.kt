package com.biometrico.personal.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.biometrico.personal.data.database.BiometricoDatabase
import com.biometrico.personal.data.model.RegistroAsistencia
import com.biometrico.personal.data.repository.BiometricoRepository
import kotlinx.coroutines.launch

enum class EstadoRegistro {
    SIN_REGISTRO, CON_ENTRADA, COMPLETO
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BiometricoRepository
    val configuracion = MutableLiveData<com.biometrico.personal.data.model.ConfiguracionHorario>()

    private val _registroHoy = MutableLiveData<RegistroAsistencia?>()
    val registroHoy: LiveData<RegistroAsistencia?> = _registroHoy

    private val _estadoHoy = MutableLiveData<EstadoRegistro>()
    val estadoHoy: LiveData<EstadoRegistro> = _estadoHoy

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> = _cargando

    init {
        val db = BiometricoDatabase.getDatabase(application)
        repository = BiometricoRepository(db)
        cargarDatos()
        repository.configuracion.observeForever { config ->
            config?.let { configuracion.value = it }
        }
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _cargando.value = true
            val registro = repository.getRegistroHoy()
            _registroHoy.value = registro
            _estadoHoy.value = when {
                registro == null -> EstadoRegistro.SIN_REGISTRO
                registro.horaSalida == null -> EstadoRegistro.CON_ENTRADA
                else -> EstadoRegistro.COMPLETO
            }
            _cargando.value = false
        }
    }

    fun registrarEntrada() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val registro = repository.registrarEntrada()
                _registroHoy.value = registro
                _estadoHoy.value = EstadoRegistro.CON_ENTRADA
                _mensaje.value = "✅ Entrada registrada: ${registro.horaEntrada}"
            } catch (e: Exception) {
                _mensaje.value = "❌ Error al registrar entrada: ${e.message}"
            }
            _cargando.value = false
        }
    }

    fun registrarSalida() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val config = repository.getConfiguracionSync()
                val registro = repository.registrarSalida(config)
                if (registro != null) {
                    _registroHoy.value = registro
                    _estadoHoy.value = EstadoRegistro.COMPLETO
                    val extras = if (registro.horasExtra > 0)
                        " | Extras: ${"%.1f".format(registro.horasExtra)}h" else ""
                    _mensaje.value = "✅ Salida registrada: ${registro.horaSalida} | Trabajadas: ${"%.1f".format(registro.horasTrabajadas)}h$extras"
                } else {
                    _mensaje.value = "⚠️ Primero registra la entrada"
                }
            } catch (e: Exception) {
                _mensaje.value = "❌ Error al registrar salida: ${e.message}"
            }
            _cargando.value = false
        }
    }
}
