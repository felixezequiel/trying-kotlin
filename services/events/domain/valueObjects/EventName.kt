package events.domain.valueObjects

@JvmInline
value class EventName private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "Nome do evento não pode estar vazio" }
        require(value.length >= 3) { "Nome do evento deve ter pelo menos 3 caracteres" }
        require(value.length <= 200) { "Nome do evento deve ter no máximo 200 caracteres" }
    }

    companion object {
        fun of(value: String): EventName = EventName(value.trim())
    }

    override fun toString(): String = value
}
