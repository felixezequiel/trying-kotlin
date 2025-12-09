package orders.domain.valueObjects

@JvmInline
value class TicketCode private constructor(val value: String) {

    init {
        require(value.matches(PATTERN)) { "Código de ingresso inválido: $value" }
    }

    companion object {
        private val PATTERN = Regex("^TKT-[A-HJ-NP-Z2-9]{8}$")
        private const val CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Sem I, O, 0, 1 (confusos)

        fun generate(): TicketCode {
            val random = (1..8).map { CHARS.random() }.joinToString("")
            return TicketCode("TKT-$random")
        }

        fun of(value: String): TicketCode = TicketCode(value)
    }

    override fun toString(): String = value
}
