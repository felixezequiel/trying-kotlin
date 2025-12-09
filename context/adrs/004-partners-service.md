# ADR-004: Partners Service

## Status

Aceito

## Data

2024-12-09

## Contexto

O sistema de ingressos ([Intent](../intent/ticket-system.md)) requer gestão de parceiros comerciais que criam e vendem ingressos para eventos.

### Responsabilidades

- Cadastro de perfil comercial (empresa, documentos)
- Fluxo de aprovação por Admin
- Vinculação com User (que possui role PARTNER)

## Decisão

Criar microserviço `partners` na porta **8082**.

### Modelo de Domínio

```kotlin
// domain/Partner.kt
data class Partner(
    val id: UUID,
    val userId: UUID,               // Referência ao User
    val companyName: String,
    val tradeName: String?,         // Nome fantasia
    val document: String,           // CNPJ ou CPF
    val documentType: DocumentType, // CNPJ, CPF
    val email: String,              // Email comercial
    val phone: String,
    val status: PartnerStatus,
    val rejectionReason: String?,   // Motivo se rejeitado
    val createdAt: Instant,
    val approvedAt: Instant?,
    val updatedAt: Instant
)

enum class PartnerStatus {
    PENDING,    // Aguardando aprovação
    APPROVED,   // Aprovado, pode criar eventos
    REJECTED,   // Rejeitado
    SUSPENDED   // Suspenso temporariamente
}

enum class DocumentType {
    CPF,
    CNPJ
}
```

### Estrutura do Serviço

```
services/partners/
├── domain/
│   ├── Partner.kt
│   ├── PartnerStatus.kt
│   └── DocumentType.kt
├── application/
│   ├── dto/
│   │   ├── CreatePartnerRequest.kt
│   │   ├── UpdatePartnerRequest.kt
│   │   ├── ApprovePartnerRequest.kt
│   │   ├── RejectPartnerRequest.kt
│   │   └── PartnerResponse.kt
│   ├── ports/outbound/
│   │   └── IPartnerRepository.kt
│   └── useCases/
│       ├── CreatePartnerUseCase.kt
│       ├── UpdatePartnerUseCase.kt
│       ├── ApprovePartnerUseCase.kt
│       ├── RejectPartnerUseCase.kt
│       ├── SuspendPartnerUseCase.kt
│       ├── GetPartnerUseCase.kt
│       └── ListPartnersUseCase.kt
├── adapters/
│   ├── inbound/
│   │   └── PartnerController.kt
│   └── outbound/
│       └── PartnerRepositoryAdapter.kt
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
| CreatePartnerUseCase | Cria perfil de parceiro | PARTNER |
| UpdatePartnerUseCase | Atualiza dados do parceiro | PARTNER (próprio) |
| ApprovePartnerUseCase | Aprova parceiro | ADMIN |
| RejectPartnerUseCase | Rejeita parceiro | ADMIN |
| SuspendPartnerUseCase | Suspende parceiro | ADMIN |
| GetPartnerUseCase | Busca parceiro por ID | Autenticado |
| ListPartnersUseCase | Lista parceiros | ADMIN |

### DTOs

```kotlin
// application/dto/CreatePartnerRequest.kt
data class CreatePartnerRequest(
    val companyName: String,
    val tradeName: String?,
    val document: String,
    val documentType: DocumentType,
    val email: String,
    val phone: String
)

// application/dto/PartnerResponse.kt
data class PartnerResponse(
    val id: UUID,
    val userId: UUID,
    val companyName: String,
    val tradeName: String?,
    val document: String,
    val documentType: DocumentType,
    val email: String,
    val phone: String,
    val status: PartnerStatus,
    val createdAt: Instant,
    val approvedAt: Instant?
)

// application/dto/RejectPartnerRequest.kt
data class RejectPartnerRequest(
    val reason: String
)
```

### Endpoints

| Método | Endpoint | Descrição | Roles |
|--------|----------|-----------|-------|
| POST | `/partners` | Cria parceiro | PARTNER |
| GET | `/partners/{id}` | Busca por ID | Autenticado |
| GET | `/partners/me` | Parceiro do usuário logado | PARTNER |
| PUT | `/partners/{id}` | Atualiza parceiro | PARTNER (próprio) |
| POST | `/partners/{id}/approve` | Aprova | ADMIN |
| POST | `/partners/{id}/reject` | Rejeita | ADMIN |
| POST | `/partners/{id}/suspend` | Suspende | ADMIN |
| GET | `/partners` | Lista (com filtros) | ADMIN |

### Fluxo de Aprovação

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   PENDING   │────▶│  APPROVED   │────▶│  SUSPENDED  │
└─────────────┘     └─────────────┘     └─────────────┘
       │                                       │
       │                                       │
       ▼                                       ▼
┌─────────────┐                         ┌─────────────┐
│  REJECTED   │                         │  APPROVED   │
└─────────────┘                         └─────────────┘
```

### Regras de Negócio

| Regra | Descrição |
|-------|-----------|
| **RN-P01** | User deve ter role PARTNER para criar Partner |
| **RN-P02** | Um User só pode ter um Partner |
| **RN-P03** | Documento (CPF/CNPJ) deve ser único |
| **RN-P04** | Apenas APPROVED pode criar eventos |
| **RN-P05** | Rejeição requer motivo |

## Alternativas Consideradas

### Alternativa 1: Partner como campo do User
- **Descrição**: Adicionar campos de Partner diretamente no User
- **Prós**: Simplicidade, menos serviços
- **Contras**: Viola SRP, acopla domínios
- **Motivo da rejeição**: Partner é um bounded context separado

## Consequências

### Positivas
- Isolamento do domínio de parceiros
- Fluxo de aprovação claro
- Fácil de auditar mudanças de status

### Negativas
- Chamada extra ao Users para validar role
- Necessidade de manter consistência userId ↔ partnerId

### Riscos

| Risco | Mitigação |
|-------|-----------|
| User sem role PARTNER cria Partner | Validar role no use case |
| Partner órfão (User deletado) | Soft delete ou cascade |

## Implementação

1. Criar estrutura de pastas do serviço
2. Implementar entidades de domínio
3. Implementar repository (in-memory para testes)
4. Implementar use cases
5. Implementar controller
6. Configurar Main.kt e Dockerfile
7. Testes unitários e integração
8. Integrar com BFF

## Referências

- [Intent: Sistema de Ingressos](../intent/ticket-system.md)
- [ADR-003: Sistema de Roles](003-users-roles-system.md)
