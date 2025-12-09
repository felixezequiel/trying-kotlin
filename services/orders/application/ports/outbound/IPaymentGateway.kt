package orders.application.ports.outbound

import java.math.BigDecimal
import java.util.UUID

interface IPaymentGateway {
    suspend fun processPayment(request: PaymentRequest): PaymentResult
    suspend fun refund(transactionId: String, amount: BigDecimal): RefundResult
}

data class PaymentRequest(
        val orderId: UUID,
        val amount: BigDecimal,
        val paymentMethod: String,
        val customerEmail: String
)

data class PaymentResult(
        val success: Boolean,
        val transactionId: String?,
        val errorCode: String?,
        val errorMessage: String?
)

data class RefundResult(val success: Boolean, val refundId: String?, val errorMessage: String?)
