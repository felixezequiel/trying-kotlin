package partners.domain.valueObjects

import partners.domain.DocumentType

@JvmInline
value class Document private constructor(val value: String) {

    init {
        require(value.isNotBlank()) { "Documento não pode estar vazio" }
    }

    companion object {
        private val CPF_REGEX = Regex("^\\d{11}$")
        private val CNPJ_REGEX = Regex("^\\d{14}$")

        fun of(value: String, type: DocumentType): Document {
            val cleanValue = value.replace(Regex("[^0-9]"), "")

            when (type) {
                DocumentType.CPF -> {
                    require(CPF_REGEX.matches(cleanValue)) {
                        "CPF inválido. Deve conter 11 dígitos"
                    }
                    require(isValidCpf(cleanValue)) { "CPF inválido" }
                }
                DocumentType.CNPJ -> {
                    require(CNPJ_REGEX.matches(cleanValue)) {
                        "CNPJ inválido. Deve conter 14 dígitos"
                    }
                    require(isValidCnpj(cleanValue)) { "CNPJ inválido" }
                }
            }

            return Document(cleanValue)
        }

        private fun isValidCpf(cpf: String): Boolean {
            if (cpf.all { it == cpf[0] }) return false

            val digits = cpf.map { it.toString().toInt() }

            // Primeiro dígito verificador
            val sum1 = (0..8).sumOf { digits[it] * (10 - it) }
            val digit1 = if (sum1 % 11 < 2) 0 else 11 - (sum1 % 11)
            if (digits[9] != digit1) return false

            // Segundo dígito verificador
            val sum2 = (0..9).sumOf { digits[it] * (11 - it) }
            val digit2 = if (sum2 % 11 < 2) 0 else 11 - (sum2 % 11)
            return digits[10] == digit2
        }

        private fun isValidCnpj(cnpj: String): Boolean {
            if (cnpj.all { it == cnpj[0] }) return false

            val digits = cnpj.map { it.toString().toInt() }
            val weights1 = listOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
            val weights2 = listOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)

            // Primeiro dígito verificador
            val sum1 = weights1.mapIndexed { i, w -> digits[i] * w }.sum()
            val digit1 = if (sum1 % 11 < 2) 0 else 11 - (sum1 % 11)
            if (digits[12] != digit1) return false

            // Segundo dígito verificador
            val sum2 = weights2.mapIndexed { i, w -> digits[i] * w }.sum()
            val digit2 = if (sum2 % 11 < 2) 0 else 11 - (sum2 % 11)
            return digits[13] == digit2
        }
    }

    override fun toString(): String = value
}
