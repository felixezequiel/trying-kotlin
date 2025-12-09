package orders.domain.valueObjects

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class QuantityTest {

    @Test
    fun `deve criar Quantity com valor valido`() {
        val quantity = Quantity.of(5)
        assertEquals(5, quantity.value)
    }

    @Test
    fun `deve criar Quantity zero`() {
        val quantity = Quantity.ZERO
        assertEquals(0, quantity.value)
    }

    @Test
    fun `deve criar Quantity positiva`() {
        val quantity = Quantity.positive(3)
        assertEquals(3, quantity.value)
    }

    @Test
    fun `deve lancar excecao para Quantity positiva com zero`() {
        val exception = assertThrows<IllegalArgumentException> { Quantity.positive(0) }
        assertTrue(exception.message!!.contains("maior que zero"))
    }

    @Test
    fun `deve criar Quantity com minimo`() {
        val quantity = Quantity.atLeast(5, 3)
        assertEquals(5, quantity.value)
    }

    @Test
    fun `deve lancar excecao para valor abaixo do minimo`() {
        val exception = assertThrows<IllegalArgumentException> { Quantity.atLeast(2, 3) }
        assertTrue(exception.message!!.contains("pelo menos 3"))
    }

    @Test
    fun `deve lancar excecao para valor negativo`() {
        val exception = assertThrows<IllegalArgumentException> { Quantity.of(-1) }
        assertTrue(exception.message!!.contains("não pode ser negativa"))
    }

    @Test
    fun `deve somar duas quantidades`() {
        val q1 = Quantity.of(5)
        val q2 = Quantity.of(3)
        val result = q1 + q2
        assertEquals(8, result.value)
    }

    @Test
    fun `deve subtrair duas quantidades`() {
        val q1 = Quantity.of(5)
        val q2 = Quantity.of(3)
        val result = q1 - q2
        assertEquals(2, result.value)
    }

    @Test
    fun `deve lancar excecao para subtracao com resultado negativo`() {
        val q1 = Quantity.of(3)
        val q2 = Quantity.of(5)
        val exception = assertThrows<IllegalArgumentException> { q1 - q2 }
        assertTrue(exception.message!!.contains("não pode ser negativo"))
    }

    @Test
    fun `deve comparar quantidades`() {
        val q1 = Quantity.of(5)
        val q2 = Quantity.of(3)
        val q3 = Quantity.of(5)

        assertTrue(q1 > q2)
        assertTrue(q2 < q1)
        assertEquals(0, q1.compareTo(q3))
    }

    @Test
    fun `isZero deve retornar true para zero`() {
        assertTrue(Quantity.ZERO.isZero())
        assertFalse(Quantity.of(1).isZero())
    }

    @Test
    fun `toString deve retornar valor como string`() {
        val quantity = Quantity.of(10)
        assertEquals("10", quantity.toString())
    }
}
