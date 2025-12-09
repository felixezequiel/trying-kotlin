# ADR-010: Unit of Work e Abstração de Persistência

## Status

Aceito

## Data

2024-12-09

## Contexto

O projeto utiliza o padrão Unit of Work para gerenciar transações e coordenar repositórios. No entanto, a implementação inicial apresentava problemas de acoplamento que dificultariam a troca de mecanismo de persistência (ex: de memória para PostgreSQL).

### Problemas identificados

1. **DatabaseContext acoplado**: `UnitOfWorkAdapter` e `EventRepositoryAdapter` dependiam diretamente de `DatabaseContext` (classe concreta de infraestrutura)
2. **Criação de repositório a cada chamada**: `eventRepository()` criava nova instância sempre, causando problemas potenciais com conexões de banco
3. **Vazamento de infraestrutura**: Classes de infraestrutura (`DatabaseContext`) vazavam para a camada de adapters

### Código problemático

```kotlin
// UnitOfWorkAdapter - PROBLEMÁTICO
class UnitOfWorkAdapter(private val dbContext: DatabaseContext) : IUnitOfWork {
    override fun eventRepository(): IEventRepository {
        return EventRepositoryAdapter(dbContext) // Cria nova instância sempre!
    }
}
```

## Decisão

Adotaremos uma arquitetura onde:

1. **Repositórios são injetados no UnitOfWork** ao invés de criados internamente
2. **TransactionManager é uma interface** para abstrair o gerenciamento de transações
3. **DatabaseContext fica isolado** na camada de infraestrutura, sem vazar para adapters

### Estrutura de interfaces

```kotlin
// application/ports/out/ITransactionManager.kt
interface ITransactionManager {
    suspend fun <T> execute(block: suspend () -> T): T
}

// application/ports/out/IUnitOfWork.kt
interface IUnitOfWork {
    val eventRepository: IEventRepository
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}
```

### Implementação correta

```kotlin
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

### IEventStore - Interface para garantir contrato

Para garantir que qualquer implementação de Store (InMemory, Postgres, etc.) tenha o mesmo contrato, criamos uma interface:

```kotlin
// application/ports/out/IEventStore.kt
interface IEventStore {
    val repository: IEventRepository
    val transactionManager: ITransactionManager
}
```

### EventStore - Encapsulamento completo

A implementação em memória implementa a interface `IEventStore`:

```kotlin
// adapters/out/InMemoryEventStore.kt
class InMemoryEventStore : IEventStore {
    private val events = mutableListOf<Event>()
    
    override val repository: IEventRepository = InMemoryEventRepository()
    override val transactionManager: ITransactionManager = InMemoryTransactionManagerImpl()
    
    // Classes internas que compartilham o estado `events`
    private inner class InMemoryEventRepository : IEventRepository { ... }
    private inner class InMemoryTransactionManagerImpl : ITransactionManager { ... }
}
```

**Benefícios da interface `IEventStore`:**
1. Garante que qualquer Store tenha `repository` e `transactionManager`
2. Permite trocar implementações sem risco de esquecer propriedades
3. Facilita testes com mocks

### Composição na camada de infraestrutura

```kotlin
// infrastructure/web/Application.kt

// Para memória - usando EventStore encapsulado:
val eventStore = InMemoryEventStore()
val unitOfWork = UnitOfWorkAdapter(eventStore.repository, eventStore.transactionManager)

// Para PostgreSQL - bastaria criar:
// val postgresStore = PostgresEventStore(connectionString)
// val unitOfWork = UnitOfWorkAdapter(postgresStore.repository, postgresStore.transactionManager)
```

## Alternativas Consideradas

### Alternativa 1: Manter DatabaseContext nos Adapters
- **Descrição**: Continuar com a implementação atual
- **Prós**: Menos código, mais simples inicialmente
- **Contras**: Acoplamento forte, difícil trocar persistência
- **Motivo da rejeição**: Viola princípios de Hexagonal Architecture

### Alternativa 2: Factory Pattern para Repositórios
- **Descrição**: Usar factory para criar repositórios
- **Prós**: Flexível para criação
- **Contras**: Adiciona complexidade desnecessária, ainda cria instâncias múltiplas
- **Motivo da rejeição**: Injeção direta é mais simples e testável

## Consequências

### Positivas
- **Troca transparente de persistência**: Basta criar novas implementações e injetar
- **Testabilidade**: Fácil mockar repositórios e transaction manager
- **Princípio de Inversão de Dependência**: Adapters dependem de interfaces, não de classes concretas
- **Single Responsibility**: Cada classe tem uma única responsabilidade

### Negativas
- **Mais interfaces**: Necessário criar `ITransactionManager`
- **Composição manual**: Injeção de dependências deve ser feita manualmente (ou via DI container)

### Riscos
- **Risco**: Desenvolvedores podem criar implementações que vazam infraestrutura
- **Mitigação**: Documentação clara e code review

## Implementação

1. Criar interface `ITransactionManager` em `application/ports/out/`
2. Atualizar `IUnitOfWork` para usar propriedade ao invés de função para repositório
3. Criar `InMemoryTransactionManager` em `adapters/out/persistence/`
4. Atualizar `UnitOfWorkAdapter` para receber dependências via construtor
5. Atualizar `Application.kt` para compor as dependências corretamente
6. Atualizar testes para refletir a nova estrutura

## Referências

- [Unit of Work Pattern - Martin Fowler](https://martinfowler.com/eaaCatalog/unitOfWork.html)
- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Dependency Inversion Principle](https://en.wikipedia.org/wiki/Dependency_inversion_principle)
