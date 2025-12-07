# AGENTS.md

Guia para agentes de IA trabalharem neste projeto Kotlin seguindo os padrões de **Vertical Slice Architecture** e **Hexagonal Architecture**.

## Visão Geral do Projeto

Este projeto utiliza:
- **Kotlin** com JVM 17
- **Gradle** como build tool
- **JUnit 5** para testes
- **Kotlin Coroutines** para programação assíncrona
- **Arquitetura**: Vertical Slice + Hexagonal Architecture (Ports and Adapters)

## Comandos de Setup

- **Instalar dependências**: `gradle build`
- **Compilar projeto**: `gradle build`
- **Executar testes**: `gradle test`
- **Executar testes com output detalhado**: `gradle test --info`
- **Limpar build**: `gradle clean`

## Estrutura de Pastas

**IMPORTANTE**: Sempre consulte `context/agents/folderStructure.md` antes de criar qualquer arquivo ou pasta. Este arquivo contém as regras completas de organização.

### Estrutura Atual

```
<feature-name>/
  ├── domain/                    # Entidades de domínio
  ├── application/
  │   ├── ports/
  │   │   ├── in/               # Portas de entrada
  │   │   └── out/              # Portas de saída
  │   └── useCases/              # Casos de uso
  ├── adapters/
  │   ├── in/                    # Adapters de entrada
  │   └── out/                   # Adapters de saída
  ├── infrastructure/
  │   └── persistence/           # Implementações de persistência
  └── index.kt                   # Ponto de entrada (opcional)

tests/
  └── <feature-name>/            # Espelha a estrutura acima
```

## Padrões de Código

### Nomenclatura

- **Classes**: PascalCase (`UserUseCase`, `UserRepositoryAdapter`)
- **Interfaces**: `I` + PascalCase (`IUserRepository`, `IUnitOfWork`)
- **Funções**: camelCase (`registerUser`, `getUserByEmail`)
- **Packages**: minúsculas, seguindo estrutura de pastas (`users.application.useCases`)
- **Arquivos**: Mesmo nome da classe principal

### Convenções Kotlin

- Use `data class` para entidades de domínio
- Prefira `suspend fun` para operações assíncronas
- Use injeção de dependência via construtor
- Interfaces devem estar em `application/ports/`
- Implementações devem estar em `adapters/`

### Exemplo de Estrutura de Classe

```kotlin
package users.application.useCases

import users.domain.User
import users.application.ports.out.IUnitOfWork

class UserUseCase(private val unitOfWork: IUnitOfWork) {
    suspend fun registerUser(name: String, email: String) {
        // Implementação
    }
}
```

## Instruções de Testes

### Localização

- Testes devem estar em `tests/` espelhando a estrutura do código fonte
- Nomenclatura: `NomeClasseTest.kt`
- Package: Mesmo package do código testado

### Executar Testes

- Todos os testes: `gradle test`
- Teste específico: `gradle test --tests "NomeClasseTest"`
- Com output: `gradle test --info`

### Padrões de Teste

- Use `@Test` do JUnit 5
- Use `runTest` do Kotlin Coroutines para testes assíncronos
- Use `@BeforeEach` para setup
- Nomes de testes em português com backticks: `` `deve registrar usuário com sucesso`() ``

### Exemplo de Teste

```kotlin
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserUseCaseTest {
    private lateinit var userUseCase: UserUseCase

    @BeforeEach
    fun setUp() {
        // Setup
    }

    @Test
    fun `deve registrar usuário com sucesso`() = runTest {
        // Teste
    }
}
```

## Princípios de Arquitetura

### Hexagonal Architecture

1. **Domain**: Entidades puras, sem dependências externas
2. **Application/Ports**: Interfaces que definem contratos
3. **Adapters**: Implementações dos ports
4. **Infrastructure**: Detalhes técnicos (banco, APIs externas)

### Regras de Dependência

- Domain não depende de nada
- Application depende apenas de Domain e Ports
- Adapters implementam Ports e dependem de Infrastructure
- Infrastructure é a camada mais externa

### Vertical Slice

- Cada feature é um slice vertical completo
- Cada slice contém todas as camadas necessárias
- Slices são independentes entre si

## Checklist Antes de Commitar

- [ ] Todos os testes passam: `gradle test`
- [ ] Código compila sem erros: `gradle build`
- [ ] Estrutura de pastas segue o padrão em `context/agents/folderStructure.md`
- [ ] Nomenclatura segue as convenções
- [ ] Testes foram criados/atualizados para código novo
- [ ] Imports estão organizados
- [ ] Código segue os princípios SOLID

## Comandos Úteis

- **Ver estrutura do projeto**: `tree /F` (Windows) ou `find . -type f -name "*.kt"` (Linux/Mac)
- **Limpar e rebuild**: `gradle clean build`
- **Ver dependências**: `gradle dependencies`
- **Executar apenas compilação**: `gradle compileKotlin`

## Configuração do Build

- **Kotlin**: 1.9.24
- **JVM**: 17
- **Source sets**: 
  - Main: `users/` (e outras features na raiz)
  - Test: `tests/`

## Notas Importantes

1. **Nunca misture camadas**: Domain não deve conhecer Application ou Infrastructure
2. **Sempre use interfaces**: Dependa de abstrações (Ports), não de implementações
3. **Testes são obrigatórios**: Código novo deve ter testes correspondentes
4. **Estrutura espelhada**: Testes sempre espelham a estrutura do código fonte
5. **Nomenclatura declarativa**: Nomes devem deixar claro o propósito

## Referências

- Estrutura de pastas: `context/agents/folderStructure.md`
- Padrão AGENTS.md: https://agents.md/#examples

