package partners.domain

import kotlinx.serialization.Serializable

@Serializable
enum class PartnerStatus {
    PENDING, // Aguardando aprovação
    APPROVED, // Aprovado, pode criar eventos
    REJECTED, // Rejeitado
    SUSPENDED // Suspenso temporariamente
}
