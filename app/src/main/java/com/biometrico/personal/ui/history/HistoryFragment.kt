package com.biometrico.personal.ui.history

import android.app.AlertDialog
import android.app.Application
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.biometrico.personal.data.database.BiometricoDatabase
import com.biometrico.personal.data.model.RegistroAsistencia
import com.biometrico.personal.data.repository.BiometricoRepository
import com.biometrico.personal.data.repository.ResumenMes
import com.biometrico.personal.databinding.FragmentHistoryBinding
import com.biometrico.personal.ui.adapters.RegistroAdapter
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: RegistroAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarRecyclerView()
        observarDatos()
        configurarMesActual()
        configurarBotones()
    }

    private fun configurarRecyclerView() {
        adapter = RegistroAdapter(
            onEliminar = { registro -> confirmarEliminar(registro) }
        )
        binding.recyclerRegistros.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRegistros.adapter = adapter
    }

    private fun configurarMesActual() {
        val hoy = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "CO"))
        binding.tvMesActual.text = hoy.format(formatter).replaceFirstChar { it.uppercase() }
        viewModel.cargarMes(hoy.format(DateTimeFormatter.ofPattern("yyyy-MM")))
    }

    private fun configurarBotones() {
        binding.btnExportarCsv.setOnClickListener { exportarCSV() }
        binding.btnEntradaManual.setOnClickListener { mostrarDialogoEntradaManual() }
    }

    private fun observarDatos() {
        viewModel.registros.observe(viewLifecycleOwner) { registros ->
            adapter.submitList(registros)
            binding.tvSinRegistros.visibility = if (registros.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.resumen.observe(viewLifecycleOwner) { resumen ->
            resumen?.let {
                binding.tvTotalHoras.text = "${"%.1f".format(it.totalHoras)}h"
                binding.tvTotalExtras.text = "${"%.1f".format(it.totalExtras)}h extra"
                binding.tvDiasAsistidos.text = "${it.diasAsistidos} días"
            }
        }
        viewModel.mensaje.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    // === EXPORTAR CSV ===
    private fun exportarCSV() {
        val registros = viewModel.registros.value ?: emptyList()
        if (registros.isEmpty()) {
            Toast.makeText(context, "No hay registros para exportar", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val archivo = File(requireContext().cacheDir, "asistencia_biometrico.csv")
            val writer = FileWriter(archivo)
            writer.append("Fecha,Dia,Hora Entrada,Hora Salida,Horas Trabajadas,Horas Extra,Observaciones\n")
            val formatter = DateTimeFormatter.ofPattern("EEEE", Locale("es", "CO"))
            registros.forEach { r ->
                val fecha = LocalDate.parse(r.fecha)
                val dia = fecha.format(formatter).replaceFirstChar { it.uppercase() }
                writer.append("${r.fecha},$dia,${r.horaEntrada ?: ""},${r.horaSalida ?: ""},${"%.2f".format(r.horasTrabajadas)},${"%.2f".format(r.horasExtra)},${r.observaciones}\n")
            }
            writer.flush()
            writer.close()

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                archivo
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Registro de Asistencia - Biométrico Personal")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Exportar CSV via..."))
        } catch (e: Exception) {
            Toast.makeText(context, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // === ENTRADA MANUAL ===
    private fun mostrarDialogoEntradaManual() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val fecha = "%04d-%02d-%02d".format(year, month + 1, day)
            val fechaDisplay = "%02d/%02d/%04d".format(day, month + 1, year)
            TimePickerDialog(requireContext(), { _, hE, mE ->
                val horaEntrada = "%02d:%02d".format(hE, mE)
                TimePickerDialog(requireContext(), { _, hS, mS ->
                    val horaSalida = "%02d:%02d".format(hS, mS)
                    val etObs = EditText(requireContext()).apply {
                        hint = "Observaciones (opcional)"
                        setPadding(40, 20, 40, 20)
                    }
                    AlertDialog.Builder(requireContext())
                        .setTitle("📝 Confirmar entrada manual")
                        .setMessage("Fecha: $fechaDisplay\nEntrada: $horaEntrada\nSalida: $horaSalida")
                        .setView(etObs)
                        .setPositiveButton("Guardar") { _, _ ->
                            viewModel.guardarEntradaManual(fecha, horaEntrada, horaSalida, etObs.text.toString())
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).apply {
                    setTitle("🚪 Hora de salida"); show()
                }
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).apply {
                setTitle("🏢 Hora de entrada"); show()
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).apply {
            setTitle("📅 Selecciona la fecha"); show()
        }
    }

    // === ELIMINAR ===
    private fun confirmarEliminar(registro: RegistroAsistencia) {
        AlertDialog.Builder(requireContext())
            .setTitle("🗑️ Eliminar registro")
            .setMessage("¿Eliminar el registro del ${registro.fecha}?\n\nEntrada: ${registro.horaEntrada ?: "--"}\nSalida: ${registro.horaSalida ?: "--"}")
            .setPositiveButton("Eliminar") { _, _ -> viewModel.eliminarRegistro(registro) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BiometricoRepository = BiometricoRepository(
        BiometricoDatabase.getDatabase(application)
    )

    val registros = repository.todosRegistros
    val resumen = MutableLiveData<ResumenMes?>()
    val mensaje = MutableLiveData<String>()

    fun cargarMes(mes: String) {
        viewModelScope.launch {
            resumen.value = repository.getResumenMes(mes)
        }
    }

    fun guardarEntradaManual(fecha: String, horaEntrada: String, horaSalida: String, obs: String) {
        viewModelScope.launch {
            try {
                val config = repository.getConfiguracionSync()
                val fmt = DateTimeFormatter.ofPattern("HH:mm")
                val entrada = LocalTime.parse(horaEntrada, fmt)
                val salida = LocalTime.parse(horaSalida, fmt)
                var minutos = Duration.between(entrada, salida).toMinutes().toInt()
                minutos -= config.duracionAlmuerzo
                if (minutos < 0) minutos = 0
                val horasTrabajadas = minutos / 60f
                val horasJornada = if (config.usarJornadaPersonalizada)
                    config.horasSemanalesPersonalizadas / 5f
                else config.jornadaLey.toFloat() / 5f
                val horasExtra = maxOf(0f, horasTrabajadas - horasJornada)

                val registro = RegistroAsistencia(
                    fecha = fecha,
                    horaEntrada = horaEntrada,
                    horaSalida = horaSalida,
                    horasTrabajadas = horasTrabajadas,
                    horasExtra = horasExtra,
                    observaciones = obs
                )
                repository.insertarRegistro(registro)
                mensaje.value = "✅ Registro manual guardado"
            } catch (e: Exception) {
                mensaje.value = "❌ Error: ${e.message}"
            }
        }
    }

    fun eliminarRegistro(registro: RegistroAsistencia) {
        viewModelScope.launch {
            repository.eliminarRegistro(registro)
            mensaje.value = "🗑️ Registro eliminado"
        }
    }
}
