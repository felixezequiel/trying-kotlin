# ADR-001: Arquitetura BFF com Microserviços em Monorepo

## Status

Aceito

## Data

2024-12-09

## Contexto

O projeto MyApp está sendo desenvolvido seguindo **Vertical Slice Architecture** com **Hexagonal Architecture**. À medida que o sistema cresce, precisamos:

1. **Modularizar** o sistema em múltiplos serviços independentes (~10 módulos previstos)
2. **Isolar domínios** com bancos de dados separados por serviço
3. **Expor APIs** tanto REST quanto GraphQL para diferentes clientes
4. **Simplificar o desenvolvimento** local com um único comando para subir todo o ambiente
5. **Manter produtividade** com hot reload durante o desenvolvimento

### Situação Atual
- Projeto monolítico em Kotlin com Ktor
- Estrutura atual em `users/` precisa ser migrada
- Sem orquestração de múltiplos serviços

### Forças em Jogo
- Equipe familiarizada com Kotlin e Ktor
- Necessidade de escalar para ~10 serviços
- Ambiente de desenvolvimento deve ser simples de configurar
- Cada domínio deve ser independente e deployável separadamente no futuro

## Decisão

Adotaremos uma arquitetura de **BFF (Backend for Frontend)** com **microserviços** em um **monorepo Gradle multi-project**, orquestrado via **Docker Compose** com suporte a **hot reload**.

### Arquitetura Geral

```
┌─────────────────────────────────────────────────────────────┐
│                        Clientes                              │
│                  (Web, Mobile, Third-party)                  │
└─────────────────────────┬───────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                     BFF (Port 8080)                          │
│              ┌─────────────┬─────────────┐                   │
│              │    REST     │   GraphQL   │                   │
│              └─────────────┴─────────────┘                   │
│                                                              │
│  Responsabilidades:                                          │
│  - Roteamento de requisições                                 │
│  - Autenticação/Autorização (futuro)                         │
│  - Agregação de dados (futuro)                               │
│  - Rate limiting (futuro)                                    │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP (REST)
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                      Serviços                                │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐            │
│  │ Users   │ │ Orders  │ │Products │ │  ...    │            │
│  │ :8081   │ │ :8082   │ │ :8083   │ │ :808X   │            │
│  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘            │
│       │           │           │           │                  │
│       ▼           ▼           ▼           ▼                  │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐            │
│  │   DB    │ │   DB    │ │   DB    │ │   DB    │            │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘            │
└─────────────────────────────────────────────────────────────┘
```

### Estrutura do Monorepo

```
myapp/
├── build.gradle.kts              # Configurações compartilhadas
├── settings.gradle.kts           # Declaração dos módulos
├── docker-compose.yml            # Orquestração de todos os serviços
├── docker-compose.dev.yml        # Override para desenvolvimento (hot reload)
├── gradle.properties             # Propriedades globais
│
├── bff/
│   ├── build.gradle.kts
│   ├── Dockerfile
│   ├── Application.kt
│   ├── routes/                   # Rotas REST
│   ├── graphql/                  # Schema e resolvers GraphQL
│   └── clients/                  # Clientes HTTP para serviços
│
├── services/
│   ├── users/
│   │   ├── build.gradle.kts
│   │   ├── Dockerfile
│   │   ├── domain/
│   │   ├── application/
│   │   │   ├── ports/
│   │   │   │   ├── inbound/
│   │   │   │   └── outbound/
│   │   │   └── useCases/
│   │   ├── adapters/
│   │   │   ├── inbound/
│   │   │   └── outbound/
│   │   └── infrastructure/
│   │
│   ├── orders/
│   │   └── ... (mesma estrutura)
│   │
│   └── [outros serviços]/
│
├── shared/
│   ├── build.gradle.kts
│   ├── dto/                      # DTOs compartilhados
│   ├── exceptions/               # Exceções comuns
│   └── utils/                    # Utilitários
│
└── context/
    ├── adrs/                     # Architecture Decision Records
    └── agents/                   # Documentação para agentes IA
```

### Padrão de Portas

| Serviço | Porta |
|---------|-------|
| BFF | 8080 |
| Users | 8081 |
| Orders | 8082 |
| Products | 8083 |
| ... | 808X |

### Comunicação

- **Clientes → BFF**: REST ou GraphQL
- **BFF → Serviços**: REST (HTTP interno)
- **Serviços → Serviços**: Não permitido (sempre via BFF)
- **Service Discovery**: Configuração estática via variáveis de ambiente

