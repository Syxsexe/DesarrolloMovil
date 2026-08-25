package com.example.imcapp

import androidx.annotation.ColorRes
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

/** Resultado de un cálculo de IMC, con el rango de peso saludable para esa altura. */
data class ImcResult(
    val imc: Double,
    val category: ImcCategory,
    val idealWeightMin: Double,
    val idealWeightMax: Double,
)

object ImcCalculator {

    const val MIN_WEIGHT_KG = 1.0
    const val MAX_WEIGHT_KG = 1000.0
    const val MIN_HEIGHT_CM = 50.0
    const val MAX_HEIGHT_CM = 260.0

    /** Extremos de la escala visual que se dibuja en la tarjeta de resultado. */
    const val SCALE_MIN = 15.0
    const val SCALE_MAX = 40.0

    fun calculate(weightKg: Double, heightCm: Double): ImcResult {
        require(weightKg > 0 && heightCm > 0) { "Peso y altura deben ser mayores que cero" }
        val heightM = heightCm / 100.0
        val squaredHeight = heightM * heightM
        val imc = weightKg / squaredHeight
        return ImcResult(
            imc = imc,
            category = categoryOf(imc),
            idealWeightMin = 18.5 * squaredHeight,
            idealWeightMax = 24.9 * squaredHeight,
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

    /** Posición 0f..1f del IMC dentro de la escala visual. */
    fun scaleFraction(imc: Double): Float =
        (((imc - SCALE_MIN) / (SCALE_MAX - SCALE_MIN)).toFloat()).coerceIn(0f, 1f)

    /** Acepta coma o punto como separador decimal. */
    fun parseNumber(raw: String): Double? = raw.trim().replace(',', '.').toDoubleOrNull()
}
