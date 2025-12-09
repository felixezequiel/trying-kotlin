# Sistema de Ingressos - Visão Geral

## Objetivo

Sistema de venda de ingressos onde **parceiros** cadastram eventos e **clientes** reservam e compram ingressos.

## Stakeholders

| Ator | Descrição |
|------|-----------|
| **Customer** | Usuário que compra ingressos |
| **Partner** | Usuário que cria e gerencia eventos |
| **Admin** | Gestão geral do sistema |

> **Nota**: Um usuário pode acumular múltiplos roles (ex: ser Customer E Partner).

## Domínios e Responsabilidades

### Users
Autenticação, autorização e gestão de roles.

- Registro e login de usuários
- Gestão de roles (CUSTOMER, PARTNER, ADMIN)
- Um usuário pode ter múltiplos roles

### Partners
Perfil comercial dos parceiros que criam eventos.

- Cadastro de dados comerciais (empresa, documentos)
- Fluxo de aprovação (PENDING → APPROVED/REJECTED)
- Vinculado a um User com role PARTNER

### Events
Eventos criados pelos parceiros.

- CRUD de eventos (nome, descrição, local, datas)
- Estados: DRAFT, PUBLISHED, CANCELLED, FINISHED
- Apenas Partners aprovados podem criar eventos

### Tickets
Tipos de ingressos e controle de estoque.

- Tipos de ingresso por evento (VIP, Pista, Camarote)
- Preço e quantidade total
- Controle de quantidade disponível

### Reservations
Reservas temporárias de ingressos.

- Bloqueia ingressos para o cliente
- Pode reservar múltiplos ingressos de uma vez
- **Não expira automaticamente** - apenas cancelamento manual
- Pode ser cancelada por: Customer, Partner ou Admin

### Orders
Pedidos, pagamentos e emissão de ingressos.

- Converte reserva em pedido
- Processa pagamento (gateway mock inicialmente)
- Emite ingressos com código único

## Fluxo Principal

```
┌─────────────────────────────────────────────────────────────────┐
│                        JORNADA DO PARTNER                        │
├─────────────────────────────────────────────────────────────────┤
│  1. User se registra                                             │
│  2. User solicita ser Partner                                    │
│  3. Admin aprova → User recebe role PARTNER                      │
│  4. Partner cria perfil comercial                                │
│  5. Partner cria Event                                           │
│  6. Partner cria TicketTypes (VIP, Pista, etc)                   │
│  7. Partner publica Event                                        │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                       JORNADA DO CUSTOMER                        │
├─────────────────────────────────────────────────────────────────┤
│  1. User se registra → recebe role CUSTOMER (default)            │
│  2. Customer navega eventos publicados                           │
│  3. Customer seleciona ingressos                                 │
│  4. Customer cria Reservation (bloqueia ingressos)               │
│  5. Customer confirma Order                                      │
│  6. Sistema processa pagamento                                   │
│  7. Sistema emite ingressos com código único                     │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      FLUXO DE CANCELAMENTO                       │
├─────────────────────────────────────────────────────────────────┤
│  - Customer cancela própria reserva                              │
│  - Partner cancela reserva de seu evento                         │
│  - Admin cancela qualquer reserva                                │
│  → Ingressos são liberados de volta ao estoque                   │
└─────────────────────────────────────────────────────────────────┘
```

## Arquitetura de Serviços

```
                    ┌─────────────┐
                    │     BFF     │
                    │   :8080     │
                    └──────┬──────┘
                           │
     ┌─────────┬─────────┬─┴───────┬───────────┬───────────┐
     │         │         │         │           │           │
     ▼         ▼         ▼         ▼           ▼           ▼
┌─────────┐┌─────────┐┌─────────┐┌─────────┐┌─────────┐┌─────────┐
│  Users  ││Partners ││ Events  ││ Tickets ││Reserva- ││ Orders  │
│  :8081  ││  :8082  ││  :8083  ││  :8084  ││  tions  ││  :8086  │
│         ││         ││         ││         ││  :8085  ││         │
└─────────┘└─────────┘└─────────┘└─────────┘└─────────┘└─────────┘
```

## Regras de Negócio Críticas

| Regra | Descrição |
|-------|-----------|
| **RN-001** | Reserva bloqueia ingressos imediatamente |
| **RN-002** | Reserva só é cancelada por ação manual |
| **RN-003** | Usuário pode ter múltiplos roles |
| **RN-004** | Apenas Partner aprovado pode criar eventos |
| **RN-005** | Ingresso emitido tem código único |
| **RN-006** | Cancelamento de reserva libera estoque |

## ADRs Relacionadas

| ADR | Serviço | Descrição |
|-----|---------|-----------|
| [ADR-003](../adrs/003-users-roles-system.md) | Users | Sistema de roles multi-tenant |
| [ADR-004](../adrs/004-partners-service.md) | Partners | Gestão de parceiros |
| [ADR-005](../adrs/005-events-service.md) | Events | Gestão de eventos |
| [ADR-006](../adrs/006-tickets-service.md) | Tickets | Tipos e estoque de ingressos |
| [ADR-007](../adrs/007-reservations-service.md) | Reservations | Sistema de reservas |
| [ADR-008](../adrs/008-orders-service.md) | Orders | Pedidos e pagamentos |

## Decisões Técnicas Globais

- **Comunicação**: Serviços se comunicam apenas via BFF
- **Pagamento**: Gateway mock para testes, real posteriormente
- **Consistência**: Eventual entre serviços (Saga pattern se necessário)
- **Autenticação**: Centralizada no Users service

## Glossário

| Termo | Definição |
|-------|-----------|
| **Event** | Acontecimento com data, local e ingressos à venda |
| **TicketType** | Categoria de ingresso (VIP, Pista, etc) |
| **Reservation** | Bloqueio temporário de ingressos antes do pagamento |
| **Order** | Pedido confirmado com pagamento processado |
| **IssuedTicket** | Ingresso emitido com código único após pagamento |
