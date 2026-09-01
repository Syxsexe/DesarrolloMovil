package com.example.imcapp

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Una medición guardada en el historial. */
data class ImcRecord(
    val timestamp: Long,
    val weightKg: Double,
    val heightCm: Double,
    val gender: Gender,
    val imc: Double,
) {
    val category: ImcCategory get() = ImcCalculator.categoryOf(imc)

    fun formattedDate(): String = DATE_FORMAT.format(Instant.ofEpochMilli(timestamp))

    fun toJson(): JSONObject = JSONObject().apply {
        put(KEY_TIMESTAMP, timestamp)
        put(KEY_WEIGHT, weightKg)
        put(KEY_HEIGHT, heightCm)
        put(KEY_GENDER, gender.name)
        put(KEY_IMC, imc)
    }

    companion object {
        private const val KEY_TIMESTAMP = "timestamp"
        private const val KEY_WEIGHT = "weight"
        private const val KEY_HEIGHT = "height"
        private const val KEY_GENDER = "gender"
        private const val KEY_IMC = "imc"

        private val DATE_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm", Locale.forLanguageTag("es"))
                .withZone(ZoneId.systemDefault())

        fun from(result: ImcResult, weightKg: Double, heightCm: Double, timestamp: Long) = ImcRecord(
            timestamp = timestamp,
            weightKg = weightKg,
            heightCm = heightCm,
            gender = result.gender,
            imc = result.imc,
        )

        /** Devuelve null si el objeto guardado está incompleto o corrupto. */
        fun fromJson(json: JSONObject): ImcRecord? {
            val gender = Gender.fromKey(json.optString(KEY_GENDER)) ?: return null
            val weight = json.optDouble(KEY_WEIGHT)
            val height = json.optDouble(KEY_HEIGHT)
            val imc = json.optDouble(KEY_IMC)
            if (weight.isNaN() || height.isNaN() || imc.isNaN()) return null
            return ImcRecord(
                timestamp = json.optLong(KEY_TIMESTAMP),
                weightKg = weight,
                heightCm = height,
                gender = gender,
                imc = imc,
            )
        }
    }
}

/** Filtro por sexo aplicado a la lista del historial. */
enum class HistoryFilter(val gender: Gender?) {
    TODOS(null),
    HOMBRES(Gender.HOMBRE),
    MUJERES(Gender.MUJER);

    fun matches(record: ImcRecord): Boolean = gender == null || record.gender == gender
}

/**
 * Historial persistente de mediciones, guardado como JSON en `SharedPreferences`.
 * Se conservan las [MAX_RECORDS] más recientes, de la más nueva a la más antigua.
 */
class ImcHistoryStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun all(): List<ImcRecord> = decode(prefs.getString(KEY_RECORDS, null))

    fun add(record: ImcRecord) {
        save(listOf(record) + all())
    }

    fun remove(record: ImcRecord) {
        save(all().filterNot { it.timestamp == record.timestamp })
    }

    fun clear() {
        prefs.edit { remove(KEY_RECORDS) }
    }

    /** Último sexo elegido, para preseleccionarlo la próxima vez. */
    var lastGender: Gender?
        get() = Gender.fromKey(prefs.getString(KEY_LAST_GENDER, null))
        set(value) {
            prefs.edit { putString(KEY_LAST_GENDER, value?.name) }
        }

    /** Guarda siempre de la medición más reciente a la más antigua. */
    private fun save(records: List<ImcRecord>) {
        val array = JSONArray()
        records.sortedByDescending(ImcRecord::timestamp)
            .take(MAX_RECORDS)
            .forEach { array.put(it.toJson()) }
        prefs.edit { putString(KEY_RECORDS, array.toString()) }
    }

    companion object {
        const val MAX_RECORDS = 50

        private const val PREFS_NAME = "imc_history"
        private const val KEY_RECORDS = "records"
        private const val KEY_LAST_GENDER = "last_gender"

        @VisibleForTesting
        fun decode(raw: String?): List<ImcRecord> {
            if (raw.isNullOrBlank()) return emptyList()
            return runCatching {
                val array = JSONArray(raw)
                (0 until array.length()).mapNotNull { index ->
                    array.optJSONObject(index)?.let(ImcRecord::fromJson)
                }
            }.getOrDefault(emptyList())
        }
    }
}

/** Resumen que se muestra en la cabecera del historial. */
data class HistorySummary(val count: Int, val averageImc: Double?) {
    companion object {
        fun of(records: List<ImcRecord>) = HistorySummary(
            count = records.size,
            averageImc = records.takeIf { it.isNotEmpty() }?.map(ImcRecord::imc)?.average(),
        )
    }
}
