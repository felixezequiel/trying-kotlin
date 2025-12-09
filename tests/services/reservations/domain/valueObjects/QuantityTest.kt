package services.reservations.domain.valueObjects

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import reservations.domain.valueObjects.Quantity

class QuantityTest {

    @Test
    fun `deve criar quantidade válida`() {
        // Act
        val quantity = Quantity.of(10)

        // Assert
        assertEquals(10, quantity.value)
    }

    @Test
    fun `deve criar quantidade zero`() {
        // Act
        val quantity = Quantity.ZERO

        // Assert
        assertEquals(0, quantity.value)
        assertTrue(quantity.isZero())
    }

    @Test
    fun `deve criar quantidade positiva`() {
        // Act
        val quantity = Quantity.positive(5)

        // Assert
        assertEquals(5, quantity.value)
    }

    @Test
    fun `deve falhar quando quantidade é negativa`() {
        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) { Quantity.of(-1) }
        assertEquals("Quantidade não pode ser negativa", exception.message)
    }

    @Test
    fun `deve falhar quando quantidade positiva é zero`() {
        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) { Quantity.positive(0) }
        assertEquals("Quantidade deve ser maior que zero", exception.message)
    }

    @Test
    fun `deve criar quantidade com mínimo`() {
        // Act
        val quantity = Quantity.atLeast(5, 3)

        // Assert
        assertEquals(5, quantity.value)
    }

    @Test
    fun `deve falhar quando quantidade é menor que mínimo`() {
        // Act & Assert
        val exception =
                assertThrows(IllegalArgumentException::class.java) { Quantity.atLeast(2, 5) }
        assertEquals("Quantidade deve ser pelo menos 5", exception.message)
    }

    @Test
    fun `deve somar quantidades`() {
        // Arrange
        val q1 = Quantity.of(10)
        val q2 = Quantity.of(5)

        // Act
        val result = q1 + q2

        // Assert
        assertEquals(15, result.value)
    }

    @Test
    fun `deve subtrair quantidades`() {
        // Arrange
        val q1 = Quantity.of(10)
        val q2 = Quantity.of(3)

        // Act
        val result = q1 - q2

        // Assert
        assertEquals(7, result.value)
    }

    @Test
    fun `deve falhar quando subtração resulta em negativo`() {
        // Arrange
        val q1 = Quantity.of(3)
        val q2 = Quantity.of(10)

        // Act & Assert
        val exception = assertThrows(IllegalArgumentException::class.java) { q1 - q2 }
        assertEquals("Resultado não pode ser negativo", exception.message)
    }

    @Test
    fun `deve comparar quantidades`() {
        // Arrange
        val q1 = Quantity.of(10)
        val q2 = Quantity.of(5)
        val q3 = Quantity.of(10)

        // Assert
        assertTrue(q1 > q2)
        assertTrue(q2 < q1)
        assertEquals(0, q1.compareTo(q3))
    }
}
