package shared.valueObjects

/**
 * Value Object para strings que não podem estar vazias. Usado como base para outros Value Objects
 * de texto.
 */
@JvmInline
value class NonBlankString private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "Valor não pode estar vazio" }
    }

    companion object {
        fun of(value: String, fieldName: String = "Valor"): NonBlankString {
            require(value.isNotBlank()) { "$fieldName não pode estar vazio" }
            return NonBlankString(value.trim())
        }
    }

    override fun toString(): String = value
}
