# Arquitetura de Backup e Sincronização com Banco de Dados

Este documento descreve a estratégia de backup da aplicação "Notas", que utiliza um banco de dados H2 como um repositório secundário e estruturado dos dados principais, que residem nos arquivos JSON.

---

## 1. Visão Geral da Arquitetura

Para garantir maior robustez e flexibilidade, o projeto adota uma arquitetura de dados híbrida e desacoplada:

- **Fonte Primária da Verdade (Aplicação Principal):** Os **arquivos JSON** na pasta `data/` continuam sendo a fonte principal de dados para a aplicação do dia a dia. A aplicação principal (`Main.java` e `GerenciadorDeDados.java`) lê e escreve diretamente nesses arquivos. Isso mantém a aplicação leve, rápida e portátil.

- **Repositório Secundário (Backup e Análise):** Um **banco de dados H2** (`data/notas_backup.mv.db`) atua como um "snapshot" ou um backup estruturado dos dados. Ele não é utilizado pela aplicação principal no seu funcionamento normal.

### Vantagens desta Abordagem:
- **Desacoplamento:** A aplicação principal não depende do banco de dados, o que simplifica seu desenvolvimento e execução.
- **Robustez:** O banco de dados oferece uma forma de backup mais segura e estruturada do que simplesmente copiar arquivos.
- **Flexibilidade para Análise:** O banco de dados pode ser acessado por ferramentas externas de análise de dados (BI, SQL clients) ou por um futuro "Painel Admin" para gerar relatórios complexos, sem impactar a performance da aplicação principal.

---

## 2. A Ferramenta de Sincronização: `SincronizadorBD.java`

A ponte entre os arquivos JSON e o banco de dados é uma ferramenta independente, o `SincronizadorBD`.

### Responsabilidades:
1.  **Inicialização do Banco de Dados:** Na primeira execução, cria toda a estrutura de tabelas (`turmas`, `alunos`, `disciplinas`, `notas`, etc.) no banco de dados H2.
2.  **Carregamento dos Dados:** Instancia o `GerenciadorDeDados` para carregar o estado atual de todos os arquivos JSON para a memória.
3.  **Sincronização:** Percorre os dados carregados e os insere/atualiza no banco de dados. A lógica de "inserir se não existir" (upsert) é gerenciada pelas classes **DAO (Data Access Objects)**, como `TurmaDAO`.

---

## 3. Como Usar a Ferramenta de Sincronização

Use esta ferramenta sempre que quiser criar ou atualizar o backup dos seus dados no banco de dados.

1.  **Verifique seus Dados:** Certifique-se de que seus arquivos JSON na pasta `data/` e `data/notas_json/` estão corretos e atualizados.
2.  **Execute o Sincronizador:**
    - No seu IDE (IntelliJ IDEA), abra o arquivo `SincronizadorBD.java`.
    - **Execute o método `main`** desta classe.

### O que Acontece a Seguir?
- **No Console:** Você verá mensagens de log indicando o início da sincronização, o carregamento dos dados dos JSONs e o número de registros sincronizados para cada entidade (turmas, alunos, etc.).
- **No Sistema de Arquivos:** Se ele não existir, um novo arquivo chamado `notas_backup.mv.db` será criado na pasta `data/`. Este é o seu banco de dados. A cada execução do `SincronizadorBD`, os dados neste arquivo serão atualizados para refletir o estado atual dos seus arquivos JSON.
