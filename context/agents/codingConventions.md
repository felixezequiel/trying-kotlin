# Convenções de Código Kotlin

Este documento define os padrões de código e convenções que DEVEM ser seguidos no projeto.

## Nomenclatura

| Tipo | Padrão | Exemplo |
|------|--------|---------|
| **Classes** | PascalCase | `UserUseCase`, `UserRepositoryAdapter` |
| **Interfaces** | `I` + PascalCase | `IUserRepository`, `IUnitOfWork` |
| **Funções** | camelCase | `registerUser`, `getUserByEmail` |
| **Packages** | minúsculas | `users.application.useCases` |
| **Arquivos** | Mesmo nome da classe principal | `UserUseCase.kt` |

## Convenções Kotlin

- Use `data class` para entidades de domínio
- Prefira `suspend fun` para operações assíncronas
- Use injeção de dependência via construtor
- Interfaces devem estar em `application/ports/`
- Implementações devem estar em `adapters/`

## Exemplo de Estrutura de Classe

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

## Anti-Patterns a Evitar

1. **Dependências circulares**: Domain nunca deve importar de Application ou Infrastructure
2. **Lógica de negócio em Adapters**: Adapters apenas traduzem, não decidem
3. **Imports no meio do arquivo**: Sempre no topo
4. **Hard-coded values**: Use configuração ou constantes
5. **Funções muito longas**: Prefira funções pequenas e focadas
6. **Ignorar erros**: Sempre trate exceções adequadamente

## Boas Práticas

- **Imports organizados**: Agrupe por pacote
- **Documentação**: Documente interfaces públicas
- **Imutabilidade**: Prefira `val` sobre `var`
- **Null Safety**: Use tipos nullable com cuidado
- **Extension Functions**: Use para adicionar funcionalidade sem herança
