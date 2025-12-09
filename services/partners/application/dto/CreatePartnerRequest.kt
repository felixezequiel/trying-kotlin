package partners.application.dto

import kotlinx.serialization.Serializable
import partners.domain.DocumentType

@Serializable
data class CreatePartnerRequest(
        val companyName: String,
        val tradeName: String? = null,
        val document: String,
        val documentType: DocumentType,
        val email: String,
        val phone: String
)
