# ADR-006: Tickets Service

## Status

Aceito

## Data

2024-12-09

## Contexto

O sistema de ingressos ([Intent](../intent/ticket-system.md)) requer gestão de tipos de ingressos e controle de estoque.

### Responsabilidades

- CRUD de tipos de ingresso por evento
- Controle de quantidade disponível
- Decremento/incremento de estoque (reservas)

## Decisão

Criar microserviço `tickets` na porta **8084**.

### Modelo de Domínio

```kotlin
// domain/TicketType.kt
data class TicketType(
    val id: UUID,
    val eventId: UUID,              // Referência ao Event
    val name: String,               // "VIP", "Pista", "Camarote"
    val description: String,
    val price: BigDecimal,
    val totalQuantity: Int,         // Quantidade total
    val availableQuantity: Int,     // Quantidade disponível
    val maxPerCustomer: Int,        // Máximo por cliente (ex: 4)
    val salesStartDate: Instant?,   // Início das vendas
    val salesEndDate: Instant?,     // Fim das vendas
    val status: TicketTypeStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)

// domain/TicketTypeStatus.kt
enum class TicketTypeStatus {
    ACTIVE,     // Disponível para venda
    PAUSED,     // Pausado temporariamente
    SOLD_OUT,   // Esgotado
    INACTIVE    // Desativado
}
```

### Estrutura do Serviço

```
services/tickets/
├── domain/
│   ├── TicketType.kt
│   └── TicketTypeStatus.kt
├── application/
│   ├── dto/
│   │   ├── CreateTicketTypeRequest.kt
│   │   ├── UpdateTicketTypeRequest.kt
│   │   ├── TicketTypeResponse.kt
│   │   ├── ReserveTicketsRequest.kt
│   │   ├── ReserveTicketsResponse.kt
│   │   └── ReleaseTicketsRequest.kt
│   ├── ports/outbound/
│   │   └── ITicketTypeRepository.kt
│   └── useCases/
│       ├── CreateTicketTypeUseCase.kt
│       ├── UpdateTicketTypeUseCase.kt
│       ├── DeactivateTicketTypeUseCase.kt
│       ├── GetTicketTypeUseCase.kt
│       ├── ListTicketTypesByEventUseCase.kt
│       ├── ReserveTicketsUseCase.kt
│       └── ReleaseTicketsUseCase.kt
├── adapters/
│   ├── inbound/
│   │   └── TicketTypeController.kt
│   └── outbound/
│       └── TicketTypeRepositoryAdapter.kt
├── infrastructure/
│   ├── persistence/
│   └── web/
│       └── Main.kt
├── build.gradle.kts
└── Dockerfile
```

### Use Cases

| Use Case | Descrição | Chamado por |
|----------|-----------|-------------|
| CreateTicketTypeUseCase | Cria tipo de ingresso | PARTNER (dono do evento) |
| UpdateTicketTypeUseCase | Atualiza tipo | PARTNER (dono) |
| DeactivateTicketTypeUseCase | Desativa tipo | PARTNER / ADMIN |
| GetTicketTypeUseCase | Busca por ID | Público |
| ListTicketTypesByEventUseCase | Lista por evento | Público |
| **ReserveTicketsUseCase** | Decrementa estoque | Reservations Service |
| **ReleaseTicketsUseCase** | Incrementa estoque | Reservations Service |

### DTOs

```kotlin
// application/dto/CreateTicketTypeRequest.kt
data class CreateTicketTypeRequest(
    val eventId: UUID,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val totalQuantity: Int,
    val maxPerCustomer: Int,
    val salesStartDate: Instant?,
    val salesEndDate: Instant?
)

// application/dto/TicketTypeResponse.kt
data class TicketTypeResponse(
    val id: UUID,
    val eventId: UUID,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val totalQuantity: Int,
    val availableQuantity: Int,
    val maxPerCustomer: Int,
    val salesStartDate: Instant?,
    val salesEndDate: Instant?,
    val status: TicketTypeStatus,
    val createdAt: Instant
)

// application/dto/ReserveTicketsRequest.kt
data class ReserveTicketsRequest(
    val ticketTypeId: UUID,
    val quantity: Int
)

// application/dto/ReserveTicketsResponse.kt
data class ReserveTicketsResponse(
    val success: Boolean,
    val ticketTypeId: UUID,
    val reservedQuantity: Int,
    val unitPrice: BigDecimal,      // Preço no momento da reserva
    val remainingQuantity: Int
)

// application/dto/ReleaseTicketsRequest.kt
data class ReleaseTicketsRequest(
    val ticketTypeId: UUID,
    val quantity: Int
)
```