### Docker Compose com Hot Reload

O ambiente de desenvolvimento usará:
1. **Volume mounts** para montar código fonte nos containers
2. **Gradle continuous build** (`gradle -t classes`) para recompilar automaticamente
3. **Ktor auto-reload** para reiniciar o servidor quando classes mudam

## Alternativas Consideradas

### Alternativa 1: Múltiplos Repositórios
- **Descrição**: Cada serviço em seu próprio repositório Git
- **Prós**: Isolamento total, CI/CD independente
- **Contras**: Complexidade de versionamento, dificuldade de refatorações cross-service
- **Motivo da rejeição**: Overhead de gerenciamento para equipe atual, dificulta compartilhamento de código

### Alternativa 2: Monolito Modular
- **Descrição**: Manter tudo em um único processo com módulos internos
- **Prós**: Simplicidade de deploy, comunicação in-process
- **Contras**: Acoplamento, não escala independentemente, banco compartilhado
- **Motivo da rejeição**: Não atende requisito de isolamento de domínios com bancos separados

### Alternativa 3: Kubernetes para Desenvolvimento
- **Descrição**: Usar Minikube/Kind para orquestrar localmente
- **Prós**: Paridade com produção
- **Contras**: Complexidade, consumo de recursos, curva de aprendizado
- **Motivo da rejeição**: Overhead desnecessário para ambiente de desenvolvimento

### Alternativa 4: Script Gradle sem Docker
- **Descrição**: Subir todos os serviços via Gradle tasks
- **Prós**: Mais leve, sem dependência de Docker
- **Contras**: Gerenciamento manual de processos, sem isolamento, bancos de dados externos
- **Motivo da rejeição**: Mantido como fallback, mas Docker Compose oferece melhor experiência

## Consequências

### Positivas
- **Isolamento de domínios**: Cada serviço com seu banco e ciclo de vida
- **Escalabilidade**: Serviços podem ser escalados independentemente em produção
- **Desenvolvimento simplificado**: Um comando (`docker-compose up`) sobe tudo
- **Hot reload**: Produtividade mantida com recompilação automática
- **Flexibilidade de API**: Clientes podem usar REST ou GraphQL conforme necessidade
- **Código compartilhado**: Módulo `shared` evita duplicação de DTOs

### Negativas
- **Complexidade inicial**: Setup mais elaborado que monolito
- **Latência de rede**: Comunicação HTTP entre BFF e serviços adiciona overhead
- **Consistência eventual**: Transações distribuídas são mais complexas

### Riscos
| Risco | Mitigação |
|-------|-----------|
| Complexidade de debug distribuído | Implementar tracing (OpenTelemetry) no futuro |
| Inconsistência de dados entre serviços | Usar padrões como Saga para operações cross-service |
| Overhead de manutenção de ~10 serviços | Padronização via templates e módulo shared |

## Implementação

### Fase 1: Setup da Estrutura (Sprint atual)
1. Criar estrutura de diretórios do monorepo
2. Configurar `settings.gradle.kts` com todos os módulos
3. Criar `build.gradle.kts` raiz com configurações compartilhadas
4. Criar módulo `shared` com DTOs base

### Fase 2: BFF Básico
1. Criar módulo `bff` com Ktor
2. Implementar roteamento REST básico
3. Configurar GraphQL com KGraphQL
4. Criar clientes HTTP para comunicação com serviços

### Fase 3: Migração do Serviço Users
1. Migrar código de `users/` para `services/users/`
2. Adaptar para estrutura de módulo Gradle
3. Criar Dockerfile do serviço
4. Testar comunicação BFF → Users

### Fase 4: Docker Compose
1. Criar `docker-compose.yml` base
2. Criar `docker-compose.dev.yml` com hot reload
3. Configurar volumes e variáveis de ambiente
4. Documentar comandos de desenvolvimento

### Fase 5: Template para Novos Serviços
1. Criar script/template para gerar novos serviços
2. Documentar processo de criação de serviço
3. Atualizar AGENTS.md com novas convenções

## Referências

- [Ktor Documentation](https://ktor.io/docs/)
- [Gradle Multi-Project Builds](https://docs.gradle.org/current/userguide/multi_project_builds.html)
- [BFF Pattern - Sam Newman](https://samnewman.io/patterns/architectural/bff/)
- [Docker Compose Development](https://docs.docker.com/compose/)
- [KGraphQL](https://kgraphql.io/)
