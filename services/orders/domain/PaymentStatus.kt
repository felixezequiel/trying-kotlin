package orders.domain

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentStatus {
    PENDING, // Aguardando pagamento
    PROCESSING, // Processando no gateway
    PAID, // Pago com sucesso
    FAILED, // Falha no pagamento
    REFUNDED // Reembolsado
}
