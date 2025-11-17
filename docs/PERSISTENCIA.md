# Arquitetura de Persistência de Dados

Este documento descreve como os dados da aplicação **Notas** são salvos e carregados, um processo conhecido como persistência.

---

## 1. Visão Geral da Abordagem

A persistência de dados neste projeto é baseada em **arquivos JSON**. Em vez de usar um banco de dados tradicional (como SQLite ou MySQL), o sistema lê e escreve todas as informações diretamente em arquivos de texto formatados em JSON.

Esta abordagem foi escolhida por ser:
- **Simples:** Não requer a configuração de um servidor de banco de dados.
- **Portátil:** Os dados podem ser facilmente copiados, editados manualmente e versionados (com Git, por exemplo).
- **Legível por Humanos:** O formato JSON é fácil de inspecionar e depurar.

---

## 2. O Fluxo de Dados (Ciclo de Vida)

O ciclo de vida dos dados na aplicação segue três etapas principais:

### Etapa 1: Leitura (Carregamento)
- **O que acontece?** Quando a aplicação inicia, uma instância da classe `GerenciadorDeDados` é criada.
- **Como?** O construtor do `GerenciadorDeDados` imediatamente dispara a lógica de carregamento (`carregarDadosCompletos`). Ele lê os diversos arquivos JSON da pasta `data/` e suas subpastas (`notas_json/`).
- **Resultado:** Todos os dados dos arquivos são convertidos em objetos Java (`Turma`, `Aluno`, `Nota`) e armazenados na memória RAM, dentro da lista `private List<Turma> turmas` no `GerenciadorDeDados`. A partir deste ponto, a aplicação trabalha com esses objetos em memória.

### Etapa 2: Modificação (Em Memória)
- **O que acontece?** Qualquer alteração feita pelo usuário (como editar uma nota) ocorre **apenas nos objetos em memória**.
- **Como?** Métodos como `adicionarOuAtualizarNota()` são chamados. Este método encontra o objeto `Aluno` e o objeto `Nota` corretos na memória e altera seu valor.
- **Importante:** Se a aplicação for fechada neste ponto, **as alterações serão perdidas**, pois elas ainda não foram salvas de volta nos arquivos em disco.

### Etapa 3: Escrita (Persistência)
- **O que acontece?** As alterações feitas na memória são salvas permanentemente nos arquivos JSON em disco.
- **Como?** Isso é feito chamando o método `salvarAlteracoesNotas()` no `GerenciadorDeDados`.
- **Lógica Interna:**
    1. O método percorre todos os objetos `Turma`, `Disciplina` e `Aluno` em memória.
    2. Para cada combinação, ele reconstrói a estrutura de dados esperada no JSON, convertendo os objetos `Nota` de volta para objetos `NotaDTO`.
    3. Ele usa a biblioteca **Gson** para transformar a lista de `NotaDTO` em uma string JSON formatada.
    4. Por fim, ele **sobrescreve** o arquivo JSON correspondente (ex: `data/notas_json/notas_297513_COMPUTACAO_T1.json`) com os novos dados.

---

## 3. Exemplo Prático do Ciclo

O código abaixo, presente na classe `Main`, demonstra o ciclo completo:

```java
// 1. LEITURA: O construtor do GerenciadorDeDados carrega tudo para a memória.
GerenciadorDeDados gerenciador = new GerenciadorDeDados("caminho/para/dados");

// 2. MODIFICAÇÃO: Uma nota é alterada apenas no objeto em memória.
gerenciador.adicionarOuAtualizarNota("RA_DO_ALUNO", "COMPUTAÇAO", "T1 - N1", 9.5);

// 3. ESCRITA: As alterações em memória são salvas de volta nos arquivos JSON.
gerenciador.salvarAlteracoesNotas();
```

Esta arquitetura garante que a aplicação seja rápida (pois todas as operações são feitas em memória) e que os dados possam ser salvos de forma segura quando necessário.
