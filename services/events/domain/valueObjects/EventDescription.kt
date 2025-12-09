package events.domain.valueObjects

@JvmInline
value class EventDescription private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "Descrição do evento não pode estar vazia" }
        require(value.length <= 5000) { "Descrição do evento deve ter no máximo 5000 caracteres" }
    }

    companion object {
        fun of(value: String): EventDescription = EventDescription(value.trim())
    }

    override fun toString(): String = value
}
