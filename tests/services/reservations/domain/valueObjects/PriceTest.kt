package services.reservations.domain.valueObjects

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import reservations.domain.valueObjects.Price
import reservations.domain.valueObjects.Quantity

class PriceTest {

    @Test
    fun `deve criar preço válido`() {
        // Act
        val price = Price.of(BigDecimal("100.00"))

        // Assert
        assertEquals(BigDecimal("100.00"), price.value)
    }

    @Test
    fun `deve criar preço zero`() {
        // Act
        val price = Price.ZERO

        // Assert
        assertEquals(BigDecimal.ZERO, price.value)
    }

    @Test
    fun `deve criar preço a partir de string`() {
        // Act
        val price = Price.fromString("150.50")

        // Assert
        assertEquals(BigDecimal("150.50"), price.value)
    }

    @Test
    fun `deve falhar quando preço é negativo`() {
        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) {
                    Price.of(BigDecimal("-10.00"))
                }
        assertEquals("Preço deve ser maior ou igual a zero", exception.message)
    }

    @Test
    fun `deve falhar quando string de preço é inválida`() {
        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) { Price.fromString("invalid") }
        assertTrue(exception.message?.contains("Preço inválido") == true)
    }

    @Test
    fun `deve somar preços`() {
        // Arrange
        val price1 = Price.of(BigDecimal("100.00"))
        val price2 = Price.of(BigDecimal("50.00"))

        // Act
        val result = price1 + price2

        // Assert
        assertEquals(BigDecimal("150.00"), result.value)
    }

    @Test
    fun `deve multiplicar preço por quantidade int`() {
        // Arrange
        val price = Price.of(BigDecimal("100.00"))

        // Act
        val result = price * 3

        // Assert
        assertEquals(BigDecimal("300.00"), result.value)
    }

    @Test
    fun `deve multiplicar preço por Quantity`() {
        // Arrange
        val price = Price.of(BigDecimal("100.00"))
        val quantity = Quantity.of(3)

        // Act
        val result = price * quantity

        // Assert
        assertEquals(BigDecimal("300.00"), result.value)
    }
}
