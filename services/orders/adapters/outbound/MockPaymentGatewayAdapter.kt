package orders.adapters.outbound

import java.math.BigDecimal
import java.util.UUID
import kotlin.random.Random
import kotlinx.coroutines.delay
import orders.application.ports.outbound.IPaymentGateway
import orders.application.ports.outbound.PaymentRequest
import orders.application.ports.outbound.PaymentResult
import orders.application.ports.outbound.RefundResult

class MockPaymentGatewayAdapter : IPaymentGateway {

    override suspend fun processPayment(request: PaymentRequest): PaymentResult {
        // Simula delay de processamento
        delay(500)

        // Simula 95% de sucesso
        return if (Random.nextFloat() < 0.95f) {
            PaymentResult(
                    success = true,
                    transactionId = "TXN-${UUID.randomUUID().toString().take(8).uppercase()}",
                    errorCode = null,
                    errorMessage = null
            )
        } else {
            PaymentResult(
                    success = false,
                    transactionId = null,
                    errorCode = "DECLINED",
                    errorMessage = "Payment declined (mock)"
            )
        }
    }

    override suspend fun refund(transactionId: String, amount: BigDecimal): RefundResult {
        delay(300)
        return RefundResult(
                success = true,
                refundId = "REF-${UUID.randomUUID().toString().take(8).uppercase()}",
                errorMessage = null
        )
    }
}
