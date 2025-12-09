package partners.domain

import java.time.Instant
import java.util.UUID

data class Partner(
        val id: UUID = UUID.randomUUID(),
        val userId: Long, // Referência ao User
        val companyName: String,
        val tradeName: String?, // Nome fantasia
        val document: String, // CNPJ ou CPF
        val documentType: DocumentType,
        val email: String, // Email comercial
        val phone: String,
        val status: PartnerStatus = PartnerStatus.PENDING,
        val rejectionReason: String? = null,
        val createdAt: Instant = Instant.now(),
        val approvedAt: Instant? = null,
        val updatedAt: Instant = Instant.now()
)
