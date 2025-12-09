package partners.domain.valueObjects

@JvmInline
value class PartnerEmail private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "Email não pode estar vazio" }
        require(EMAIL_REGEX.matches(value)) { "Email inválido: $value" }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

        fun of(value: String): PartnerEmail = PartnerEmail(value.trim().lowercase())
    }

    override fun toString(): String = value
}
