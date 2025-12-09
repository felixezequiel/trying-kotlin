package tickets.domain.valueObjects

@JvmInline
value class TicketName private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "Nome do ingresso não pode estar vazio" }
        require(value.length >= 2) { "Nome do ingresso deve ter pelo menos 2 caracteres" }
        require(value.length <= 100) { "Nome do ingresso deve ter no máximo 100 caracteres" }
    }

    companion object {
        fun of(value: String): TicketName = TicketName(value.trim())
    }

    override fun toString(): String = value
}
