# ADR-011: Vinculação Inteligente de User ao Criar Partner

## Status

Aceito

## Data

2025-12-10

## Contexto

Durante testes com Playwright na UI do sistema de tickets, foram identificados problemas no fluxo de criação de Partners:

### Problemas Identificados

1. **Frontend não envia header `X-User-Id`**: A função `createPartner` em `ui/src/lib/api.ts` não inclui o header `X-User-Id` na requisição POST.

2. **Validação de CNPJ no backend**: O serviço de Partners valida o CNPJ com algoritmo de dígitos verificadores. CNPJs inválidos são rejeitados.

3. **Dependência de User existente**: O `CreatePartnerUseCase` requer um `userId` válido, mas o fluxo atual não garante que o usuário exista.

4. **UX complexa**: Exigir que o admin crie um usuário antes de criar um partner adiciona fricção desnecessária.

### Fluxo Atual (Problemático)

```
UI (Partners Page)
    │
    ├─► POST /api/partners (sem X-User-Id)
    │
BFF (PartnersRoutes.kt)
    │
    ├─► userId = headers["X-User-Id"] ?: System.currentTimeMillis()  ❌
    │
Partners Service (CreatePartnerUseCase)
    │
    ├─► Valida CNPJ
    ├─► Verifica se userId já tem Partner
    └─► Cria Partner (com userId potencialmente inválido)
```

## Decisão

Implementaremos **vinculação inteligente de usuário baseada no email** no serviço de Partners:

1. O Partner recebe o email no cadastro
2. O serviço de Partners consulta o serviço de Users (via BFF) pelo email
3. Se o usuário existe: vincula e adiciona role PARTNER
4. Se o usuário não existe: cria o usuário e adiciona role PARTNER
5. Cria o Partner vinculado ao userId

### Novo Fluxo

```
UI (Partners Page)
    │
    ├─► POST /api/partners { email, companyName, document, ... }
    │
BFF (PartnersRoutes.kt)
    │
    ├─► Repassa para Partners Service
    │
Partners Service (CreatePartnerUseCase)
    │
    ├─► Valida dados (CNPJ, email, etc.)
    ├─► Consulta IUserGateway.findOrCreateByEmail(email, name)
    │       │
    │       └─► (via HTTP) BFF → Users Service
    │               ├─► Se existe: retorna userId, adiciona role PARTNER
    │               └─► Se não existe: cria user, adiciona role PARTNER, retorna userId
    │
    ├─► Verifica se userId já tem Partner (RN-P02)
    └─► Cria Partner com userId
```

### Arquitetura (Hexagonal)

```
┌─────────────────────────────────────────────────────────────┐
│                    Partners Service                          │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                 Application Layer                    │    │
│  │  ┌─────────────────────────────────────────────┐    │    │
│  │  │           CreatePartnerUseCase              │    │    │
│  │  │  - Valida dados                             │    │    │
│  │  │  - Chama IUserGateway.findOrCreateByEmail() │    │    │
│  │  │  - Cria Partner                             │    │    │
│  │  └─────────────────────────────────────────────┘    │    │
│  │                        │                             │    │
│  │              ┌─────────┴─────────┐                   │    │
│  │              ▼                   ▼                   │    │
│  │  ┌─────────────────┐   ┌─────────────────┐          │    │
│  │  │ IPartnerRepository│ │  IUserGateway   │ (port)   │    │
│  │  └─────────────────┘   └─────────────────┘          │    │
│  └─────────────────────────────────────────────────────┘    │
│                              │                               │
│  ┌───────────────────────────┴───────────────────────────┐  │
│  │                   Adapters Layer                       │  │
│  │  ┌─────────────────────┐   ┌─────────────────────┐    │  │
│  │  │InMemoryPartnerStore │   │ UserGatewayAdapter  │    │  │
│  │  └─────────────────────┘   └──────────┬──────────┘    │  │
│  └───────────────────────────────────────┼───────────────┘  │
└──────────────────────────────────────────┼──────────────────┘
                                           │ HTTP
                                           ▼
                              ┌─────────────────────┐
                              │        BFF          │
                              │  /api/users/...     │
                              └──────────┬──────────┘
                                         │
                                         ▼
                              ┌─────────────────────┐
                              │   Users Service     │
                              └─────────────────────┘
```

### Interface do Gateway

```kotlin
// application/ports/outbound/IUserGateway.kt
interface IUserGateway {
    /**
     * Busca usuário por email. Se não existir, cria um novo.
     * Em ambos os casos, adiciona a role PARTNER ao usuário.
     * @return userId do usuário encontrado ou criado
     */
    suspend fun findOrCreateByEmail(email: String, name: String): Long
}
```

### Implementação do Adapter

