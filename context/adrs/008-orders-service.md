# ADR-008: Orders Service

## Status

Aceito

## Data

2024-12-09

## Contexto

O sistema de ingressos ([Intent](../intent/ticket-system.md)) requer processamento de pedidos, pagamentos e emissão de ingressos.

### Responsabilidades

- Converter reserva em pedido
- Processar pagamento (mock inicialmente)
- Emitir ingressos com código único
- Gestão de reembolsos

## Decisão

Criar microserviço `orders` na porta **8086**.

### Modelo de Domínio

```kotlin
// domain/Order.kt
data class Order(
    val id: UUID,
    val customerId: UUID,
    val reservationId: UUID,        // Referência à Reservation
    val eventId: UUID,              // Desnormalizado
    val items: List<OrderItem>,
    val totalAmount: BigDecimal,
    val paymentStatus: PaymentStatus,
    val paymentMethod: String?,     // "credit_card", "pix", etc
    val transactionId: String?,     // ID do gateway
    val createdAt: Instant,
    val paidAt: Instant?,
    val refundedAt: Instant?
)

// domain/OrderItem.kt
data class OrderItem(
    val id: UUID,
    val ticketTypeId: UUID,
    val ticketTypeName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)

// domain/PaymentStatus.kt
enum class PaymentStatus {
    PENDING,    // Aguardando pagamento
    PROCESSING, // Processando no gateway
    PAID,       // Pago com sucesso
    FAILED,     // Falha no pagamento
    REFUNDED    // Reembolsado
}

// domain/IssuedTicket.kt
data class IssuedTicket(
    val id: UUID,
    val orderId: UUID,
    val orderItemId: UUID,
    val ticketTypeId: UUID,
    val ticketTypeName: String,
    val eventId: UUID,
    val eventName: String,          // Desnormalizado
    val customerId: UUID,
    val code: String,               // Código único (ex: "TKT-ABC123XYZ")
    val qrCode: String,             // Dados para QR Code
    val status: TicketStatus,
    val issuedAt: Instant,
    val usedAt: Instant?
)

// domain/TicketStatus.kt
enum class TicketStatus {
    VALID,      // Válido para uso
    USED,       // Já utilizado
    CANCELLED   // Cancelado (reembolso)
}
```

### Estrutura do Serviço

```
services/orders/
├── domain/
│   ├── Order.kt
│   ├── OrderItem.kt
│   ├── PaymentStatus.kt
│   ├── IssuedTicket.kt
│   └── TicketStatus.kt
├── application/
│   ├── dto/
│   │   ├── CreateOrderRequest.kt
│   │   ├── OrderResponse.kt
│   │   ├── ProcessPaymentRequest.kt
│   │   ├── PaymentResponse.kt
│   │   ├── IssuedTicketResponse.kt
│   │   └── RefundRequest.kt
│   ├── ports/outbound/
│   │   ├── IOrderRepository.kt
│   │   ├── IIssuedTicketRepository.kt
│   │   ├── IPaymentGateway.kt
│   │   └── IReservationsClient.kt
│   └── useCases/
│       ├── CreateOrderUseCase.kt
│       ├── ProcessPaymentUseCase.kt
│       ├── GetOrderUseCase.kt
│       ├── ListCustomerOrdersUseCase.kt
│       ├── RefundOrderUseCase.kt
│       ├── GetIssuedTicketUseCase.kt
│       ├── ListOrderTicketsUseCase.kt
│       └── ValidateTicketUseCase.kt
├── adapters/
│   ├── inbound/
│   │   ├── OrderController.kt
│   │   └── TicketController.kt
│   └── outbound/
│       ├── OrderRepositoryAdapter.kt
│       ├── IssuedTicketRepositoryAdapter.kt
│       ├── MockPaymentGatewayAdapter.kt
│       └── ReservationsClientAdapter.kt
├── infrastructure/
│   ├── persistence/
│   └── web/
│       └── Main.kt
├── build.gradle.kts
└── Dockerfile
```

### Payment Gateway (Mock)

```kotlin
// application/ports/outbound/IPaymentGateway.kt
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

data class RefundResult(
    val success: Boolean,
    val refundId: String?,
    val errorMessage: String?
)

// adapters/outbound/MockPaymentGatewayAdapter.kt
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
```

### Geração de Código do Ingresso

```kotlin
// domain/TicketCodeGenerator.kt
object TicketCodeGenerator {
    fun generate(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Sem I, O, 0, 1 (confusos)
        val random = (1..8).map { chars.random() }.joinToString("")
        return "TKT-$random"
    }
}
```

### Use Cases

| Use Case | Descrição | Roles |
|----------|-----------|-------|
| CreateOrderUseCase | Cria order a partir de reservation | CUSTOMER |
| ProcessPaymentUseCase | Processa pagamento | CUSTOMER |
| GetOrderUseCase | Busca order | CUSTOMER (própria) / ADMIN |
| ListCustomerOrdersUseCase | Lista orders do cliente | CUSTOMER |
| RefundOrderUseCase | Reembolsa order | ADMIN |
| GetIssuedTicketUseCase | Busca ingresso por código | Público |
| ListOrderTicketsUseCase | Lista ingressos do order | CUSTOMER (dono) |
| ValidateTicketUseCase | Valida ingresso (check-in) | PARTNER / ADMIN |

### DTOs

