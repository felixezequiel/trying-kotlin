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

## Value Objects

> **ADR Relacionada**: [ADR-009: Value Objects Pattern](../adrs/009-value-objects-pattern.md)

### Regra: Validações de domínio devem estar em Value Objects

Value Objects encapsulam validações e comportamentos no domínio, evitando lógica espalhada em Use Cases.

| Aspecto | Descrição |
|---------|-----------|
| **Localização** | `services/{service}/domain/valueObjects/` |
| **Padrão** | `@JvmInline value class` com construtor privado |
| **Factory** | Usar `of()` ou `fromString()` no companion object |

### Padrão de Implementação

```kotlin
// domain/valueObjects/Price.kt
@JvmInline
value class Price private constructor(val value: BigDecimal) {
    
    init {
        require(value >= BigDecimal.ZERO) { 
            "Preço deve ser maior ou igual a zero" 
        }
    }
    
    companion object {
        fun of(value: BigDecimal): Price = Price(value)
        
        fun fromString(value: String): Price {
            val decimal = value.toBigDecimalOrNull()
                ?: throw IllegalArgumentException("Preço inválido: $value")
            return Price(decimal)
        }
    }
    
    operator fun plus(other: Price): Price = Price(value + other.value)
    
    override fun toString(): String = value.toString()
}
```

### ❌ NÃO fazer

```kotlin
// Use Case com validação - ERRADO!
class CreateTicketTypeUseCase(...) {
    suspend fun execute(request: CreateTicketTypeRequest): UUID {
        val price = try {
            BigDecimal(request.price)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Preço inválido")
        }
        if (price < BigDecimal.ZERO) {
            throw IllegalArgumentException("Preço deve ser >= 0")
        }
        // ...
    }
}
```

### ✅ Fazer

```kotlin
// Use Case usando Value Object - CORRETO
class CreateTicketTypeUseCase(...) {
    suspend fun execute(request: CreateTicketTypeRequest): UUID {
        val price = Price.fromString(request.price)
        val ticketType = TicketType(
            price = price,
            // ...
        )
        return ticketTypeRepository.add(ticketType)
    }
}
```

### Quando criar Value Objects

| Criar VO | Não criar VO |
|----------|--------------|
| Valores com regras de validação | Strings simples sem regras |
| Valores com comportamento (operações) | IDs simples (usar UUID diretamente) |
| Valores reutilizados em múltiplos contextos | Valores únicos sem lógica |

### Value Objects por Serviço

| Serviço | Value Objects |
|---------|---------------|
| **Users** | `Email`, `Password`, `UserName` |
| **Partners** | `Email`, `CompanyName`, `Document` |
| **Events** | `EventName`, `EventDescription`, `DateRange` |
| **Tickets** | `Price`, `Quantity`, `TicketName` |

---

## Unit of Work

> **Regra**: Toda operação que envolve persistência de dados DEVE utilizar o padrão Unit of Work.

### Conceito

O Unit of Work gerencia transações e garante consistência nas operações de dados, coordenando múltiplos repositórios em uma única transação.

| Aspecto | Descrição |
|---------|-----------|
| **Interface** | `IUnitOfWork` em `application/ports/out/` |
| **Implementação** | `UnitOfWorkAdapter` em `adapters/out/persistence/` |
| **Responsabilidade** | Gerenciar transações e expor repositórios |

### Padrão de Interface

```kotlin
// application/ports/out/IUnitOfWork.kt
interface IUnitOfWork {
    val userRepository: IUserRepository
    val partnerRepository: IPartnerRepository
    // ... outros repositórios
    
    suspend fun <T> transaction(block: suspend () -> T): T
}
```

### Padrão de Uso em Use Cases

```kotlin
// application/useCases/CreatePartnerUseCase.kt
class CreatePartnerUseCase(private val unitOfWork: IUnitOfWork) {
    
    suspend fun execute(request: CreatePartnerRequest): UUID {
        return unitOfWork.transaction {
            val partner = Partner(
                name = CompanyName.of(request.name),
                email = Email.of(request.email)
            )
            unitOfWork.partnerRepository.add(partner)
        }
    }
}
```

### ❌ NÃO fazer

```kotlin
// Injetar repositório diretamente - ERRADO!
class CreatePartnerUseCase(private val partnerRepository: IPartnerRepository) {
    suspend fun execute(request: CreatePartnerRequest): UUID {
        // Sem controle transacional
        return partnerRepository.add(partner)
    }
}
```

### ✅ Fazer

```kotlin
// Usar Unit of Work - CORRETO
class CreatePartnerUseCase(private val unitOfWork: IUnitOfWork) {
    suspend fun execute(request: CreatePartnerRequest): UUID {
        return unitOfWork.transaction {
            // Operações dentro de transação
            unitOfWork.partnerRepository.add(partner)
        }
    }
}
```