```kotlin
// adapters/outbound/UserGatewayAdapter.kt
class UserGatewayAdapter(
    private val httpClient: HttpClient,
    private val bffBaseUrl: String
) : IUserGateway {
    
    override suspend fun findOrCreateByEmail(email: String, name: String): Long {
        // 1. Tenta buscar usuário por email
        val existingUser = httpClient.get("$bffBaseUrl/api/users?email=$email")
        
        val userId = if (existingUser.status == HttpStatusCode.OK) {
            existingUser.body<UserResponse>().id
        } else {
            // 2. Cria novo usuário
            val newUser = httpClient.post("$bffBaseUrl/api/users") {
                setBody(RegisterUserRequest(name = name, email = email))
            }
            newUser.body<UserResponse>().id
        }
        
        // 3. Adiciona role PARTNER
        httpClient.post("$bffBaseUrl/api/users/$userId/roles") {
            setBody(AddRoleRequest(role = "PARTNER"))
        }
        
        return userId.toLong()
    }
}
```

## Alternativas Consideradas

### Alternativa 1: Seleção manual de usuário na UI

- **Descrição**: Admin seleciona usuário existente ao criar Partner
- **Prós**: Controle explícito
- **Contras**: UX ruim; requer criar usuário antes; formulário complexo
- **Motivo da rejeição**: Adiciona fricção desnecessária ao fluxo

### Alternativa 2: Lógica de vinculação no BFF

- **Descrição**: BFF orquestra criação de User e Partner
- **Prós**: Simples de implementar
- **Contras**: Lógica de negócio no BFF; viola responsabilidade do domínio
- **Motivo da rejeição**: BFF deve ser apenas roteador, não conter regras de negócio

### Alternativa 3: Comunicação direta Partners → Users

- **Descrição**: Serviço Partners chama Users diretamente
- **Prós**: Menor latência
- **Contras**: Viola ADR-001 (comunicação sempre via BFF)
- **Motivo da rejeição**: Quebra padrão arquitetural estabelecido

## Consequências

### Positivas

- **UX simplificada**: Admin só precisa preencher dados do Partner
- **Vinculação automática**: Sistema encontra ou cria usuário automaticamente
- **Role PARTNER adicionada**: Usuário recebe permissões corretas
- **Arquitetura respeitada**: Comunicação via BFF, lógica no domínio
- **Dados consistentes**: Partner sempre vinculado a User válido

### Negativas

- **Latência adicional**: Chamadas HTTP Partners → BFF → Users
- **Dependência de disponibilidade**: Partners depende de Users estar online

### Riscos

| Risco | Mitigação |
|-------|-----------|
| Falha na comunicação com Users | Retry com backoff; transação atômica |
| Email duplicado entre Partner e User | Email é chave de vinculação, não duplica |
| Usuário criado mas Partner falha | Rollback ou compensação |

## Implementação

### Fase 1: Infraestrutura (Partners Service)

1. Criar interface `IUserGateway` em `application/ports/outbound/`
2. Criar `UserGatewayAdapter` em `adapters/outbound/`
3. Atualizar `IUnitOfWork` para incluir `userGateway`
4. Configurar HttpClient no Application.kt

### Fase 2: Lógica de Negócio

5. Atualizar `CreatePartnerUseCase`:
   - Remover dependência de `userId` no header
   - Chamar `userGateway.findOrCreateByEmail(email, companyName)`
   - Usar userId retornado para criar Partner

### Fase 3: BFF

6. Atualizar `PartnersRoutes.kt`:
   - Remover lógica de X-User-Id
   - Apenas repassar request para Partners Service

7. Adicionar endpoint em `UsersRoutes.kt`:
   - POST `/api/users/{id}/roles` (se não existir)

### Fase 4: Frontend

8. Atualizar `api.ts`: remover userId do createPartner
9. Criar `validators.ts` com validação de CNPJ
10. Atualizar `partners/page.tsx` com validação de CNPJ

## Arquivos Afetados

| Arquivo | Alteração |
|---------|-----------|
| `services/partners/application/ports/outbound/IUserGateway.kt` | Criar (nova interface) |
| `services/partners/adapters/outbound/UserGatewayAdapter.kt` | Criar (novo adapter) |
| `services/partners/application/ports/outbound/IUnitOfWork.kt` | Adicionar userGateway |
| `services/partners/application/useCases/CreatePartnerUseCase.kt` | Usar userGateway |
| `services/partners/infrastructure/web/Application.kt` | Configurar HttpClient e adapter |
| `bff/routes/PartnersRoutes.kt` | Simplificar (remover X-User-Id) |
| `bff/routes/UsersRoutes.kt` | Adicionar endpoint de roles (se necessário) |
| `ui/src/lib/api.ts` | Remover userId do createPartner |
| `ui/src/lib/validators.ts` | Criar com validação de CNPJ |
| `ui/src/app/partners/page.tsx` | Adicionar validação de CNPJ |

## Referências

- ADR-001: BFF Microservices Architecture (comunicação via BFF)
- ADR-003: Users Roles System (roles e AddRoleToUserUseCase)
- ADR-004: Partners Service
- ADR-010: Unit of Work Pattern
