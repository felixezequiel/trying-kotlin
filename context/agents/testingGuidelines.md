# Guia de Testes

Este documento define as diretrizes para criação e manutenção de testes no projeto.

## Localização

- Testes devem estar em `tests/` espelhando a estrutura do código fonte
- Nomenclatura: `NomeClasseTest.kt`
- Package: Mesmo package do código testado

## Comandos

| Comando | Descrição |
|---------|-----------|
| `gradle test` | Executar todos os testes |
| `gradle test --tests "NomeClasseTest"` | Teste específico |
| `gradle test --info` | Com output detalhado |

## Padrões de Teste

- Use `@Test` do JUnit 5
- Use `runTest` do Kotlin Coroutines para testes assíncronos
- Use `@BeforeEach` para setup
- Nomes de testes em português com backticks

## Exemplo de Teste

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
        // Arrange
        val name = "João"
        val email = "joao@email.com"
        
        // Act
        val result = userUseCase.registerUser(name, email)
        
        // Assert
        assertNotNull(result)
    }
}
```

## Estrutura AAA (Arrange-Act-Assert)

1. **Arrange**: Configure o cenário de teste
2. **Act**: Execute a ação sendo testada
3. **Assert**: Verifique o resultado esperado

## Regras Obrigatórias

- [ ] Todo código novo DEVE ter testes correspondentes
- [ ] Nenhum teste existente pode ser quebrado
- [ ] Testes devem estar na estrutura espelhada em `tests/`
- [ ] Nomenclatura: `NomeClasseTest.kt`
- [ ] Testes devem ser independentes entre si
- [ ] Não usar dados de produção em testes
