package com.example.imcapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ImcHistoryTest {

    private fun record(imc: Double, gender: Gender, timestamp: Long = 0L) = ImcRecord(
        timestamp = timestamp,
        weightKg = 70.0,
        heightCm = 175.0,
        gender = gender,
        imc = imc,
    )

    @Test
    fun `la categoria del registro sale del imc guardado`() {
        assertEquals(ImcCategory.NORMAL, record(22.0, Gender.MUJER).category)
        assertEquals(ImcCategory.SOBREPESO, record(27.0, Gender.HOMBRE).category)
    }

    @Test
    fun `el filtro todos deja pasar cualquier medicion`() {
        assertTrue(HistoryFilter.TODOS.matches(record(22.0, Gender.HOMBRE)))
        assertTrue(HistoryFilter.TODOS.matches(record(22.0, Gender.MUJER)))
    }

    @Test
    fun `los filtros por sexo separan las mediciones`() {
        val hombre = record(22.0, Gender.HOMBRE)
        val mujer = record(22.0, Gender.MUJER)

        assertTrue(HistoryFilter.HOMBRES.matches(hombre))
        assertFalse(HistoryFilter.HOMBRES.matches(mujer))
        assertTrue(HistoryFilter.MUJERES.matches(mujer))
        assertFalse(HistoryFilter.MUJERES.matches(hombre))
    }

    @Test
    fun `el resumen promedia el imc de las mediciones visibles`() {
        val summary = HistorySummary.of(
            listOf(
                record(20.0, Gender.HOMBRE, timestamp = 1),
                record(24.0, Gender.MUJER, timestamp = 2),
            )
        )

        assertEquals(2, summary.count)
        assertEquals(22.0, summary.averageImc!!, 0.0001)
    }

    @Test
    fun `el resumen de una lista vacia no tiene promedio`() {
        val summary = HistorySummary.of(emptyList())

        assertEquals(0, summary.count)
        assertNull(summary.averageImc)
    }

    @Test
    fun `el sexo se reconstruye desde el nombre guardado`() {
        assertEquals(Gender.HOMBRE, Gender.fromKey("HOMBRE"))
        assertEquals(Gender.MUJER, Gender.fromKey("MUJER"))
        assertNull(Gender.fromKey("OTRO"))
        assertNull(Gender.fromKey(null))
        assertNull(Gender.fromKey(""))
    }
}