### Endpoints

| Método | Endpoint | Descrição | Roles |
|--------|----------|-----------|-------|
| POST | `/ticket-types` | Cria tipo | PARTNER |
| GET | `/ticket-types/{id}` | Busca por ID | Público |
| PUT | `/ticket-types/{id}` | Atualiza | PARTNER (dono) |
| DELETE | `/ticket-types/{id}` | Desativa | PARTNER / ADMIN |
| GET | `/ticket-types/event/{eventId}` | Lista por evento | Público |
| POST | `/ticket-types/reserve` | Reserva (decrementa) | Internal |
| POST | `/ticket-types/release` | Libera (incrementa) | Internal |

> Endpoints `reserve` e `release` são internos (chamados pelo Reservations service via BFF)

### Controle de Estoque

```
┌─────────────────────────────────────────────────────────────┐
│                    FLUXO DE ESTOQUE                          │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  totalQuantity = 100 (fixo)                                  │
│  availableQuantity = 100 (variável)                          │
│                                                              │
│  [Reserva de 3 ingressos]                                    │
│  availableQuantity = 100 - 3 = 97                            │
│                                                              │
│  [Cancelamento da reserva]                                   │
│  availableQuantity = 97 + 3 = 100                            │
│                                                              │
│  [Compra confirmada]                                         │
│  availableQuantity permanece (já foi decrementado na reserva)│
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Regras de Negócio

| Regra | Descrição |
|-------|-----------|
| **RN-T01** | Apenas Partner dono do evento pode criar TicketType |
| **RN-T02** | price deve ser >= 0 (pode ser gratuito) |
| **RN-T03** | totalQuantity deve ser > 0 |
| **RN-T04** | availableQuantity não pode ser negativo |
| **RN-T05** | maxPerCustomer deve ser >= 1 |
| **RN-T06** | Reserva falha se availableQuantity < quantidade solicitada |
| **RN-T07** | Status muda para SOLD_OUT quando availableQuantity = 0 |
| **RN-T08** | Não pode reservar se status != ACTIVE |

### Concorrência

Para evitar race conditions no estoque:

```kotlin
// Usar operação atômica no repository
interface ITicketTypeRepository {
    // Decrementa atomicamente, retorna false se não houver estoque
    suspend fun decrementAvailableQuantity(id: UUID, quantity: Int): Boolean
    
    // Incrementa atomicamente
    suspend fun incrementAvailableQuantity(id: UUID, quantity: Int): Boolean
}
```

## Alternativas Consideradas

### Alternativa 1: Tickets dentro de Events
- **Descrição**: TicketTypes como parte do Events service
- **Prós**: Menos serviços, dados co-localizados
- **Contras**: Events service fica complexo, estoque é crítico
- **Motivo da rejeição**: Estoque merece isolamento para escalar

## Consequências

### Positivas
- Controle de estoque isolado e escalável
- Operações atômicas de reserva/liberação
- Fácil de monitorar disponibilidade

### Negativas
- Validação de ownership requer chamada ao Events
- Latência adicional nas reservas

### Riscos

| Risco | Mitigação |
|-------|-----------|
| Race condition no estoque | Operações atômicas no banco |
| Estoque negativo | Constraint no banco + validação |
| Inconsistência reserva/estoque | Transação compensatória |

## Implementação

1. Criar estrutura de pastas
2. Implementar entidades de domínio
3. Implementar repository com operações atômicas
4. Implementar use cases
5. Implementar controller
6. Configurar Main.kt e Dockerfile
7. Testes (especialmente concorrência)
8. Integrar com BFF

## Referências

- [Intent: Sistema de Ingressos](../intent/ticket-system.md)
- [ADR-005: Events Service](005-events-service.md)
- [ADR-007: Reservations Service](007-reservations-service.md)
