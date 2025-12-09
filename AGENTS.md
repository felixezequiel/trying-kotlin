# AGENTS.md

Guia para agentes de IA trabalharem neste projeto Kotlin seguindo os padrões de **Vertical Slice Architecture** e **Hexagonal Architecture**.

## Visão Geral do Projeto

| Stack | Versão/Detalhe |
|-------|----------------|
| **Kotlin** | JVM 17 |
| **Build** | Gradle |
| **Testes** | JUnit 5 |
| **Async** | Kotlin Coroutines |
| **Arquitetura** | Vertical Slice + Hexagonal |

## Comandos Essenciais

```bash
gradle build          # Compilar projeto
gradle test           # Executar testes
gradle test --info    # Testes com output detalhado
gradle clean build    # Limpar e rebuild
```

---

## Documentação Detalhada

**IMPORTANTE**: Consulte os documentos específicos em `context/agents/` para detalhes completos.

| Documento | Descrição |
|-----------|-----------|
| [`folderStructure.md`](context/agents/folderStructure.md) | Estrutura de pastas e organização de arquivos |
| [`codingConventions.md`](context/agents/codingConventions.md) | Padrões de código, nomenclatura e anti-patterns |
| [`testingGuidelines.md`](context/agents/testingGuidelines.md) | Guia de testes e padrões |
| [`architecturePrinciples.md`](context/agents/architecturePrinciples.md) | Hexagonal, Vertical Slice e SOLID |
| [`definitionOfDone.md`](context/agents/definitionOfDone.md) | DOD e regras críticas obrigatórias |

### ADRs (Architecture Decision Records)

| Documento | Descrição |
|-----------|-----------|
| [`context/adrs/`](context/adrs/) | Decisões arquiteturais |
| [`000-template.md`](context/adrs/000-template.md) | Template para novas ADRs |

---

## Regras Críticas (Resumo)

> **Detalhes completos em**: [`context/agents/definitionOfDone.md`](context/agents/definitionOfDone.md)

1. **ADR Obrigatória**: Antes de implementar features, deve existir uma ADR aprovada
2. **Testes Obrigatórios**: Todo código novo deve ter testes correspondentes
3. **Estrutura**: Sempre seguir `context/agents/folderStructure.md`
4. **Validação**: `gradle test` deve passar 100% antes de finalizar

### Exceções (não requerem ADR)
- Correção de bugs simples
- Refatorações sem mudança de comportamento
- Atualizações de documentação

---

## Checklist Rápido

```bash
# Antes de finalizar qualquer tarefa
gradle clean build && gradle test
```

- [ ] ADR existe/foi criada
- [ ] Estrutura segue `folderStructure.md`
- [ ] Nomenclatura segue `codingConventions.md`
- [ ] Testes criados e passando
- [ ] Documentação atualizada (se necessário)

---

## Referências

- [agents.md](https://agents.md/#examples) - Padrão AGENTS.md
