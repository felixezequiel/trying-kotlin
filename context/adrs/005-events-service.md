# ADR-005: Events Service

## Status

Aceito

## Data

2024-12-09

## Contexto

O sistema de ingressos ([Intent](../intent/ticket-system.md)) requer gestão de eventos criados por parceiros.

### Responsabilidades

- CRUD de eventos
- Controle de estados (DRAFT → PUBLISHED → FINISHED)
- Vinculação com Partner

## Decisão

Criar microserviço `events` na porta **8083**.

### Modelo de Domínio

```kotlin
// domain/Event.kt
data class Event(
    val id: UUID,
    val partnerId: UUID,            // Referência ao Partner
    val name: String,
    val description: String,
    val venue: Venue,
    val startDate: Instant,
    val endDate: Instant,
    val status: EventStatus,
    val imageUrl: String?,
    val createdAt: Instant,
    val publishedAt: Instant?,
    val updatedAt: Instant
)

// domain/Venue.kt
data class Venue(
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val capacity: Int?
)

// domain/EventStatus.kt
enum class EventStatus {
    DRAFT,      // Rascunho, não visível
    PUBLISHED,  // Publicado, vendendo ingressos
    CANCELLED,  // Cancelado
    FINISHED    // Encerrado
}
```

### Estrutura do Serviço

```
services/events/
├── domain/
│   ├── Event.kt
│   ├── EventStatus.kt
│   └── Venue.kt
├── application/
│   ├── dto/
│   │   ├── CreateEventRequest.kt
│   │   ├── UpdateEventRequest.kt
│   │   ├── EventResponse.kt
│   │   └── EventListResponse.kt
│   ├── ports/outbound/
│   │   └── IEventRepository.kt
│   └── useCases/
│       ├── CreateEventUseCase.kt
│       ├── UpdateEventUseCase.kt
│       ├── PublishEventUseCase.kt
│       ├── CancelEventUseCase.kt
│       ├── FinishEventUseCase.kt
│       ├── GetEventUseCase.kt
│       └── ListEventsUseCase.kt
├── adapters/
│   ├── inbound/
│   │   └── EventController.kt
│   └── outbound/
│       └── EventRepositoryAdapter.kt
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
| CreateEventUseCase | Cria evento em DRAFT | PARTNER (aprovado) |
| UpdateEventUseCase | Atualiza evento | PARTNER (dono) |
| PublishEventUseCase | Publica evento | PARTNER (dono) |
| CancelEventUseCase | Cancela evento | PARTNER (dono) / ADMIN |
| FinishEventUseCase | Finaliza evento | PARTNER (dono) / ADMIN |
| GetEventUseCase | Busca evento | Público (se PUBLISHED) |
| ListEventsUseCase | Lista eventos | Público / PARTNER / ADMIN |

### DTOs

```kotlin
// application/dto/CreateEventRequest.kt
data class CreateEventRequest(
    val name: String,
    val description: String,
    val venue: VenueRequest,
    val startDate: Instant,
    val endDate: Instant,
    val imageUrl: String?
)

data class VenueRequest(
    val name: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val capacity: Int?
)

// application/dto/EventResponse.kt
data class EventResponse(
    val id: UUID,
    val partnerId: UUID,
    val name: String,
    val description: String,
    val venue: VenueResponse,
    val startDate: Instant,
    val endDate: Instant,
    val status: EventStatus,
    val imageUrl: String?,
    val createdAt: Instant,
    val publishedAt: Instant?
)
```

### Endpoints

| Método | Endpoint | Descrição | Roles |
|--------|----------|-----------|-------|
| POST | `/events` | Cria evento | PARTNER |
| GET | `/events/{id}` | Busca por ID | Público* |
| PUT | `/events/{id}` | Atualiza | PARTNER (dono) |
| POST | `/events/{id}/publish` | Publica | PARTNER (dono) |
| POST | `/events/{id}/cancel` | Cancela | PARTNER / ADMIN |
| POST | `/events/{id}/finish` | Finaliza | PARTNER / ADMIN |
| GET | `/events` | Lista públicos | Público |
| GET | `/events/partner/{partnerId}` | Lista do partner | PARTNER (dono) |

> *Eventos DRAFT só visíveis pelo dono

### Fluxo de Estados

```
┌─────────────┐
│    DRAFT    │──────────────────┐
└──────┬──────┘                  │
       │ publish                 │ cancel
       ▼                         ▼
┌─────────────┐           ┌─────────────┐
│  PUBLISHED  │──────────▶│  CANCELLED  │
└──────┬──────┘  cancel   └─────────────┘
       │
       │ finish (após endDate)
       ▼
┌─────────────┐
│  FINISHED   │
└─────────────┘
```

### Regras de Negócio

| Regra | Descrição |
|-------|-----------|
| **RN-E01** | Apenas Partner APPROVED pode criar eventos |
| **RN-E02** | Evento inicia em DRAFT |
| **RN-E03** | Só pode publicar se tiver pelo menos 1 TicketType |
| **RN-E04** | startDate deve ser futura |
| **RN-E05** | endDate deve ser após startDate |
| **RN-E06** | Não pode editar evento CANCELLED ou FINISHED |
| **RN-E07** | Cancelar evento cancela todas as reservas |

## Alternativas Consideradas

### Alternativa 1: Events dentro de Partners
- **Descrição**: Events como sub-recurso de Partners
- **Prós**: Menos serviços
- **Contras**: Acoplamento, Partner service fica grande
- **Motivo da rejeição**: Events é bounded context próprio

## Consequências

### Positivas
- Domínio isolado e coeso
- Fácil de escalar independentemente
- Queries de eventos públicos não afetam Partners

### Negativas
- Validação de Partner requer chamada externa
- RN-E03 requer chamada ao Tickets service

### Riscos

| Risco | Mitigação |
|-------|-----------|
| Publicar sem tickets | Validar via BFF antes de publicar |
| Evento órfão (Partner deletado) | Soft delete ou manter histórico |

## Implementação

1. Criar estrutura de pastas
2. Implementar entidades de domínio
3. Implementar repository
4. Implementar use cases
5. Implementar controller
6. Configurar Main.kt e Dockerfile
7. Testes
8. Integrar com BFF

## Referências

- [Intent: Sistema de Ingressos](../intent/ticket-system.md)
- [ADR-004: Partners Service](004-partners-service.md)
- [ADR-006: Tickets Service](006-tickets-service.md)
