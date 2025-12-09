# ADR-009: Value Objects Pattern

## Status

Aceito

## Data

2024-12-09

## Contexto

Atualmente, as validações e conversões de dados estão espalhadas pelos Use Cases e Controllers, violando princípios fundamentais do DDD:

### Problema Atual

```kotlin
// CreateTicketTypeUseCase.kt - Code Smell
class CreateTicketTypeUseCase(...) {
    suspend fun execute(request: CreateTicketTypeRequest): UUID {
        // Validação de preço no Use Case - ERRADO
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

### Problemas Identificados

1. **Validações duplicadas** em múltiplos Use Cases
2. **Lógica de domínio vazando** para camada de aplicação
3. **Entidades anêmicas** sem comportamento
4. **Difícil manutenção** - mudança de regra requer alteração em vários lugares
5. **Testes redundantes** - mesma validação testada em múltiplos lugares

## Decisão

Adotar **Value Objects** para encapsular validações e comportamentos no domínio.

### Estrutura

```
services/<feature>/domain/
├── <Entity>.kt           # Entidade principal
├── <EntityStatus>.kt     # Enum de status (se houver)
└── valueObjects/         # Value Objects do domínio
    ├── Email.kt
    ├── Price.kt
    ├── Quantity.kt
    └── ...
```

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
    operator fun times(quantity: Int): Price = Price(value * quantity.toBigDecimal())
    
    override fun toString(): String = value.toString()
}
```

### Benefícios do `@JvmInline value class`

- **Zero overhead** em runtime (unwrapped pelo compilador)
- **Type safety** em compile time
- **Imutabilidade** garantida
- **Validação no construtor** - impossível criar instância inválida

### Value Objects por Serviço

| Serviço | Value Objects |
|---------|---------------|
| **Users** | `Email`, `Password`, `UserName` |
| **Partners** | `Email`, `CompanyName`, `Document` |
| **Events** | `EventName`, `EventDescription`, `DateRange` |
| **Tickets** | `Price`, `Quantity`, `TicketName` |

### Uso nas Entidades

```kotlin
// domain/TicketType.kt
data class TicketType(
    val id: UUID = UUID.randomUUID(),
    val eventId: UUID,
    val name: TicketName,           // Value Object
    val description: String,
    val price: Price,               // Value Object
    val totalQuantity: Quantity,    // Value Object
    val availableQuantity: Quantity,
    val maxPerCustomer: Quantity,
    // ...
)
```

### Uso nos Use Cases

```kotlin
// CreateTicketTypeUseCase.kt - CORRETO
class CreateTicketTypeUseCase(...) {
    suspend fun execute(request: CreateTicketTypeRequest): UUID {
        // Validação encapsulada no Value Object
        val price = Price.fromString(request.price)
        val totalQuantity = Quantity.of(request.totalQuantity)
        val maxPerCustomer = Quantity.of(request.maxPerCustomer, min = 1)
        
        val ticketType = TicketType(
            eventId = UUID.fromString(request.eventId),
            name = TicketName.of(request.name),
            price = price,
            totalQuantity = totalQuantity,
            // ...
        )
        
        return ticketTypeRepository.add(ticketType)
    }
}
```

## Alternativas Consideradas

### Alternativa 1: Validação em DTOs

- **Descrição**: Usar annotations de validação nos DTOs
- **Prós**: Simples, suportado por frameworks
- **Contras**: Validação fora do domínio, DTOs com lógica
- **Motivo da rejeição**: Viola separação de responsabilidades

### Alternativa 2: Validation Service

- **Descrição**: Serviço centralizado de validação
- **Prós**: Centralizado
- **Contras**: Lógica de domínio fora do domínio, acoplamento
- **Motivo da rejeição**: Anti-pattern em DDD

## Consequências

### Positivas

- **Single Source of Truth** - validação em um único lugar
- **Domínio rico** - entidades com comportamento
- **Type Safety** - compilador previne erros
- **Testabilidade** - Value Objects testados isoladamente
- **Reutilização** - mesmo Value Object em múltiplos contextos

### Negativas

- **Mais arquivos** - cada Value Object é uma classe
- **Curva de aprendizado** - equipe precisa entender o padrão
- **Serialização** - requer adaptadores para JSON

### Riscos

| Risco | Mitigação |
|-------|-----------|
| Over-engineering | Criar VOs apenas para valores com regras |
| Serialização complexa | Usar serializers customizados |

## Implementação

1. Criar pasta `valueObjects/` em cada serviço
2. Identificar valores com regras de validação
3. Criar Value Objects com validação no construtor
4. Refatorar entidades para usar Value Objects
5. Refatorar Use Cases para usar factory methods
6. Atualizar testes
7. Garantir serialização correta nos DTOs

## Referências

- [DDD - Value Objects](https://martinfowler.com/bliki/ValueObject.html)
- [Kotlin Inline Classes](https://kotlinlang.org/docs/inline-classes.html)
- [ADR-002: DTO Isolation](002-dto-isolation-per-bounded-context.md)
