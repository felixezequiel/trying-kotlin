package partners.domain.valueObjects

@JvmInline
value class Phone private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "Telefone não pode estar vazio" }
        require(value.length >= 10) { "Telefone deve ter pelo menos 10 dígitos" }
        require(value.length <= 15) { "Telefone deve ter no máximo 15 dígitos" }
    }

    companion object {
        fun of(value: String): Phone {
            val cleanValue = value.replace(Regex("[^0-9]"), "")
            require(cleanValue.length >= 10) { "Telefone deve ter pelo menos 10 dígitos" }
            require(cleanValue.length <= 15) { "Telefone deve ter no máximo 15 dígitos" }
            return Phone(cleanValue)
        }
    }

    fun formatted(): String {
        return when (value.length) {
            10 -> "(${value.substring(0, 2)}) ${value.substring(2, 6)}-${value.substring(6)}"
            11 -> "(${value.substring(0, 2)}) ${value.substring(2, 7)}-${value.substring(7)}"
            else -> value
        }
    }

    override fun toString(): String = value
}
