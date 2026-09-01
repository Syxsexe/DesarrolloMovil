package com.example.imcapp

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.util.Locale

/** Lista de mediciones del historial. Cada fila se colorea según el sexo y la categoría. */
class HistoryAdapter(
    private val onDelete: (ImcRecord) -> Unit,
) : ListAdapter<ImcRecord, HistoryAdapter.RecordViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(getItem(position), onDelete)
    }

    class RecordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val genderIcon: ImageView = itemView.findViewById(R.id.itemGenderIcon)
        private val imc: TextView = itemView.findViewById(R.id.itemImc)
        private val category: TextView = itemView.findViewById(R.id.itemCategory)
        private val data: TextView = itemView.findViewById(R.id.itemData)
        private val date: TextView = itemView.findViewById(R.id.itemDate)
        private val delete: MaterialButton = itemView.findViewById(R.id.itemDelete)

        fun bind(record: ImcRecord, onDelete: (ImcRecord) -> Unit) {
            val context = itemView.context
            val genderColor = ContextCompat.getColor(context, record.gender.colorRes)
            val categoryColor = ContextCompat.getColor(context, record.category.colorRes)

            genderIcon.setImageResource(record.gender.iconRes)
            genderIcon.backgroundTintList = ColorStateList.valueOf(genderColor)
            genderIcon.contentDescription = context.getString(record.gender.labelRes)

            imc.text = format(record.imc)
            imc.setTextColor(categoryColor)
            category.text = context.getString(record.category.labelRes)
            category.backgroundTintList = ColorStateList.valueOf(categoryColor)

            data.text = context.getString(
                R.string.history_item_data,
                format(record.weightKg),
                format(record.heightCm),
            )
            date.text = record.formattedDate()

            delete.setOnClickListener { onDelete(record) }
        }

        private fun format(value: Double) = String.format(Locale.US, "%.1f", value)
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<ImcRecord>() {
            override fun areItemsTheSame(oldItem: ImcRecord, newItem: ImcRecord) =
                oldItem.timestamp == newItem.timestamp

            override fun areContentsTheSame(oldItem: ImcRecord, newItem: ImcRecord) =
                oldItem == newItem
        }
    }
}
