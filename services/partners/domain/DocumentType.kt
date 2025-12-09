package partners.domain

import kotlinx.serialization.Serializable

@Serializable
enum class DocumentType {
    CPF,
    CNPJ
}
