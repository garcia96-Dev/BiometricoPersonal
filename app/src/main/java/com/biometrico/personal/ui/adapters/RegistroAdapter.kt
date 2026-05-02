package com.biometrico.personal.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.biometrico.personal.data.model.RegistroAsistencia
import com.biometrico.personal.databinding.ItemRegistroBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RegistroAdapter : ListAdapter<RegistroAsistencia, RegistroAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRegistroBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemRegistroBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(registro: RegistroAsistencia) {
            val fecha = LocalDate.parse(registro.fecha)
            val formatter = DateTimeFormatter.ofPattern("EEE dd/MM", Locale("es", "CO"))
            binding.tvFecha.text = fecha.format(formatter).replaceFirstChar { it.uppercase() }

            binding.tvEntrada.text = registro.horaEntrada ?: "--:--"
            binding.tvSalida.text = registro.horaSalida ?: "--:--"

            if (registro.horasTrabajadas > 0) {
                binding.tvHoras.text = "${"%.1f".format(registro.horasTrabajadas)}h"
            } else {
                binding.tvHoras.text = "- -"
            }

            if (registro.horasExtra > 0) {
                binding.tvExtra.text = "+${"%.1f".format(registro.horasExtra)}h"
                binding.tvExtra.visibility = android.view.View.VISIBLE
            } else {
                binding.tvExtra.visibility = android.view.View.GONE
            }

            // Color según estado
            val colorRes = when {
                registro.horaSalida != null -> android.R.color.holo_green_light
                registro.horaEntrada != null -> android.R.color.holo_orange_light
                else -> android.R.color.darker_gray
            }
            binding.viewIndicador.setBackgroundColor(
                binding.root.context.getColor(colorRes)
            )
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<RegistroAsistencia>() {
        override fun areItemsTheSame(a: RegistroAsistencia, b: RegistroAsistencia) = a.id == b.id
        override fun areContentsTheSame(a: RegistroAsistencia, b: RegistroAsistencia) = a == b
    }
}
