# Estrutura de Pastas e Arquivos - Padrão do Projeto

Este documento define o padrão de organização de pastas e arquivos que DEVE ser seguido sempre que criar novos arquivos ou pastas no projeto.

## Arquitetura

O projeto segue os padrões **Vertical Slice Architecture** e **Hexagonal Architecture (Ports and Adapters)** com arquitetura de **microserviços**.

## Estrutura Raiz do Projeto

```
projeto/
  ├── bff/                       # Backend for Frontend (GraphQL gateway)
  ├── services/                  # Microserviços (cada feature é um serviço)
  │   └── <feature-name>/
  ├── shared/                    # Código compartilhado entre serviços
  ├── context/
    ├── adrs/                     # Architecture Decision Records
    ├── agents/                   # Documentação para agentes IA
    └── intent/                   # Documentos de intenção (visão geral de sistemas)
  ├── tests/                     # Testes (espelha estrutura de services/)
  └── docker-compose.yml         # Orquestração dos serviços
```

## Estrutura de Microserviço (Vertical Slice + Hexagonal)

Cada microserviço em `services/<feature>/` segue a estrutura:

```
services/<feature-name>/
  ├── domain/                    # Entidades e objetos de valor do domínio
  ├── application/
  │   ├── dto/                   # Data Transfer Objects
  │   ├── ports/
  │   │   └── outbound/          # Portas de saída (interfaces para repositórios, serviços externos)
  │   └── useCases/              # Casos de uso da aplicação
  ├── adapters/
  │   ├── inbound/               # Adapters de entrada (controllers HTTP)
  │   └── outbound/              # Adapters de saída (repositórios, clientes externos)
  ├── infrastructure/
  │   ├── persistence/           # Implementações de persistência (banco de dados)
  │   └── web/                   # Configuração do servidor web (Ktor)
  ├── build.gradle.kts           # Dependências do serviço
  └── Dockerfile                 # Container do serviço
```

## Estrutura do BFF (Backend for Frontend)

```
bff/
  ├── clients/                   # Clientes HTTP para comunicação com microserviços
  ├── graphql/                   # Schema e resolvers GraphQL
  ├── routes/                    # Rotas HTTP (REST endpoints)
  ├── Application.kt             # Ponto de entrada
  ├── build.gradle.kts
  └── Dockerfile
```

## Estrutura Shared (Código Compartilhado)

```
shared/
  ├── dto/                       # DTOs compartilhados (PaginatedResponse, ServiceResponse)
  ├── exceptions/                # Exceções compartilhadas
  ├── utils/                     # Utilitários (EnvConfig, etc)
  └── build.gradle.kts
```

## Regras de Nomenclatura

### Pastas
- **Nomes em minúsculas**: `users/`, `orders/`, `payments/`
- **Sem espaços ou caracteres especiais**: use `-` ou `_` se necessário
- **Singular ou plural**: siga o padrão do domínio (ex: `users` para múltiplos usuários)

### Arquivos
- **Nomes em PascalCase para classes**: `UserUseCase.kt`, `UserRepositoryAdapter.kt`
- **Nomes em camelCase para arquivos de funções/utilitários**: `userHelper.kt`
- **Sufixos obrigatórios**:
  - Interfaces: `I` + Nome (ex: `IUserRepository.kt`)
  - Adapters: Nome + `Adapter` (ex: `UserRepositoryAdapter.kt`)
  - Use Cases: Nome + `UseCase` (ex: `UserUseCase.kt`)
  - Entidades de domínio: Nome direto (ex: `User.kt`)

### Packages
- **Seguir a estrutura de pastas**: `users.application.useCases`
- **Tudo em minúsculas**: `users.domain`, `users.adapters.out`
- **Sem underscores**: use pontos para separar níveis

## Onde Colocar Cada Tipo de Arquivo

### Entidades de Domínio
- **Localização**: `services/<feature>/domain/`
- **Exemplo**: `services/users/domain/User.kt`
- **Package**: `<feature>.domain`

### DTOs (Data Transfer Objects)
- **Localização**: `services/<feature>/application/dto/`
- **Exemplo**: `services/users/application/dto/CreateUserRequest.kt`
- **Package**: `<feature>.application.dto`

### Interfaces (Ports)
- **Portas de saída**: `services/<feature>/application/ports/outbound/`
- **Nomenclatura**: `I` + Nome (ex: `IUserRepository.kt`)
- **Package**: `<feature>.application.ports.outbound`

