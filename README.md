# MyApp - Monorepo

Arquitetura de microserviços com BFF (Backend for Frontend) usando Kotlin e Ktor.

## Estrutura do Projeto

```
myapp/
├── bff/                    # Backend for Frontend (API Gateway)
├── services/
│   └── users/              # Serviço de usuários
├── shared/                 # Código compartilhado (DTOs, utils)
├── context/
│   ├── adrs/               # Architecture Decision Records
│   └── agents/             # Documentação para agentes IA
└── tests/                  # Testes
```

## Requisitos

- JDK 17+
- Gradle 8.5+
- Docker e Docker Compose (para ambiente containerizado)

## Comandos de Desenvolvimento

### Build

```bash
# Build de todos os módulos
gradle build

# Build sem testes
gradle build -x test
```

### Executar Localmente (sem Docker)

```bash
# Executar serviço Users (porta 8081)
gradle :services:users:run

# Executar BFF (porta 8080) - em outro terminal
gradle :bff:run
```

### Executar com Docker Compose

```bash
# Produção
docker-compose up --build

# Desenvolvimento com hot reload
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

### Testes

```bash
# Todos os testes
gradle test

# Testes de um módulo específico
gradle :services:users:test
gradle :bff:test
```

## Portas dos Serviços

| Serviço | Porta |
|---------|-------|
| BFF | 8080 |
| Users | 8081 |

## Endpoints

### BFF (porta 8080)

- `GET /health` - Health check
- `GET /api/users?email={email}` - Buscar usuário por email
- `GET /api/users/all` - Listar todos os usuários
- `POST /api/users` - Registrar novo usuário
- `GET /graphql` - GraphQL Playground

### Users Service (porta 8081)

- `GET /users?email={email}` - Buscar usuário por email
- `GET /users/all` - Listar todos os usuários
- `POST /users` - Registrar novo usuário
- `GET /graphql` - GraphQL Playground

## Arquitetura

Consulte o ADR em `context/adrs/001-bff-microservices-architecture.md` para detalhes completos da arquitetura.

### Comunicação

```
Clientes → BFF (8080) → Services (8081+)
```

- Clientes se comunicam apenas com o BFF
- BFF roteia requisições para os serviços internos
- Serviços não se comunicam diretamente entre si

## Criando Novos Serviços

1. Criar pasta em `services/<nome-servico>/`
2. Adicionar `build.gradle.kts` baseado no template de `services/users/`
3. Registrar no `settings.gradle.kts`
4. Criar cliente no BFF em `bff/clients/`
5. Adicionar rotas no BFF
