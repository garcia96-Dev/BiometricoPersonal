package com.biometrico.personal.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biometrico.personal.data.database.BiometricoDatabase
import com.biometrico.personal.data.model.ConfiguracionHorario
import com.biometrico.personal.data.repository.BiometricoRepository
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BiometricoRepository = BiometricoRepository(
        BiometricoDatabase.getDatabase(application)
    )

    val configuracion = repository.configuracion

    val jornadasLegales = listOf(
        JornadaLegal("48h (antes 2023)", "48", "Jornada anterior a la Ley 2101"),
        JornadaLegal("47h (Jul 2023)", "47", "Reducción 1ª etapa"),
        JornadaLegal("46h (Jul 2024)", "46", "Reducción 2ª etapa"),
        JornadaLegal("44h (Jul 2025)", "44", "Reducción 3ª etapa ← Actual"),
        JornadaLegal("42h (Jul 2026)", "42", "Reducción final - meta de la ley")
    )

    fun guardarConfiguracion(config: ConfiguracionHorario) {
        viewModelScope.launch {
            repository.guardarConfiguracion(config)
        }
    }
}

data class JornadaLegal(
    val label: String,
    val horas: String,
    val descripcion: String
)
