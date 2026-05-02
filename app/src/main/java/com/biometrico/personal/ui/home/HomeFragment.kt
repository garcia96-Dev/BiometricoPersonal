package com.biometrico.personal.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.biometrico.personal.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var accionPendiente: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configurarUI()
        observarViewModel()
        actualizarReloj()
    }

    private fun configurarUI() {
        binding.btnEntrada.setOnClickListener {
            accionPendiente = { viewModel.registrarEntrada() }
            autenticar("Registrar Entrada", "Confirma tu identidad para registrar la entrada")
        }

        binding.btnSalida.setOnClickListener {
            accionPendiente = { viewModel.registrarSalida() }
            autenticar("Registrar Salida", "Confirma tu identidad para registrar la salida")
        }

        binding.btnRefrescar.setOnClickListener {
            viewModel.cargarDatos()
        }
    }

    private fun observarViewModel() {
        viewModel.estadoHoy.observe(viewLifecycleOwner) { estado ->
            actualizarUI(estado)
        }

        viewModel.registroHoy.observe(viewLifecycleOwner) { registro ->
            if (registro != null) {
                binding.tvHoraEntrada.text = registro.horaEntrada ?: "--:--"
                binding.tvHoraSalida.text = registro.horaSalida ?: "--:--"
                if (registro.horasTrabajadas > 0) {
                    binding.tvHorasTrabajadas.text = "${"%.1f".format(registro.horasTrabajadas)} horas"
                    binding.cardResumen.visibility = View.VISIBLE
                }
                if (registro.horasExtra > 0) {
                    binding.tvHorasExtra.text = "+${"%.1f".format(registro.horasExtra)}h extra"
                    binding.tvHorasExtra.visibility = View.VISIBLE
                }
            } else {
                binding.tvHoraEntrada.text = "--:--"
                binding.tvHoraSalida.text = "--:--"
                binding.cardResumen.visibility = View.GONE
            }
        }

        viewModel.configuracion.observe(viewLifecycleOwner) { config ->
            config?.let {
                binding.tvNombreTrabajador.text = if (it.nombreTrabajador.isNotEmpty())
                    it.nombreTrabajador else "Mi Biométrico"
                binding.tvHorarioConfig.text = "Horario: ${it.horaEntrada} - ${it.horaSalida}"
                binding.tvJornadaLegal.text = "Jornada: ${it.jornadaLey}h/semana (Ley 2101/2021)"
            }
        }

        viewModel.mensaje.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotEmpty()) {
                Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.cargando.observe(viewLifecycleOwner) { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        }
    }

    private fun actualizarUI(estado: EstadoRegistro) {
        when (estado) {
            EstadoRegistro.SIN_REGISTRO -> {
                binding.btnEntrada.isEnabled = true
                binding.btnSalida.isEnabled = false
                binding.tvEstado.text = "Sin registro hoy"
                binding.tvEstado.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                binding.ivEstadoIcon.setImageResource(android.R.drawable.ic_menu_recent_history)
            }
            EstadoRegistro.CON_ENTRADA -> {
                binding.btnEntrada.isEnabled = false
                binding.btnSalida.isEnabled = true
                binding.tvEstado.text = "En jornada laboral 🟢"
                binding.tvEstado.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                binding.ivEstadoIcon.setImageResource(android.R.drawable.presence_online)
            }
            EstadoRegistro.COMPLETO -> {
                binding.btnEntrada.isEnabled = false
                binding.btnSalida.isEnabled = false
                binding.tvEstado.text = "Jornada completada ✔️"
                binding.tvEstado.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark))
                binding.ivEstadoIcon.setImageResource(android.R.drawable.checkbox_on_background)
            }
        }
    }

    private fun autenticar(titulo: String, descripcion: String) {
        val config = viewModel.configuracion.value
        val usarHuella = config?.usarHuella ?: true

        if (!usarHuella) {
            accionPendiente?.invoke()
            return
        }

        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> mostrarDialogoBiometrico(titulo, descripcion)
            else -> {
                Toast.makeText(context, "Biométrico no disponible, registrando directamente", Toast.LENGTH_SHORT).show()
                accionPendiente?.invoke()
            }
        }
    }

    private fun mostrarDialogoBiometrico(titulo: String, descripcion: String) {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                accionPendiente?.invoke()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    Toast.makeText(context, "Error: $errString", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onAuthenticationFailed() {
                Toast.makeText(context, "Huella no reconocida", Toast.LENGTH_SHORT).show()
            }
        }

        val prompt = BiometricPrompt(this, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(titulo)
            .setSubtitle(descripcion)
            .setNegativeButtonText("Cancelar")
            .build()
        prompt.authenticate(info)
    }

    private fun actualizarReloj() {
        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale("es", "CO"))
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM yyyy", Locale("es", "CO"))

        binding.root.postDelayed(object : Runnable {
            override fun run() {
                if (_binding != null) {
                    binding.tvHoraActual.text = LocalTime.now().format(formatter)
                    binding.tvFechaActual.text = LocalDate.now().format(dateFormatter)
                        .replaceFirstChar { it.uppercase() }
                    binding.root.postDelayed(this, 1000)
                }
            }
        }, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
