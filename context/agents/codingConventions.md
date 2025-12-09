# Convenções de Código Kotlin

Este documento define os padrões de código e convenções que DEVEM ser seguidos no projeto.

## Nomenclatura

| Tipo | Padrão | Exemplo |
|------|--------|---------|
| **Classes** | PascalCase | `UserUseCase`, `UserRepositoryAdapter` |
| **Interfaces** | `I` + PascalCase | `IUserRepository`, `IUnitOfWork` |
| **Funções** | camelCase | `registerUser`, `getUserByEmail` |
| **Packages** | minúsculas | `users.application.useCases` |
| **Arquivos** | Mesmo nome da classe principal | `UserUseCase.kt` |

## Convenções Kotlin

- Use `data class` para entidades de domínio
- Prefira `suspend fun` para operações assíncronas
- Use injeção de dependência via construtor
- Interfaces devem estar em `application/ports/`
- Implementações devem estar em `adapters/`

## Exemplo de Estrutura de Classe

```kotlin
package users.application.useCases

import users.domain.User
import users.application.ports.out.IUnitOfWork

class UserUseCase(private val unitOfWork: IUnitOfWork) {
    suspend fun registerUser(name: String, email: String) {
        // Implementação
    }
}
```

## DTOs e Bounded Contexts

> **ADR Relacionada**: [ADR-002: Isolamento de DTOs por Bounded Context](../adrs/002-dto-isolation-per-bounded-context.md)

### Regra: DTOs devem ser isolados por Bounded Context

Cada módulo (BFF, serviços) deve ter seus **próprios DTOs**, mesmo que pareçam similares.

| Local | Conteúdo | Exemplo |
|-------|----------|---------|
| `services/{service}/application/dto/` | DTOs do serviço | `UserResponse`, `RegisterUserRequest` |
| `bff/clients/` | DTOs do BFF (ACL) | `UserResponse`, `RegisterUserRequest` |
| `shared/dto/` | **Apenas** infra genérica | `PaginatedResponse<T>`, `ServiceResponse<T>` |

### ❌ NÃO fazer

```kotlin
// shared/dto/UserResponse.kt - ERRADO!
// DTOs de domínio específico não devem estar em shared
data class UserResponse(val id: String, val name: String)
```

### ✅ Fazer

```kotlin
// bff/clients/UsersClient.kt - CORRETO
// Cada contexto tem seu próprio DTO
@Serializable
data class UserResponse(val id: String, val name: String, val email: String)

// services/users/application/dto/UserResponse.kt - CORRETO
// Pode ter estrutura diferente conforme necessidade do contexto
@Serializable
data class UserResponse(val id: Long, val name: String, val email: String)
```

### Justificativa

- **Anti-Corruption Layer**: Protege cada contexto de mudanças externas
- **Evolução independente**: Serviços podem evoluir sem afetar outros
- **DDD**: Respeita o isolamento de Bounded Contexts

---

## Anti-Patterns a Evitar

1. **Dependências circulares**: Domain nunca deve importar de Application ou Infrastructure
2. **Lógica de negócio em Adapters**: Adapters apenas traduzem, não decidem
3. **Imports no meio do arquivo**: Sempre no topo
4. **Hard-coded values**: Use configuração ou constantes
5. **Funções muito longas**: Prefira funções pequenas e focadas
6. **Ignorar erros**: Sempre trate exceções adequadamente
7. **DTOs de domínio em shared**: Cada Bounded Context deve ter seus próprios DTOs (ver ADR-002)

## Boas Práticas

- **Imports organizados**: Agrupe por pacote
- **Documentação**: Documente interfaces públicas
- **Imutabilidade**: Prefira `val` sobre `var`
- **Null Safety**: Use tipos nullable com cuidado
- **Extension Functions**: Use para adicionar funcionalidade sem herança
