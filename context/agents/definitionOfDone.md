# DOD - Definition of Done

Este documento define as regras obrigatórias que DEVEM ser validadas antes de considerar qualquer tarefa como concluída.

## ⚠️ REGRAS CRÍTICAS

### 1. Nunca Trabalhar Sem ADR

**ANTES de qualquer implementação, DEVE existir uma ADR aprovada.**

Se o usuário solicitar uma feature/mudança sem ADR documentada:
1. **PARAR** a implementação
2. **PERGUNTAR**: "Esta alteração não possui uma ADR documentada. Deseja que eu crie a ADR antes de prosseguir?"
3. **CRIAR** a ADR usando o template em `context/adrs/000-template.md`
4. **AGUARDAR** aprovação do usuário
5. **SOMENTE ENTÃO** iniciar a implementação

**Exceções (não requerem ADR):**
- Correção de bugs simples
- Refatorações que não alteram comportamento
- Atualizações de documentação
- Ajustes de estilo/formatação

### 2. Validação de Testes

**APÓS qualquer alteração de código:**

```bash
gradle test
```

- [ ] Nenhum teste existente foi quebrado
- [ ] Todo código novo possui testes unitários
- [ ] Testes estão na estrutura espelhada em `tests/`
- [ ] Nomenclatura segue o padrão: `NomeClasseTest.kt`

### 3. Conformidade com ADR

- [ ] Todos os requisitos da seção "Decisão" foram implementados
- [ ] Estrutura segue o padrão definido na ADR
- [ ] Nenhum desvio da arquitetura documentada
- [ ] Se houver desvio necessário, a ADR deve ser atualizada ANTES

### 4. Atualização de Documentação

Documentação DEVE ser atualizada quando:
- Novos padrões são descobertos
- Decisões arquiteturais mudam
- Novos comandos ou processos são criados
- Erros ou omissões são encontrados

---

## Checklist Completo

### Pré-Implementação
- [ ] ADR existe e está aprovada (ou foi criada)
- [ ] Requisitos estão claros na ADR

### Durante Implementação
- [ ] Código segue `context/agents/folderStructure.md`
- [ ] Nomenclatura segue `context/agents/codingConventions.md`
- [ ] Princípios SOLID estão sendo seguidos
- [ ] Hexagonal Architecture está sendo respeitada

### Pós-Implementação
- [ ] `gradle test` passa sem erros
- [ ] Testes unitários foram criados para código novo
- [ ] Nenhum teste existente foi quebrado
- [ ] ADR foi 100% atendida
- [ ] Documentação foi atualizada (se necessário)
- [ ] `gradle build` compila sem erros

### Validação Final
```bash
gradle clean build
gradle test
```

---

## Fluxo de Trabalho

```
┌─────────────────────────────────────────────────────────────┐
│                    NOVA SOLICITAÇÃO                         │
└─────────────────────────────┬───────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│              EXISTE ADR PARA ESTA MUDANÇA?                  │
└─────────────────────────────┬───────────────────────────────┘
                              │
              ┌───────────────┴───────────────┐
              │                               │
              ▼                               ▼
        ┌─────────┐                     ┌─────────┐
        │   SIM   │                     │   NÃO   │
        └────┬────┘                     └────┬────┘
             │                               │
             │                               ▼
             │                  ┌─────────────────────────┐
             │                  │  SOLICITAR AO USUÁRIO   │
             │                  │  CRIAÇÃO DA ADR         │
             │                  └────────────┬────────────┘
             │                               │
             │                               ▼
             │                  ┌─────────────────────────┐
             │                  │  CRIAR ADR USANDO       │
             │                  │  000-template.md        │
             │                  └────────────┬────────────┘
             │                               │
             │                               ▼
             │                  ┌─────────────────────────┐
             │                  │  AGUARDAR APROVAÇÃO     │
             │                  └────────────┬────────────┘
             │                               │
             └───────────────┬───────────────┘
                             │
                             ▼
              ┌─────────────────────────────┐
              │      IMPLEMENTAR            │
              │  (seguindo folderStructure) │
              └──────────────┬──────────────┘
                             │
                             ▼
              ┌─────────────────────────────┐
              │   CRIAR TESTES UNITÁRIOS    │
              └──────────────┬──────────────┘
                             │
                             ▼
              ┌─────────────────────────────┐
              │      gradle test            │
              │   (DEVE PASSAR 100%)        │
              └──────────────┬──────────────┘
                             │
                             ▼
              ┌─────────────────────────────┐
              │  ATUALIZAR DOCUMENTAÇÃO     │
              │     (se necessário)         │
              └──────────────┬──────────────┘
                             │
                             ▼
              ┌─────────────────────────────┐
              │          DONE ✓             │
              └─────────────────────────────┘
```

---

## Mensagens Padrão

### Quando não há ADR:
```
⚠️ ATENÇÃO: Esta solicitação não possui uma ADR documentada.

Antes de prosseguir com a implementação, preciso criar uma ADR para documentar 
esta decisão arquitetural.

Deseja que eu crie a ADR agora? Ela será salva em `context/adrs/XXX-nome.md`
```

### Quando testes falham:
```
❌ ERRO: Testes falharam após as alterações.

Não é possível considerar esta tarefa como concluída até que:
1. Todos os testes existentes passem
2. Testes para o código novo sejam criados e passem

Executando: gradle test --info
```

### Quando documentação precisa ser atualizada:
```
📝 NOTA: Detectei que esta implementação introduz novos padrões/convenções.

Documentos que podem precisar de atualização:
- [ ] context/agents/codingConventions.md
- [ ] context/agents/folderStructure.md
- [ ] ADR relacionada

Deseja que eu atualize a documentação?
```
