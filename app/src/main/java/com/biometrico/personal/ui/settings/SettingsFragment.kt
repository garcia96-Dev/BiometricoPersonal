package com.biometrico.personal.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.biometrico.personal.R
import com.biometrico.personal.data.model.ConfiguracionHorario
import com.biometrico.personal.databinding.FragmentSettingsBinding
import java.time.LocalDate

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()
    private var configActual: ConfiguracionHorario = ConfiguracionHorario()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarSpinnerJornada()
        observarConfiguracion()
        configurarListeners()
        mostrarJornadaVigente()
    }

    private fun mostrarJornadaVigente() {
        val hoy = LocalDate.now()
        val jornadaVigente = when {
            hoy >= LocalDate.of(2026, 7, 15) -> "42 horas/semana (desde Jul 2026)"
            hoy >= LocalDate.of(2025, 7, 15) -> "44 horas/semana (desde Jul 2025) ← VIGENTE"
            hoy >= LocalDate.of(2024, 7, 15) -> "46 horas/semana (desde Jul 2024)"
            hoy >= LocalDate.of(2023, 7, 15) -> "47 horas/semana (desde Jul 2023)"
            else -> "48 horas/semana (anterior)"
        }
        binding.tvJornadaVigente.text = "📋 Jornada legal vigente hoy: $jornadaVigente"
    }

    private fun configurarSpinnerJornada() {
        val opciones = viewModel.jornadasLegales.map { "${it.label} — ${it.descripcion}" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, opciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerJornada.adapter = adapter
    }

    private fun observarConfiguracion() {
        viewModel.configuracion.observe(viewLifecycleOwner) { config ->
            config?.let {
                configActual = it
                llenarFormulario(it)
            }
        }
    }

    private fun llenarFormulario(config: ConfiguracionHorario) {
        // Datos personales
        binding.etNombreTrabajador.setText(config.nombreTrabajador)
        binding.etEmpresa.setText(config.empresaNombre)
        binding.etCargo.setText(config.cargo)

        // Jornada legal
        val idx = viewModel.jornadasLegales.indexOfFirst { it.horas == config.jornadaLey }
        if (idx >= 0) binding.spinnerJornada.setSelection(idx)
        binding.switchJornadaPersonalizada.isChecked = config.usarJornadaPersonalizada
        binding.etHorasPersonalizadas.setText(config.horasSemanalesPersonalizadas.toInt().toString())
        binding.layoutHorasPersonalizadas.visibility =
            if (config.usarJornadaPersonalizada) View.VISIBLE else View.GONE

        // Horario diario
        binding.tvHoraEntrada.text = config.horaEntrada
        binding.tvHoraSalida.text = config.horaSalida
        binding.sliderAlmuerzo.value = config.duracionAlmuerzo.toFloat()
        binding.tvAlmuerzoValor.text = "${config.duracionAlmuerzo} min"

        // Días laborales
        binding.checkLunes.isChecked = config.trabajaLunes
        binding.checkMartes.isChecked = config.trabajaMartes
        binding.checkMiercoles.isChecked = config.trabajaMiercoles
        binding.checkJueves.isChecked = config.trabajaJueves
        binding.checkViernes.isChecked = config.trabajaViernes
        binding.checkSabado.isChecked = config.trabajaSabado
        binding.checkDomingo.isChecked = config.trabajaDomingo

        // Tolerancias
        binding.sliderToleranciaEntrada.value = config.toleranciaEntradaMin.toFloat()
        binding.tvToleranciaEntradaValor.text = "${config.toleranciaEntradaMin} min"
        binding.sliderToleranciaSalida.value = config.toleranciaSalidaMin.toFloat()
        binding.tvToleranciaSalidaValor.text = "${config.toleranciaSalidaMin} min"

        // Recargos
        binding.tvInicioNocturna.text = config.inicioJornadaNocturna
        binding.tvFinNocturna.text = config.finJornadaNocturna

        // Biométrico
        binding.switchHuella.isChecked = config.usarHuella
        binding.switchPin.isChecked = config.usarPin

        // Notificaciones
        binding.switchNotifEntrada.isChecked = config.notificarEntrada
        binding.switchNotifSalida.isChecked = config.notificarSalida
        binding.sliderMinutosNotif.value = config.minutosAntesNotificacion.toFloat()
        binding.tvMinutosNotifValor.text = "${config.minutosAntesNotificacion} min antes"
    }

    private fun configurarListeners() {
        // Jornada personalizada toggle
        binding.switchJornadaPersonalizada.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutHorasPersonalizadas.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Time pickers
        binding.btnPickerEntrada.setOnClickListener {
            mostrarTimePicker(configActual.horaEntrada) { hora ->
                binding.tvHoraEntrada.text = hora
            }
        }
        binding.btnPickerSalida.setOnClickListener {
            mostrarTimePicker(configActual.horaSalida) { hora ->
                binding.tvHoraSalida.text = hora
            }
        }
        binding.btnPickerInicioNocturna.setOnClickListener {
            mostrarTimePicker(configActual.inicioJornadaNocturna) { hora ->
                binding.tvInicioNocturna.text = hora
            }
        }
        binding.btnPickerFinNocturna.setOnClickListener {
            mostrarTimePicker(configActual.finJornadaNocturna) { hora ->
                binding.tvFinNocturna.text = hora
            }
        }

        // Sliders
        binding.sliderAlmuerzo.addOnChangeListener { _, value, _ ->
            binding.tvAlmuerzoValor.text = "${value.toInt()} min"
        }
        binding.sliderToleranciaEntrada.addOnChangeListener { _, value, _ ->
            binding.tvToleranciaEntradaValor.text = "${value.toInt()} min"
        }
        binding.sliderToleranciaSalida.addOnChangeListener { _, value, _ ->
            binding.tvToleranciaSalidaValor.text = "${value.toInt()} min"
        }
        binding.sliderMinutosNotif.addOnChangeListener { _, value, _ ->
            binding.tvMinutosNotifValor.text = "${value.toInt()} min antes"
        }

        // Botón guardar
        binding.btnGuardar.setOnClickListener {
            guardarConfiguracion()
        }

        // Botón reset recargos Colombia
        binding.btnResetRecargos.setOnClickListener {
            resetearRecargosLegales()
        }
    }

    private fun mostrarTimePicker(horaActual: String, callback: (String) -> Unit) {
        val partes = horaActual.split(":")
        val hora = partes[0].toInt()
        val minuto = partes[1].toInt()
        TimePickerDialog(requireContext(), { _, h, m ->
            callback("%02d:%02d".format(h, m))
        }, hora, minuto, true).show()
    }

    private fun resetearRecargosLegales() {
        Toast.makeText(context,
            "Recargos restablecidos según Ley 2101 y Reforma Laboral 2024",
            Toast.LENGTH_LONG).show()
        binding.tvInicioNocturna.text = "19:00"
        binding.tvFinNocturna.text = "06:00"
    }

    private fun guardarConfiguracion() {
        val jornadaIdx = binding.spinnerJornada.selectedItemPosition
        val jornadaSeleccionada = viewModel.jornadasLegales[jornadaIdx].horas

        val config = configActual.copy(
            nombreTrabajador = binding.etNombreTrabajador.text.toString(),
            empresaNombre = binding.etEmpresa.text.toString(),
            cargo = binding.etCargo.text.toString(),

            jornadaLey = jornadaSeleccionada,
            usarJornadaPersonalizada = binding.switchJornadaPersonalizada.isChecked,
            horasSemanalesPersonalizadas = binding.etHorasPersonalizadas.text.toString()
                .toFloatOrNull() ?: 44f,

            horaEntrada = binding.tvHoraEntrada.text.toString(),
            horaSalida = binding.tvHoraSalida.text.toString(),
            duracionAlmuerzo = binding.sliderAlmuerzo.value.toInt(),

            trabajaLunes = binding.checkLunes.isChecked,
            trabajaMartes = binding.checkMartes.isChecked,
            trabajaMiercoles = binding.checkMiercoles.isChecked,
            trabajaJueves = binding.checkJueves.isChecked,
            trabajaViernes = binding.checkViernes.isChecked,
            trabajaSabado = binding.checkSabado.isChecked,
            trabajaDomingo = binding.checkDomingo.isChecked,

            toleranciaEntradaMin = binding.sliderToleranciaEntrada.value.toInt(),
            toleranciaSalidaMin = binding.sliderToleranciaSalida.value.toInt(),

            inicioJornadaNocturna = binding.tvInicioNocturna.text.toString(),
            finJornadaNocturna = binding.tvFinNocturna.text.toString(),

            usarHuella = binding.switchHuella.isChecked,
            usarPin = binding.switchPin.isChecked,

            notificarEntrada = binding.switchNotifEntrada.isChecked,
            notificarSalida = binding.switchNotifSalida.isChecked,
            minutosAntesNotificacion = binding.sliderMinutosNotif.value.toInt()
        )

        viewModel.guardarConfiguracion(config)
        Toast.makeText(context, "✅ Configuración guardada", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
