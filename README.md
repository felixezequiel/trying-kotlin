# Ticket System - Monorepo

Sistema de venda de ingressos com arquitetura de microserviços, BFF (Backend for Frontend) e UI moderna.

## Estrutura do Projeto

```
ticket-system/
├── ui/                     # Frontend Next.js com shadcn/ui
├── bff/                    # Backend for Frontend (API Gateway)
├── services/
│   ├── users/              # Serviço de usuários
│   ├── events/             # Serviço de eventos
│   ├── partners/           # Serviço de parceiros
│   ├── tickets/            # Serviço de tipos de ingresso
│   ├── orders/             # Serviço de pedidos
│   └── reservations/       # Serviço de reservas
├── shared/                 # Código compartilhado (DTOs, utils)
├── context/
│   ├── adrs/               # Architecture Decision Records
│   └── agents/             # Documentação para agentes IA
├── scripts/                # Scripts de automação
└── tests/                  # Testes
```

## Requisitos

- JDK 17+
- Gradle 8.5+
- Node.js 18+ (para UI)
- Docker e Docker Compose (para ambiente containerizado)

## Quick Start

### Opção 1: Executar Tudo com Um Comando (Desenvolvimento Local)

**Windows (CMD):**
```bash
scripts\start-all.bat
```

**Windows (PowerShell):**
```powershell
.\scripts\start-all.ps1
```

**Linux/Mac:**
```bash
chmod +x scripts/start-all.sh
./scripts/start-all.sh
```

Depois, em outro terminal, inicie a UI:
```bash
cd ui
npm install
npm run dev
```

### Opção 2: Docker Compose (Recomendado)

```bash
# Subir todos os serviços
docker-compose up --build

# Ou em modo detached
docker-compose up -d --build
```

### Opção 3: Executar Serviços Individualmente

```bash
# Terminal 1 - Users Service
gradle :services:users:run

# Terminal 2 - Events Service
gradle :services:events:run

# Terminal 3 - Partners Service
gradle :services:partners:run

# Terminal 4 - Tickets Service
gradle :services:tickets:run

# Terminal 5 - BFF
gradle :bff:run

# Terminal 6 - UI
cd ui && npm install && npm run dev
```

## Portas dos Serviços

| Serviço | Porta | URL |
|---------|-------|-----|
| UI (Frontend) | 3000 | http://localhost:3000 |
| BFF | 8080 | http://localhost:8080 |
| Users | 8081 | http://localhost:8081 |
| Events | 8082 | http://localhost:8082 |
| Partners | 8083 | http://localhost:8083 |
| Tickets | 8084 | http://localhost:8084 |
| Orders | 8085 | http://localhost:8085 |
| Reservations | 8086 | http://localhost:8086 |

## Endpoints da API (BFF)

### Health Check
- `GET /health` - Status do BFF

### Users
- `GET /api/users?email={email}` - Buscar usuário por email
- `GET /api/users/all` - Listar todos os usuários
- `POST /api/users` - Registrar novo usuário

### Events
- `GET /api/events` - Listar eventos públicos
- `GET /api/events/admin?status={status}` - Listar todos eventos (admin)
- `GET /api/events/partner/{partnerId}` - Listar eventos do partner
- `GET /api/events/{id}` - Buscar evento por ID
- `POST /api/events` - Criar evento
- `PUT /api/events/{id}` - Atualizar evento
- `POST /api/events/{id}/publish` - Publicar evento
- `POST /api/events/{id}/cancel` - Cancelar evento
- `POST /api/events/{id}/finish` - Finalizar evento

### Partners
- `GET /api/partners?status={status}` - Listar parceiros
- `GET /api/partners/{id}` - Buscar parceiro por ID
- `POST /api/partners` - Criar parceiro
- `PUT /api/partners/{id}` - Atualizar parceiro
- `POST /api/partners/{id}/approve` - Aprovar parceiro
- `POST /api/partners/{id}/reject` - Rejeitar parceiro
- `POST /api/partners/{id}/suspend` - Suspender parceiro
- `POST /api/partners/{id}/reactivate` - Reativar parceiro

### Tickets (Tipos de Ingresso)
- `GET /api/ticket-types/event/{eventId}` - Listar tipos por evento
- `GET /api/ticket-types/{id}` - Buscar tipo por ID
- `POST /api/ticket-types` - Criar tipo de ingresso
- `PUT /api/ticket-types/{id}` - Atualizar tipo
- `DELETE /api/ticket-types/{id}` - Desativar tipo
- `POST /api/ticket-types/reserve` - Reservar ingressos (interno)
- `POST /api/ticket-types/release` - Liberar ingressos (interno)

### Reservations
- `GET /api/reservations/me` - Listar minhas reservas
- `GET /api/reservations/event/{eventId}` - Listar reservas do evento
- `GET /api/reservations/{id}` - Buscar reserva por ID
- `POST /api/reservations` - Criar reserva
- `POST /api/reservations/{id}/cancel` - Cancelar reserva
- `POST /api/reservations/{id}/convert` - Converter em pedido (interno)

### Orders
- `GET /api/orders/me` - Listar meus pedidos
- `GET /api/orders/{id}` - Buscar pedido por ID
- `GET /api/orders/{id}/tickets` - Listar ingressos do pedido
- `POST /api/orders` - Criar pedido
- `POST /api/orders/{id}/pay` - Processar pagamento
- `POST /api/orders/{id}/refund` - Reembolsar pedido

### Issued Tickets (Ingressos Emitidos)
- `GET /api/tickets/{code}` - Buscar ingresso por código
- `POST /api/tickets/{code}/validate` - Validar ingresso (check-in)

### GraphQL
- `GET /graphql` - GraphQL Playground

## Comandos Úteis

### Build
```bash
gradle build              # Build completo
gradle build -x test      # Build sem testes
```

### Testes
```bash
gradle test               # Todos os testes
gradle :services:users:test   # Testes de um módulo
```

### Docker
```bash
docker-compose up --build           # Produção
docker-compose down                 # Parar serviços
docker-compose logs -f bff          # Ver logs do BFF
```

## Arquitetura

```
┌─────────────┐     ┌─────────────┐     ┌─────────────────────────────┐
│             │     │             │     │         Services            │
│    UI       │────▶│    BFF      │────▶│  Users | Events | Partners  │
│  (Next.js)  │     │  (Gateway)  │     │  Tickets | Orders | ...     │
│             │     │             │     │                             │
└─────────────┘     └─────────────┘     └─────────────────────────────┘
   :3000              :8080                    :8081-8089
```

- **UI**: Frontend Next.js com shadcn/ui, tema dark/light
- **BFF**: Gateway que agrega e roteia requisições
- **Services**: Microserviços independentes com Vertical Slice Architecture

Consulte `context/adrs/` para decisões arquiteturais detalhadas.

## Criando Novos Serviços

1. Criar pasta em `services/<nome-servico>/`
2. Adicionar `build.gradle.kts` baseado no template existente
3. Registrar no `settings.gradle.kts`
4. Criar cliente no BFF em `bff/clients/`
5. Adicionar rotas no BFF em `bff/routes/`
6. Atualizar `docker-compose.yml`
