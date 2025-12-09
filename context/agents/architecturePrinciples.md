# Princípios de Arquitetura

Este documento define os princípios arquiteturais que guiam o projeto.

## Hexagonal Architecture (Ports and Adapters)

```
┌─────────────────────────────────────────────────────────────┐
│                      INFRASTRUCTURE                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                     ADAPTERS                         │    │
│  │  ┌─────────────────────────────────────────────┐    │    │
│  │  │                APPLICATION                   │    │    │
│  │  │  ┌─────────────────────────────────────┐    │    │    │
│  │  │  │              DOMAIN                  │    │    │    │
│  │  │  │         (Entidades puras)            │    │    │    │
│  │  │  └─────────────────────────────────────┘    │    │    │
│  │  │           (Use Cases + Ports)               │    │    │
│  │  └─────────────────────────────────────────────┘    │    │
│  │        (Implementações dos Ports)                   │    │
│  └─────────────────────────────────────────────────────┘    │
│              (Banco, APIs externas, etc)                     │
└─────────────────────────────────────────────────────────────┘
```

### Camadas

| Camada | Responsabilidade | Dependências |
|--------|------------------|--------------|
| **Domain** | Entidades puras, regras de negócio | Nenhuma |
| **Application/Ports** | Interfaces que definem contratos | Domain |
| **Adapters** | Implementações dos ports | Ports, Infrastructure |
| **Infrastructure** | Detalhes técnicos | Tudo |

### Regras de Dependência

- Domain **não depende** de nada
- Application depende apenas de **Domain e Ports**
- Adapters **implementam Ports** e dependem de Infrastructure
- Infrastructure é a **camada mais externa**

## Vertical Slice Architecture

Cada feature é um slice vertical completo e independente.

```
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│   USERS     │  │   ORDERS    │  │  PAYMENTS   │
├─────────────┤  ├─────────────┤  ├─────────────┤
│  domain/    │  │  domain/    │  │  domain/    │
│  application│  │  application│  │  application│
│  adapters/  │  │  adapters/  │  │  adapters/  │
│  infra/     │  │  infra/     │  │  infra/     │
└─────────────┘  └─────────────┘  └─────────────┘
```

### Princípios

- Cada feature é um **slice vertical completo**
- Cada slice contém **todas as camadas necessárias**
- Slices são **independentes entre si**
- Comunicação entre slices via **interfaces bem definidas**

## SOLID

| Princípio | Descrição |
|-----------|-----------|
| **S** - Single Responsibility | Uma classe, uma responsabilidade |
| **O** - Open/Closed | Aberto para extensão, fechado para modificação |
| **L** - Liskov Substitution | Subtipos devem ser substituíveis |
| **I** - Interface Segregation | Interfaces específicas > interfaces genéricas |
| **D** - Dependency Inversion | Dependa de abstrações, não de implementações |

## Notas Importantes

1. **Nunca misture camadas**: Domain não deve conhecer Application ou Infrastructure
2. **Sempre use interfaces**: Dependa de abstrações (Ports), não de implementações
3. **Testes são obrigatórios**: Código novo deve ter testes correspondentes
4. **Estrutura espelhada**: Testes sempre espelham a estrutura do código fonte
5. **Nomenclatura declarativa**: Nomes devem deixar claro o propósito