```kotlin
// application/dto/CreateOrderRequest.kt
data class CreateOrderRequest(
    val reservationId: UUID
)

// application/dto/ProcessPaymentRequest.kt
data class ProcessPaymentRequest(
    val orderId: UUID,
    val paymentMethod: String  // "credit_card", "pix", "mock"
)

// application/dto/OrderResponse.kt
data class OrderResponse(
    val id: UUID,
    val customerId: UUID,
    val reservationId: UUID,
    val eventId: UUID,
    val items: List<OrderItemResponse>,
    val totalAmount: BigDecimal,
    val paymentStatus: PaymentStatus,
    val paymentMethod: String?,
    val createdAt: Instant,
    val paidAt: Instant?
)

// application/dto/IssuedTicketResponse.kt
data class IssuedTicketResponse(
    val id: UUID,
    val code: String,
    val qrCode: String,
    val ticketTypeName: String,
    val eventName: String,
    val status: TicketStatus,
    val issuedAt: Instant
)
```

### Endpoints

| Método | Endpoint | Descrição | Roles |
|--------|----------|-----------|-------|
| POST | `/orders` | Cria order | CUSTOMER |
| POST | `/orders/{id}/pay` | Processa pagamento | CUSTOMER |
| GET | `/orders/{id}` | Busca order | CUSTOMER/ADMIN |
| GET | `/orders/me` | Meus orders | CUSTOMER |
| POST | `/orders/{id}/refund` | Reembolsa | ADMIN |
| GET | `/orders/{id}/tickets` | Ingressos do order | CUSTOMER |
| GET | `/tickets/{code}` | Busca por código | Público |
| POST | `/tickets/{code}/validate` | Check-in | PARTNER/ADMIN |

### Fluxo de Compra

```
┌─────────────────────────────────────────────────────────────────┐
│                      FLUXO DE COMPRA                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. Customer tem Reservation ACTIVE                              │
│                                                                  │
│  2. POST /orders {reservationId}                                 │
│     └── Valida reservation pertence ao customer                  │
│     └── Valida reservation está ACTIVE                           │
│     └── Cria Order com status PENDING                            │
│                                                                  │
│  3. POST /orders/{id}/pay {paymentMethod}                        │
│     └── Order.status = PROCESSING                                │
│     └── Chama PaymentGateway.processPayment()                    │
│                                                                  │
│  4a. Se pagamento OK:                                            │
│     └── Order.status = PAID                                      │
│     └── Order.paidAt = now()                                     │
│     └── Chama Reservations.convert(reservationId)                │
│     └── Gera IssuedTickets (1 por quantidade)                    │
│     └── Retorna OrderResponse com tickets                        │
│                                                                  │
│  4b. Se pagamento FALHOU:                                        │
│     └── Order.status = FAILED                                    │
│     └── Reservation permanece ACTIVE                             │
│     └── Retorna erro                                             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Emissão de Ingressos

```
┌─────────────────────────────────────────────────────────────────┐
│                   EMISSÃO DE INGRESSOS                           │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Order com 2 items:                                              │
│  - TicketType "VIP" x 2                                          │
│  - TicketType "Pista" x 3                                        │
│                                                                  │
│  Gera 5 IssuedTickets:                                           │
│  - TKT-ABC12345 (VIP)                                            │
│  - TKT-DEF67890 (VIP)                                            │
│  - TKT-GHI11111 (Pista)                                          │
│  - TKT-JKL22222 (Pista)                                          │
│  - TKT-MNO33333 (Pista)                                          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Regras de Negócio

| Regra | Descrição |
|-------|-----------|
| **RN-O01** | Order só pode ser criada de Reservation ACTIVE |
| **RN-O02** | Customer só cria order de própria reservation |
| **RN-O03** | Pagamento só pode ser processado se status == PENDING |
| **RN-O04** | Cada ingresso tem código único |
| **RN-O05** | Ingresso só pode ser usado uma vez |
| **RN-O06** | Reembolso cancela todos os ingressos do order |
| **RN-O07** | Não pode reembolsar ingresso já usado |

## Alternativas Consideradas

### Alternativa 1: Pagamento Síncrono
- **Descrição**: Processar pagamento na criação do order
- **Prós**: Fluxo mais simples
- **Contras**: Timeout em pagamentos lentos
- **Motivo da rejeição**: Separar criação de pagamento é mais robusto

### Alternativa 2: Gateway Real desde o Início
- **Descrição**: Integrar com Stripe/PagSeguro imediatamente
- **Prós**: Mais realista
- **Contras**: Complexidade, custos, sandbox
- **Motivo da rejeição**: Mock permite focar na lógica de negócio

## Consequências

### Positivas
- Fluxo de pagamento isolado
- Mock permite testes completos
- Ingressos com código único para validação

### Negativas
- Mock não testa cenários reais de gateway
- Precisa trocar adapter para produção

### Riscos

| Risco | Mitigação |
|-------|-----------|
| Pagamento OK mas falha ao emitir tickets | Transação + retry |
| Código de ingresso duplicado | UUID + constraint unique |
| Reembolso parcial | Não suportado inicialmente |

## Implementação

1. Criar estrutura de pastas
2. Implementar entidades de domínio
3. Implementar MockPaymentGatewayAdapter
4. Implementar repositories
5. Implementar use cases
6. Implementar controllers
7. Configurar Main.kt e Dockerfile
8. Testes (mock gateway)
9. Integrar com BFF

## Referências

- [Intent: Sistema de Ingressos](../intent/ticket-system.md)
- [ADR-007: Reservations Service](007-reservations-service.md)
