package orders.domain

import kotlinx.serialization.Serializable

@Serializable
enum class TicketStatus {
    VALID, // Válido para uso
    USED, // Já utilizado
    CANCELLED // Cancelado (reembolso)
}
