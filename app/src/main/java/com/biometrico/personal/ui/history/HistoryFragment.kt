package com.biometrico.personal.ui.history

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.biometrico.personal.data.database.BiometricoDatabase
import com.biometrico.personal.data.model.RegistroAsistencia
import com.biometrico.personal.data.repository.BiometricoRepository
import com.biometrico.personal.databinding.FragmentHistoryBinding
import com.biometrico.personal.ui.adapters.RegistroAdapter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    }

    private fun configurarRecyclerView() {
        adapter = RegistroAdapter()
        binding.recyclerRegistros.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerRegistros.adapter = adapter
    }

    private fun configurarMesActual() {
        val hoy = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "CO"))
        binding.tvMesActual.text = hoy.format(formatter).replaceFirstChar { it.uppercase() }
        viewModel.cargarMes(hoy.format(DateTimeFormatter.ofPattern("yyyy-MM")))
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BiometricoRepository
    val registros = repository.run {
        val db = BiometricoDatabase.getDatabase(application)
        BiometricoRepository(db).also { repository = it }.todosRegistros
    }

    val resumen = androidx.lifecycle.MutableLiveData<com.biometrico.personal.data.repository.ResumenMes?>()

    fun cargarMes(mes: String) {
        viewModelScope.launch {
            resumen.value = repository.getResumenMes(mes)
        }
    }
}
