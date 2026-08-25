package com.example.imcapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ImcCalculatorTest {

    @Test
    fun `calcula el imc y el rango de peso ideal`() {
        val result = ImcCalculator.calculate(weightKg = 70.0, heightCm = 175.0)

        assertEquals(22.86, result.imc, 0.01)
        assertEquals(ImcCategory.NORMAL, result.category)
        assertEquals(56.66, result.idealWeightMin, 0.01)
        assertEquals(76.26, result.idealWeightMax, 0.01)
    }

    @Test
    fun `clasifica los limites de cada categoria`() {
        assertEquals(ImcCategory.BAJO_PESO, ImcCalculator.categoryOf(18.49))
        assertEquals(ImcCategory.NORMAL, ImcCalculator.categoryOf(18.5))
        assertEquals(ImcCategory.NORMAL, ImcCalculator.categoryOf(24.99))
        assertEquals(ImcCategory.SOBREPESO, ImcCalculator.categoryOf(25.0))
        assertEquals(ImcCategory.OBESIDAD_1, ImcCalculator.categoryOf(30.0))
        assertEquals(ImcCategory.OBESIDAD_2, ImcCalculator.categoryOf(35.0))
        assertEquals(ImcCategory.OBESIDAD_3, ImcCalculator.categoryOf(40.0))
    }

    @Test
    fun `la escala se mantiene entre 0 y 1`() {
        assertEquals(0f, ImcCalculator.scaleFraction(10.0), 0.001f)
        assertEquals(0.5f, ImcCalculator.scaleFraction(27.5), 0.001f)
        assertEquals(1f, ImcCalculator.scaleFraction(60.0), 0.001f)
    }

    @Test
    fun `acepta coma o punto como separador decimal`() {
        assertEquals(70.5, ImcCalculator.parseNumber("70,5")!!, 0.001)
        assertEquals(70.5, ImcCalculator.parseNumber(" 70.5 ")!!, 0.001)
        assertNull(ImcCalculator.parseNumber("abc"))
        assertNull(ImcCalculator.parseNumber(""))
    }
}