### Quando usar `transaction {}`

| Usar `transaction {}` | Pode omitir |
|-----------------------|-------------|
| Operações de escrita (INSERT, UPDATE, DELETE) | Leituras simples (queries) |
| Múltiplas operações que devem ser atômicas | Operações já dentro de outra transação |
| Operações que dependem de consistência | - |

### Justificativa

- **Consistência**: Garante atomicidade em operações complexas
- **Testabilidade**: Facilita mocking em testes unitários
- **Desacoplamento**: Use Cases não conhecem detalhes de persistência
- **Rollback automático**: Em caso de erro, todas as operações são revertidas

### Abstração de Persistência

> **ADR Relacionada**: [ADR-010: Unit of Work e Abstração de Persistência](../adrs/010-unit-of-work-persistence-abstraction.md)

O Unit of Work deve ser implementado de forma que a troca de mecanismo de persistência (memória, PostgreSQL, MongoDB, etc.) seja **transparente** para os Use Cases.

#### Regras de Implementação

| Regra | Descrição |
|-------|-----------|
| **Repositórios injetados** | Repositórios devem ser injetados no UnitOfWork, não criados internamente |
| **TransactionManager abstrato** | Usar interface `ITransactionManager` para gerenciar transações |
| **Sem vazamento de infra** | `DatabaseContext` ou conexões de banco não devem vazar para adapters |

#### ❌ NÃO fazer (vazamento de infraestrutura)

```kotlin
// UnitOfWorkAdapter - ERRADO!
// DatabaseContext é classe concreta de infraestrutura
class UnitOfWorkAdapter(private val dbContext: DatabaseContext) : IUnitOfWork {
    override fun eventRepository(): IEventRepository {
        return EventRepositoryAdapter(dbContext) // Cria nova instância sempre!
    }
}
```

#### ✅ Fazer (abstração correta)

```kotlin
// application/ports/out/ITransactionManager.kt
interface ITransactionManager {
    suspend fun <T> execute(block: suspend () -> T): T
}

// adapters/out/persistence/UnitOfWorkAdapter.kt
class UnitOfWorkAdapter(
    override val eventRepository: IEventRepository,
    private val transactionManager: ITransactionManager
) : IUnitOfWork {
    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return transactionManager.execute(block)
    }
}
```

#### EventStore - Encapsulamento completo

Para garantir que detalhes de infraestrutura não vazem, criamos um `EventStore` que encapsula repositório e transaction manager:

```kotlin
// adapters/out/InMemoryEventStore.kt
class InMemoryEventStore {
    private val events = mutableListOf<Event>()
    
    val repository: IEventRepository = InMemoryEventRepository()
    val transactionManager: ITransactionManager = InMemoryTransactionManagerImpl()
    
    // Classes internas que compartilham o estado `events`
    private inner class InMemoryEventRepository : IEventRepository { ... }
    private inner class InMemoryTransactionManagerImpl : ITransactionManager { ... }
}
```

#### Composição na Application

```kotlin
// infrastructure/web/Application.kt

// Para memória:
val eventStore = InMemoryEventStore()
val unitOfWork = UnitOfWorkAdapter(eventStore.repository, eventStore.transactionManager)

// Para PostgreSQL - bastaria criar e trocar:
// val postgresStore = PostgresEventStore(connectionString)
// val unitOfWork = UnitOfWorkAdapter(postgresStore.repository, postgresStore.transactionManager)
```

---

## Anti-Patterns a Evitar

1. **Dependências circulares**: Domain nunca deve importar de Application ou Infrastructure
2. **Lógica de negócio em Adapters**: Adapters apenas traduzem, não decidem
3. **Imports no meio do arquivo**: Sempre no topo
4. **Hard-coded values**: Use configuração ou constantes
5. **Funções muito longas**: Prefira funções pequenas e focadas
6. **Ignorar erros**: Sempre trate exceções adequadamente
7. **DTOs de domínio em shared**: Cada Bounded Context deve ter seus próprios DTOs (ver ADR-002)
8. **Validações em Use Cases**: Encapsular em Value Objects (ver ADR-009)
9. **Injetar repositórios diretamente em Use Cases**: Sempre usar Unit of Work para operações de dados
10. **Vazamento de infraestrutura em Adapters**: DatabaseContext ou conexões não devem ser dependências de Adapters (ver ADR-010)

## Boas Práticas

- **Imports organizados**: Agrupe por pacote
- **Documentação**: Documente interfaces públicas
- **Imutabilidade**: Prefira `val` sobre `var`
- **Null Safety**: Use tipos nullable com cuidado
- **Extension Functions**: Use para adicionar funcionalidade sem herança
