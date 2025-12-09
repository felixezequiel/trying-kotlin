# ADR-003: Sistema de Roles no Users Service

## Status

Aceito

## Data

2024-12-09

## Contexto

O sistema de ingressos ([Intent](../intent/ticket-system.md)) requer que usuários possam assumir diferentes papéis:

- **Customer**: Compra ingressos
- **Partner**: Cria eventos e vende ingressos
- **Admin**: Gestão geral

Um mesmo usuário pode acumular múltiplos roles (ex: ser Customer E Partner).

### Situação Atual

O serviço `users` existe mas não possui sistema de roles.

## Decisão

Implementaremos um sistema de roles baseado em `Set<Role>` na entidade User.

### Modelo de Domínio

```kotlin
// domain/Role.kt
enum class Role {
    CUSTOMER,   // Pode comprar ingressos (atribuído no registro)
    PARTNER,    // Pode criar eventos (requer aprovação)
    ADMIN       // Gestão geral (atribuído manualmente)
}

// domain/User.kt (atualizado)
data class User(
    val id: UUID,
    val name: String,
    val email: String,
    val passwordHash: String,
    val roles: Set<Role>,       // NOVO: múltiplos roles
    val createdAt: Instant,
    val updatedAt: Instant
)
```

### Regras de Atribuição de Roles

| Role | Atribuição |
|------|------------|
| CUSTOMER | Automático no registro |
| PARTNER | Via solicitação + aprovação Admin |
| ADMIN | Apenas via banco/seed |

### Use Cases Necessários

```kotlin
// application/useCases/
class AddRoleToUserUseCase      // Admin adiciona role a usuário
class RemoveRoleFromUserUseCase // Admin remove role de usuário
class RequestPartnerRoleUseCase // User solicita ser Partner
```

### DTOs

```kotlin
// application/dto/
data class AddRoleRequest(
    val userId: UUID,
    val role: Role
)

data class UserResponse(
    val id: UUID,
    val name: String,
    val email: String,
    val roles: Set<Role>,
    val createdAt: Instant
)
```

### Endpoints

| Método | Endpoint | Descrição | Roles Permitidos |
|--------|----------|-----------|------------------|
| POST | `/users/{id}/roles` | Adiciona role | ADMIN |
| DELETE | `/users/{id}/roles/{role}` | Remove role | ADMIN |
| POST | `/users/request-partner` | Solicita ser Partner | CUSTOMER |
| GET | `/users/{id}` | Retorna user com roles | Autenticado |

## Alternativas Consideradas

### Alternativa 1: Tabela Separada de Roles
- **Descrição**: Criar tabela `user_roles` com relacionamento N:N
- **Prós**: Mais flexível para adicionar metadados aos roles
- **Contras**: Complexidade adicional, joins
- **Motivo da rejeição**: Set<Role> é suficiente para o caso atual

### Alternativa 2: Roles como Serviço Separado
- **Descrição**: Criar microserviço `roles` ou `permissions`
- **Prós**: Isolamento total, reutilizável
- **Contras**: Overhead de comunicação, complexidade
- **Motivo da rejeição**: Roles são intrínsecos ao User, não justifica serviço separado

## Consequências

### Positivas
- Flexibilidade de múltiplos roles por usuário
- Simples de implementar e testar
- Fácil de estender com novos roles

### Negativas
- Roles são simples (sem permissões granulares)
- Não suporta roles temporários ou com expiração

### Riscos

| Risco | Mitigação |
|-------|-----------|
| Usuário sem role CUSTOMER | Garantir atribuição automática no registro |
| Admin remove próprio role ADMIN | Validar que sempre existe pelo menos 1 ADMIN |

## Implementação

1. Criar enum `Role` em `domain/`
2. Atualizar entidade `User` com `roles: Set<Role>`
3. Atualizar `IUserRepository` para persistir roles
4. Criar use cases de gestão de roles
5. Atualizar DTOs
6. Atualizar controller com novos endpoints
7. Atualizar testes

## Referências

- [Intent: Sistema de Ingressos](../intent/ticket-system.md)
- [ADR-001: Arquitetura BFF com Microserviços](001-bff-microservices-architecture.md)
