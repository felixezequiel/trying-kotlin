package orders.domain.valueObjects

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TicketCodeTest {

    @Test
    fun `deve gerar codigo valido`() {
        val code = TicketCode.generate()
        assertTrue(code.value.startsWith("TKT-"))
        assertEquals(12, code.value.length) // TKT- + 8 chars
    }

    @Test
    fun `deve criar codigo a partir de string valida`() {
        val code = TicketCode.of("TKT-ABCD2345")
        assertEquals("TKT-ABCD2345", code.value)
    }

    @Test
    fun `deve lancar excecao para codigo invalido`() {
        val exception = assertThrows<IllegalArgumentException> { TicketCode.of("INVALID") }
        assertTrue(exception.message!!.contains("Código de ingresso inválido"))
    }

    @Test
    fun `deve lancar excecao para codigo com caracteres confusos`() {
        // I, O, 0, 1 não são permitidos
        assertThrows<IllegalArgumentException> { TicketCode.of("TKT-ABCD1230") } // 0 não permitido
        assertThrows<IllegalArgumentException> { TicketCode.of("TKT-ABCD123I") } // I não permitido
        assertThrows<IllegalArgumentException> { TicketCode.of("TKT-ABCD123O") } // O não permitido
        assertThrows<IllegalArgumentException> { TicketCode.of("TKT-ABCD1231") } // 1 não permitido
    }

    @Test
    fun `codigos gerados devem ser unicos`() {
        val codes = (1..100).map { TicketCode.generate() }.map { it.value }
        assertEquals(100, codes.toSet().size)
    }

    @Test
    fun `toString deve retornar o valor`() {
        val code = TicketCode.of("TKT-ABCD2345")
        assertEquals("TKT-ABCD2345", code.toString())
    }
}
