package partners.domain.valueObjects

@JvmInline
value class CompanyName private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "Razão social não pode estar vazia" }
        require(value.length >= 2) { "Razão social deve ter pelo menos 2 caracteres" }
        require(value.length <= 200) { "Razão social deve ter no máximo 200 caracteres" }
    }

    companion object {
        fun of(value: String): CompanyName = CompanyName(value.trim())
    }

    override fun toString(): String = value
}
