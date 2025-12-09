# ADR-002: Isolamento de DTOs por Bounded Context

## Status

Aceito

## Data

2024-12-09

## Contexto

Durante o desenvolvimento do BFF e dos microserviços, identificamos a existência de DTOs similares em diferentes módulos:

- **BFF** (`bff/clients/`): `RegisterUserRequest`, `UserResponse`
- **Users Service** (`services/users/application/dto/`): `RegisterUserRequest`, `UserResponse`

Surgiu a questão: devemos centralizar esses DTOs no módulo `shared` para evitar duplicação?

### Forças em jogo

1. **DRY (Don't Repeat Yourself)**: Tendência natural de eliminar código duplicado
2. **DDD - Bounded Contexts**: Cada contexto deve ser autônomo e independente
3. **Hexagonal Architecture - Anti-Corruption Layer (ACL)**: Proteção contra mudanças externas
4. **Evolução independente**: Serviços podem evoluir em ritmos diferentes

## Decisão

**Manteremos DTOs isolados em cada Bounded Context.** Cada módulo (BFF, Users Service, etc.) terá seus próprios DTOs, mesmo que pareçam similares.

### Estrutura definida

```
bff/
  clients/
    UsersClient.kt          # Contém DTOs específicos do BFF
    
services/users/
  application/
    dto/
      RegisterUserRequest.kt  # DTOs específicos do serviço Users
      UserResponse.kt
      
shared/
  dto/
    PaginatedResponse.kt     # Apenas estruturas genéricas de infraestrutura
    ServiceResponse.kt
  exceptions/
    ServiceException.kt
```

### O que vai em cada local

| Local | Conteúdo | Exemplo |
|-------|----------|---------|
| `services/{service}/application/dto/` | DTOs do contrato da API do serviço | `UserResponse`, `RegisterUserRequest` |
| `bff/clients/` | DTOs para comunicação com serviços (ACL) | `UserResponse`, `RegisterUserRequest` |
| `shared/dto/` | **Apenas** estruturas genéricas de infraestrutura | `PaginatedResponse<T>`, `ServiceResponse<T>` |
| `shared/exceptions/` | Exceções comuns de infraestrutura | `ServiceException`, `NotFoundException` |

### O que NÃO deve ir para shared

- DTOs de domínio específico (User, Order, Product, etc.)
- Contratos de API de serviços específicos
- Qualquer estrutura que represente conceitos de um Bounded Context

## Alternativas Consideradas

### Alternativa 1: Centralizar DTOs em shared

- **Descrição**: Mover todos os DTOs comuns para `shared/dto/`
- **Prós**: 
  - Elimina duplicação de código
  - Garante consistência de tipos entre módulos
- **Contras**: 
  - Cria acoplamento forte entre Bounded Contexts
  - Mudança em um serviço afeta todos os consumidores
  - Viola o princípio de Anti-Corruption Layer
  - Impede evolução independente dos serviços
- **Motivo da rejeição**: Viola princípios fundamentais de DDD e Hexagonal Architecture

### Alternativa 2: Criar módulo shared por domínio

- **Descrição**: Criar `shared-users`, `shared-orders`, etc.
- **Prós**: Organização por domínio
- **Contras**: 
  - Ainda cria acoplamento
  - Complexidade adicional de módulos
- **Motivo da rejeição**: Não resolve o problema fundamental de acoplamento

## Consequências

### Positivas

- **Isolamento de Bounded Contexts**: Cada contexto evolui independentemente
- **Anti-Corruption Layer**: BFF protegido de mudanças nos serviços
- **Flexibilidade**: DTOs podem divergir conforme necessidade (ex: `id: String` no BFF vs `id: Long` no serviço)
- **Conformidade com DDD/Hexagonal**: Arquitetura alinhada com as melhores práticas

### Negativas

- **Duplicação aparente**: Código similar em múltiplos lugares
- **Manutenção**: Alterações de contrato requerem mudanças em múltiplos locais
- **Curva de aprendizado**: Desenvolvedores precisam entender o motivo da separação

### Riscos

- **Divergência não intencional**: DTOs podem divergir acidentalmente
  - *Mitigação*: Testes de integração/contrato entre BFF e serviços
- **Confusão de desenvolvedores**: Podem não entender por que não centralizar
  - *Mitigação*: Esta ADR documenta a decisão e justificativa

## Implementação

1. Manter DTOs de domínio em cada módulo específico
2. Usar `shared/` apenas para infraestrutura transversal
3. Documentar em `codingConventions.md` a regra sobre DTOs
4. Revisar PRs para garantir conformidade

## Referências

- [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/ddd/)
- [Hexagonal Architecture - Alistair Cockburn](https://alistair.cockburn.us/hexagonal-architecture/)
- [Anti-Corruption Layer Pattern](https://docs.microsoft.com/en-us/azure/architecture/patterns/anti-corruption-layer)
- [Bounded Context - Martin Fowler](https://martinfowler.com/bliki/BoundedContext.html)
