# ADR-007: Reservations Service

## Status

Aceito

## Data

2024-12-09

## Contexto

O sistema de ingressos ([Intent](../intent/ticket-system.md)) requer sistema de reservas que bloqueia ingressos até cancelamento manual.

### Responsabilidades

- Criar reservas (bloqueia ingressos)
- Cancelar reservas (libera ingressos)
- Converter reserva em pedido
- **Reservas NÃO expiram automaticamente**

## Decisão

Criar microserviço `reservations` na porta **8085**.

### Modelo de Domínio

```kotlin
// domain/Reservation.kt
data class Reservation(
    val id: UUID,
    val customerId: UUID,           // User com role CUSTOMER
    val eventId: UUID,              // Referência ao Event (desnormalizado)
    val items: List<ReservationItem>,
    val totalAmount: BigDecimal,    // Soma dos itens
    val status: ReservationStatus,
    val createdAt: Instant,
    val cancelledAt: Instant?,
    val cancelledBy: UUID?,         // User que cancelou
    val cancellationReason: String?,
    val convertedAt: Instant?,      // Quando virou Order
    val orderId: UUID?              // Referência ao Order criado
)

// domain/ReservationItem.kt
data class ReservationItem(
    val id: UUID,
    val ticketTypeId: UUID,
    val ticketTypeName: String,     // Desnormalizado para histórico
    val quantity: Int,
    val unitPrice: BigDecimal,      // Preço no momento da reserva
    val subtotal: BigDecimal        // quantity * unitPrice
)

// domain/ReservationStatus.kt
enum class ReservationStatus {
    ACTIVE,     // Reserva ativa, ingressos bloqueados
    CANCELLED,  // Cancelada, ingressos liberados
    CONVERTED   // Convertida em Order
}

// domain/CancellationType.kt
enum class CancellationType {
    BY_CUSTOMER,    // Cliente cancelou
    BY_PARTNER,     // Partner cancelou
    BY_ADMIN,       // Admin cancelou
    EVENT_CANCELLED // Evento foi cancelado
}
```

### Estrutura do Serviço

```
services/reservations/
├── domain/
│   ├── Reservation.kt
│   ├── ReservationItem.kt
│   ├── ReservationStatus.kt
│   └── CancellationType.kt
├── application/
│   ├── dto/
│   │   ├── CreateReservationRequest.kt
│   │   ├── CreateReservationItemRequest.kt
│   │   ├── ReservationResponse.kt
│   │   ├── CancelReservationRequest.kt
│   │   └── ConvertReservationRequest.kt
│   ├── ports/outbound/
│   │   ├── IReservationRepository.kt
│   │   └── ITicketsClient.kt
│   └── useCases/
│       ├── CreateReservationUseCase.kt
│       ├── CancelReservationUseCase.kt
│       ├── ConvertReservationUseCase.kt
│       ├── GetReservationUseCase.kt
│       ├── ListCustomerReservationsUseCase.kt
│       └── ListEventReservationsUseCase.kt
├── adapters/
│   ├── inbound/
│   │   └── ReservationController.kt
│   └── outbound/
│       ├── ReservationRepositoryAdapter.kt
│       └── TicketsClientAdapter.kt
├── infrastructure/
│   ├── persistence/
│   └── web/
│       └── Main.kt
├── build.gradle.kts
└── Dockerfile
```

### Use Cases

| Use Case | Descrição | Roles |
|----------|-----------|-------|
| CreateReservationUseCase | Cria reserva e bloqueia ingressos | CUSTOMER |
| CancelReservationUseCase | Cancela e libera ingressos | CUSTOMER/PARTNER/ADMIN |
| ConvertReservationUseCase | Marca como convertida | Orders Service |
| GetReservationUseCase | Busca por ID | Dono / PARTNER / ADMIN |
| ListCustomerReservationsUseCase | Lista do cliente | CUSTOMER (próprias) |
| ListEventReservationsUseCase | Lista do evento | PARTNER (dono) / ADMIN |

### DTOs

```kotlin
// application/dto/CreateReservationRequest.kt
data class CreateReservationRequest(
    val eventId: UUID,
    val items: List<CreateReservationItemRequest>
)

data class CreateReservationItemRequest(
    val ticketTypeId: UUID,
    val quantity: Int
)

// application/dto/ReservationResponse.kt
data class ReservationResponse(
    val id: UUID,
    val customerId: UUID,
    val eventId: UUID,
    val items: List<ReservationItemResponse>,
    val totalAmount: BigDecimal,
    val status: ReservationStatus,
    val createdAt: Instant,
    val cancelledAt: Instant?,
    val cancelledBy: UUID?,
    val cancellationReason: String?
)

data class ReservationItemResponse(
    val ticketTypeId: UUID,
    val ticketTypeName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val subtotal: BigDecimal
)

// application/dto/CancelReservationRequest.kt
data class CancelReservationRequest(
    val reason: String?
)
```