### Use Cases
- **Localização**: `services/<feature>/application/useCases/`
- **Nomenclatura**: Nome + `UseCase` (ex: `UserUseCase.kt`)
- **Package**: `<feature>.application.useCases`

### Adapters de Entrada
- **Localização**: `services/<feature>/adapters/inbound/`
- **Nomenclatura**: Nome + `Controller` (ex: `UserController.kt`)
- **Package**: `<feature>.adapters.inbound`

### Adapters de Saída
- **Localização**: `services/<feature>/adapters/outbound/`
- **Nomenclatura**: Nome + `Adapter` (ex: `UserRepositoryAdapter.kt`)
- **Package**: `<feature>.adapters.outbound`

### Infraestrutura
- **Persistência**: `services/<feature>/infrastructure/persistence/`
- **Web (Ktor)**: `services/<feature>/infrastructure/web/`
- **Package**: `<feature>.infrastructure.persistence` ou `<feature>.infrastructure.web`

## Estrutura de Testes

Os testes DEVEM espelhar exatamente a estrutura do código fonte em `services/`:

```
tests/
  └── services/
      └── <feature-name>/
          ├── domain/                    # Testes de entidades (se necessário)
          ├── application/
          │   └── useCases/
          │       └── <Nome>UseCaseTest.kt
          ├── adapters/
          │   ├── inbound/
          │   │   └── <Nome>ControllerTest.kt
          │   └── outbound/
          │       └── <Nome>AdapterTest.kt
          └── infrastructure/
              └── persistence/
                  └── <Nome>Test.kt
```

### Regras de Testes
- **Nomenclatura**: Nome da classe + `Test` (ex: `UserUseCaseTest.kt`)
- **Package**: Mesmo package do código testado
- **Localização**: `tests/services/<feature>/` espelhando `services/<feature>/`

## Exemplo Completo

Para um novo microserviço `orders`:

```
services/
  └── orders/
      ├── domain/
      │   └── Order.kt
      ├── application/
      │   ├── dto/
      │   │   ├── CreateOrderRequest.kt
      │   │   └── OrderResponse.kt
      │   ├── ports/
      │   │   └── outbound/
      │   │       ├── IOrderRepository.kt
      │   │       └── IPaymentService.kt
      │   └── useCases/
      │       ├── CreateOrderUseCase.kt
      │       └── CancelOrderUseCase.kt
      ├── adapters/
      │   ├── inbound/
      │   │   └── OrderController.kt
      │   └── outbound/
      │       ├── OrderRepositoryAdapter.kt
      │       └── PaymentServiceAdapter.kt
      ├── infrastructure/
      │   ├── persistence/
      │   │   └── OrderDatabaseContext.kt
      │   └── web/
      │       └── Main.kt
      ├── build.gradle.kts
      └── Dockerfile

tests/
  └── services/
      └── orders/
          ├── application/
          │   └── useCases/
          │       ├── CreateOrderUseCaseTest.kt
          │       └── CancelOrderUseCaseTest.kt
          ├── adapters/
          │   ├── inbound/
          │   │   └── OrderControllerTest.kt
          │   └── outbound/
          │       ├── OrderRepositoryAdapterTest.kt
          │       └── PaymentServiceAdapterTest.kt
          └── infrastructure/
              └── persistence/
                  └── OrderDatabaseContextTest.kt
```

## Checklist ao Criar Arquivos

Antes de criar qualquer arquivo, verifique:

- [ ] O microserviço existe em `services/<feature>/`?
- [ ] O arquivo está na pasta correta conforme seu tipo?
- [ ] O nome segue o padrão de nomenclatura?
- [ ] O package corresponde à estrutura de pastas?
- [ ] Se for uma classe testável, o teste foi criado em `tests/services/<feature>/`?
- [ ] O teste segue a nomenclatura `NomeClasseTest.kt`?

## Princípios Importantes

1. **Microserviços Independentes**: Cada serviço em `services/` é deployável separadamente
2. **BFF como Gateway**: O BFF orquestra chamadas aos microserviços
3. **Shared para Reutilização**: Código comum vai em `shared/`
4. **Separação de Responsabilidades**: Cada camada tem uma responsabilidade clara
5. **Dependências**: Domain não depende de nada. Application depende apenas de Domain e Ports. Adapters implementam Ports.
6. **Testabilidade**: Tudo deve ser testável através de interfaces (Ports)
7. **Espelhamento**: Testes em `tests/services/` espelham `services/`

