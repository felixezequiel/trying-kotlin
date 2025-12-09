package orders.domain.valueObjects

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PriceTest {

    @Test
    fun `deve criar Price com valor valido`() {
        val price = Price.of(BigDecimal("100.00"))
        assertEquals(BigDecimal("100.00"), price.value)
    }

    @Test
    fun `deve criar Price zero`() {
        val price = Price.ZERO
        assertEquals(BigDecimal.ZERO, price.value)
    }

    @Test
    fun `deve criar Price a partir de string`() {
        val price = Price.fromString("50.99")
        assertEquals(BigDecimal("50.99"), price.value)
    }

    @Test
    fun `deve lancar excecao para string invalida`() {
        val exception = assertThrows<IllegalArgumentException> { Price.fromString("invalido") }
        assertTrue(exception.message!!.contains("Preço inválido"))
    }

    @Test
    fun `deve lancar excecao para valor negativo`() {
        val exception = assertThrows<IllegalArgumentException> { Price.of(BigDecimal("-10.00")) }
        assertTrue(exception.message!!.contains("maior ou igual a zero"))
    }

    @Test
    fun `deve somar dois precos`() {
        val price1 = Price.of(BigDecimal("100.00"))
        val price2 = Price.of(BigDecimal("50.00"))
        val result = price1 + price2
        assertEquals(BigDecimal("150.00"), result.value)
    }

    @Test
    fun `deve multiplicar preco por quantidade inteira`() {
        val price = Price.of(BigDecimal("25.00"))
        val result = price * 3
        assertEquals(BigDecimal("75.00"), result.value)
    }

    @Test
    fun `deve multiplicar preco por Quantity`() {
        val price = Price.of(BigDecimal("25.00"))
        val quantity = Quantity.of(4)
        val result = price * quantity
        assertEquals(BigDecimal("100.00"), result.value)
    }

    @Test
    fun `toString deve retornar valor como string`() {
        val price = Price.of(BigDecimal("99.99"))
        assertEquals("99.99", price.toString())
    }
}
