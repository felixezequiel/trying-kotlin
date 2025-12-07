# Estrutura de Pastas e Arquivos - Padrão do Projeto

Este documento define o padrão de organização de pastas e arquivos que DEVE ser seguido sempre que criar novos arquivos ou pastas no projeto.

## Arquitetura

O projeto segue os padrões **Vertical Slice Architecture** e **Hexagonal Architecture (Ports and Adapters)**.

## Estrutura Base por Feature (Vertical Slice)

Cada feature/bounded context deve ter sua própria pasta na raiz do projeto, seguindo a estrutura abaixo:

```
<feature-name>/
  ├── domain/                    # Entidades e objetos de valor do domínio
  ├── application/
  │   ├── ports/
  │   │   ├── in/               # Portas de entrada (interfaces para controllers, handlers)
  │   │   └── out/               # Portas de saída (interfaces para repositórios, serviços externos)
  │   └── useCases/              # Casos de uso da aplicação
  ├── adapters/
  │   ├── in/                    # Adapters de entrada (controllers, handlers HTTP, CLI)
  │   └── out/                   # Adapters de saída (repositórios, clientes de APIs externas)
  ├── infrastructure/
  │   └── persistence/           # Implementações de persistência (banco de dados, cache)
  │   └── external/              # Integrações com serviços externos (se necessário)
  └── index.kt                   # Ponto de entrada da feature (opcional)
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
- **Localização**: `<feature>/domain/`
- **Exemplo**: `users/domain/User.kt`
- **Package**: `<feature>.domain`

### Interfaces (Ports)
- **Portas de entrada**: `<feature>/application/ports/in/`
- **Portas de saída**: `<feature>/application/ports/out/`
- **Nomenclatura**: `I` + Nome (ex: `IUserRepository.kt`)
- **Package**: `<feature>.application.ports.in` ou `<feature>.application.ports.out`

### Use Cases
- **Localização**: `<feature>/application/useCases/`
- **Nomenclatura**: Nome + `UseCase` (ex: `UserUseCase.kt`)
- **Package**: `<feature>.application.useCases`

### Adapters de Entrada
- **Localização**: `<feature>/adapters/in/`
- **Nomenclatura**: Nome + `Adapter` ou `Controller` (ex: `UserController.kt`)
- **Package**: `<feature>.adapters.in`

### Adapters de Saída
- **Localização**: `<feature>/adapters/out/`
- **Nomenclatura**: Nome + `Adapter` (ex: `UserRepositoryAdapter.kt`)
- **Package**: `<feature>.adapters.out`

### Infraestrutura
- **Persistência**: `<feature>/infrastructure/persistence/`
- **Serviços externos**: `<feature>/infrastructure/external/`
- **Package**: `<feature>.infrastructure.persistence` ou `<feature>.infrastructure.external`

## Estrutura de Testes

Os testes DEVEM espelhar exatamente a estrutura do código fonte:

```
tests/
  └── <feature-name>/
      ├── domain/                    # Testes de entidades (se necessário)
      ├── application/
      │   ├── ports/                 # Testes de contratos/interfaces (se necessário)
      │   └── useCases/
      │       └── <Nome>UseCaseTest.kt
      ├── adapters/
      │   ├── in/
      │   │   └── <Nome>AdapterTest.kt
      │   └── out/
      │       └── <Nome>AdapterTest.kt
      └── infrastructure/
          └── persistence/
              └── <Nome>Test.kt
```

### Regras de Testes
- **Nomenclatura**: Nome da classe + `Test` (ex: `UserUseCaseTest.kt`)
- **Package**: Mesmo package do código testado
- **Localização**: Espelhar exatamente a estrutura de pastas do código fonte

## Exemplo Completo

Para uma feature `orders`:

```
orders/
  ├── domain/
  │   └── Order.kt
  ├── application/
  │   ├── ports/
  │   │   ├── in/
  │   │   │   └── IOrderHandler.kt
  │   │   └── out/
  │   │       ├── IOrderRepository.kt
  │   │       └── IPaymentService.kt
  │   └── useCases/
  │       ├── CreateOrderUseCase.kt
  │       └── CancelOrderUseCase.kt
  ├── adapters/
  │   ├── in/
  │   │   └── OrderController.kt
  │   └── out/
  │       ├── OrderRepositoryAdapter.kt
  │       └── PaymentServiceAdapter.kt
  ├── infrastructure/
  │   └── persistence/
  │       └── OrderDatabaseContext.kt
  └── index.kt

tests/
  └── orders/
      ├── application/
      │   └── useCases/
      │       ├── CreateOrderUseCaseTest.kt
      │       └── CancelOrderUseCaseTest.kt
      ├── adapters/
      │   ├── in/
      │   │   └── OrderControllerTest.kt
      │   └── out/
      │       ├── OrderRepositoryAdapterTest.kt
      │       └── PaymentServiceAdapterTest.kt
      └── infrastructure/
          └── persistence/
              └── OrderDatabaseContextTest.kt
```

## Checklist ao Criar Arquivos

Antes de criar qualquer arquivo, verifique:

- [ ] A pasta da feature existe na raiz?
- [ ] O arquivo está na pasta correta conforme seu tipo?
- [ ] O nome segue o padrão de nomenclatura?
- [ ] O package corresponde à estrutura de pastas?
- [ ] Se for uma classe testável, o teste foi criado na estrutura espelhada em `tests/`?
- [ ] O teste segue a nomenclatura `NomeClasseTest.kt`?

## Princípios Importantes

1. **Separação de Responsabilidades**: Cada camada tem uma responsabilidade clara
2. **Dependências**: Domain não depende de nada. Application depende apenas de Domain e Ports. Adapters implementam Ports.
3. **Testabilidade**: Tudo deve ser testável através de interfaces (Ports)
4. **Espelhamento**: Testes sempre espelham a estrutura do código fonte

