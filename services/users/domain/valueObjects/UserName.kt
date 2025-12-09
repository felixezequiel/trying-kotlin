package users.domain.valueObjects

@JvmInline
value class UserName private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "Nome não pode estar vazio" }
        require(value.length >= 2) { "Nome deve ter pelo menos 2 caracteres" }
        require(value.length <= 100) { "Nome deve ter no máximo 100 caracteres" }
    }

    companion object {
        fun of(value: String): UserName = UserName(value.trim())
    }

    override fun toString(): String = value
}