### Endpoints

| Método | Endpoint | Descrição | Roles |
|--------|----------|-----------|-------|
| POST | `/reservations` | Cria reserva | CUSTOMER |
| GET | `/reservations/{id}` | Busca por ID | Dono/PARTNER/ADMIN |
| POST | `/reservations/{id}/cancel` | Cancela | CUSTOMER/PARTNER/ADMIN |
| POST | `/reservations/{id}/convert` | Converte (interno) | Internal |
| GET | `/reservations/me` | Minhas reservas | CUSTOMER |
| GET | `/reservations/event/{eventId}` | Reservas do evento | PARTNER/ADMIN |

### Fluxo de Criação de Reserva

```
┌─────────────────────────────────────────────────────────────────┐
│                   CRIAR RESERVA                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. Customer envia CreateReservationRequest                      │
│     └── items: [{ticketTypeId, quantity}, ...]                   │
│                                                                  │
│  2. Para cada item:                                              │
│     └── Chama Tickets.reserve(ticketTypeId, quantity)            │
│     └── Se falhar: rollback dos anteriores                       │
│                                                                  │
│  3. Se todos OK:                                                 │
│     └── Cria Reservation com status ACTIVE                       │
│     └── Retorna ReservationResponse                              │
│                                                                  │
│  4. Se algum falhar:                                             │
│     └── Chama Tickets.release() para os já reservados            │
│     └── Retorna erro                                             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Fluxo de Cancelamento

```
┌─────────────────────────────────────────────────────────────────┐
│                   CANCELAR RESERVA                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. Valida permissão:                                            │
│     └── CUSTOMER: só própria reserva                             │
│     └── PARTNER: reservas do seu evento                          │
│     └── ADMIN: qualquer reserva                                  │
│                                                                  │
│  2. Valida status == ACTIVE                                      │
│                                                                  │
│  3. Para cada item:                                              │
│     └── Chama Tickets.release(ticketTypeId, quantity)            │
│                                                                  │
│  4. Atualiza Reservation:                                        │
│     └── status = CANCELLED                                       │
│     └── cancelledAt = now()                                      │
│     └── cancelledBy = userId                                     │
│     └── cancellationReason = reason                              │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Regras de Negócio

| Regra | Descrição |
|-------|-----------|
| **RN-R01** | Apenas CUSTOMER pode criar reserva |
| **RN-R02** | Reserva deve ter pelo menos 1 item |
| **RN-R03** | Quantidade por item deve respeitar maxPerCustomer do TicketType |
| **RN-R04** | Todos os TicketTypes devem ser do mesmo Event |
| **RN-R05** | Event deve estar PUBLISHED |
| **RN-R06** | Reserva NÃO expira automaticamente |
| **RN-R07** | Só pode cancelar reserva ACTIVE |
| **RN-R08** | Customer só cancela própria reserva |
| **RN-R09** | Partner só cancela reservas do seu evento |
| **RN-R10** | Cancelamento libera ingressos imediatamente |

### Permissões de Cancelamento

| Quem | Pode cancelar |
|------|---------------|
| CUSTOMER | Próprias reservas |
| PARTNER | Reservas de seus eventos |
| ADMIN | Qualquer reserva |

## Alternativas Consideradas

### Alternativa 1: Reserva com Expiração
- **Descrição**: Reservas expiram após X minutos
- **Prós**: Libera ingressos automaticamente
- **Contras**: Requer scheduler, complexidade
- **Motivo da rejeição**: Requisito de negócio define cancelamento manual

### Alternativa 2: Reserva dentro de Orders
- **Descrição**: Unificar Reservations e Orders
- **Prós**: Menos serviços
- **Contras**: Responsabilidades diferentes, ciclos de vida distintos
- **Motivo da rejeição**: Reserva e Order são bounded contexts separados

## Consequências

### Positivas
- Controle explícito de reservas
- Auditoria de quem cancelou
- Flexibilidade de cancelamento

### Negativas
- Reservas podem ficar "esquecidas" indefinidamente
- Requer dashboard admin para gestão

### Riscos

| Risco | Mitigação |
|-------|-----------|
| Reservas órfãs bloqueando estoque | Dashboard admin + relatórios |
| Falha ao liberar estoque | Retry + alerta |
| Inconsistência reserva/estoque | Saga pattern |

## Implementação

1. Criar estrutura de pastas
2. Implementar entidades de domínio
3. Implementar ITicketsClient (port para chamar Tickets)
4. Implementar repository
5. Implementar use cases com rollback
6. Implementar controller
7. Configurar Main.kt e Dockerfile
8. Testes (especialmente cenários de falha)
9. Integrar com BFF

## Referências

- [Intent: Sistema de Ingressos](../intent/ticket-system.md)
- [ADR-006: Tickets Service](006-tickets-service.md)
- [ADR-008: Orders Service](008-orders-service.md)
