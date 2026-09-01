package com.example.imcapp

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/** Categorías de IMC según los rangos de la OMS. */
enum class ImcCategory(
    @get:StringRes val labelRes: Int,
    @get:StringRes val messageRes: Int,
    @get:ColorRes val colorRes: Int,
) {
    BAJO_PESO(R.string.cat_bajo, R.string.msg_bajo, R.color.imc_bajo),
    NORMAL(R.string.cat_normal, R.string.msg_normal, R.color.imc_normal),
    SOBREPESO(R.string.cat_sobrepeso, R.string.msg_sobrepeso, R.color.imc_sobrepeso),
    OBESIDAD_1(R.string.cat_obesidad_1, R.string.msg_obesidad_1, R.color.imc_obesidad_1),
    OBESIDAD_2(R.string.cat_obesidad_2, R.string.msg_obesidad_2, R.color.imc_obesidad_2),
    OBESIDAD_3(R.string.cat_obesidad_3, R.string.msg_obesidad_3, R.color.imc_obesidad_3),
}

/**
 * Sexo biológico de la persona.
 *
 * Los rangos de IMC de la OMS son los mismos para hombres y mujeres, así que la
 * categoría no cambia. Lo que sí depende del sexo es el peso ideal calculado con
 * la fórmula de Lorentz y el porcentaje de grasa corporal de referencia.
 */
enum class Gender(
    @get:StringRes val labelRes: Int,
    @get:StringRes val bodyFatRes: Int,
    @get:DrawableRes val iconRes: Int,
    @get:ColorRes val colorRes: Int,
    /** Divisor de la fórmula de Lorentz: 4 en hombres, 2.5 en mujeres. */
    val lorentzDivisor: Double,
) {
    HOMBRE(R.string.gender_male, R.string.gender_male_body_fat, R.drawable.ic_male, R.color.gender_male, 4.0),
    MUJER(R.string.gender_female, R.string.gender_female_body_fat, R.drawable.ic_female, R.color.gender_female, 2.5);

    companion object {
        /** Convierte el nombre guardado en el historial, ignorando valores desconocidos. */
        fun fromKey(key: String?): Gender? = entries.firstOrNull { it.name == key }
    }
}

/** Resultado de un cálculo de IMC, con los rangos de peso saludable para esa altura. */
data class ImcResult(
    val imc: Double,
    val category: ImcCategory,
    val gender: Gender,
    val idealWeightMin: Double,
    val idealWeightMax: Double,
    /** Peso ideal según la fórmula de Lorentz, que sí distingue hombre y mujer. */
    val lorentzWeight: Double,
)

object ImcCalculator {

    const val MIN_WEIGHT_KG = 1.0
    const val MAX_WEIGHT_KG = 1000.0
    const val MIN_HEIGHT_CM = 50.0
    const val MAX_HEIGHT_CM = 260.0

    /** Extremos de la escala visual que se dibuja en la tarjeta de resultado. */
    const val SCALE_MIN = 15.0
    const val SCALE_MAX = 40.0

    fun calculate(weightKg: Double, heightCm: Double, gender: Gender): ImcResult {
        require(weightKg > 0 && heightCm > 0) { "Peso y altura deben ser mayores que cero" }
        val heightM = heightCm / 100.0
        val squaredHeight = heightM * heightM
        val imc = weightKg / squaredHeight
        return ImcResult(
            imc = imc,
            category = categoryOf(imc),
            gender = gender,
            idealWeightMin = 18.5 * squaredHeight,
            idealWeightMax = 24.9 * squaredHeight,
            lorentzWeight = lorentzWeight(heightCm, gender),
        )
    }

    fun categoryOf(imc: Double): ImcCategory = when {
        imc < 18.5 -> ImcCategory.BAJO_PESO
        imc < 25.0 -> ImcCategory.NORMAL
        imc < 30.0 -> ImcCategory.SOBREPESO
        imc < 35.0 -> ImcCategory.OBESIDAD_1
        imc < 40.0 -> ImcCategory.OBESIDAD_2
        else -> ImcCategory.OBESIDAD_3
    }

    /**
     * Fórmula de Lorentz: altura − 100 − (altura − 150) / divisor, donde el divisor
     * es 4 para hombres y 2.5 para mujeres. En alturas muy bajas el resultado se
     * limita al peso mínimo aceptado para no devolver valores sin sentido.
     */
    fun lorentzWeight(heightCm: Double, gender: Gender): Double =
        (heightCm - 100.0 - (heightCm - 150.0) / gender.lorentzDivisor).coerceAtLeast(MIN_WEIGHT_KG)

    /** Posición 0f..1f del IMC dentro de la escala visual. */
    fun scaleFraction(imc: Double): Float =
        (((imc - SCALE_MIN) / (SCALE_MAX - SCALE_MIN)).toFloat()).coerceIn(0f, 1f)

    /** Acepta coma o punto como separador decimal. */
    fun parseNumber(raw: String): Double? = raw.trim().replace(',', '.').toDoubleOrNull()
}
