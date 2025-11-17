# Automação com Scripts PowerShell

Para facilitar a entrada de dados e a manutenção da integridade do sistema, foram desenvolvidos scripts em PowerShell. Eles atuam como ferramentas auxiliares que automatizam tarefas repetitivas, como a digitação de notas e a sincronização de listas de alunos.

Todos os scripts são interativos e utilizam menus para guiar o usuário, minimizando a chance de erros.

---

## 1. `AtualizarNotas.ps1`

Este é o principal script para a entrada de dados em lote. Ele é projetado para lidar com a atualização de turmas no início de um período, quando a lista de alunos pode ter mudado.

### Propósito

Criar ou atualizar os arquivos de notas a partir de um bloco de texto copiado de uma plataforma externa (ex: SGE).

### Funcionalidades

- **Menus Interativos:** Permite selecionar a Turma, a Disciplina e o Trimestre através de menus numerados, evitando erros de digitação.
- **Extração de Dados com Regex:** Utiliza uma expressão regular (Regex) para interpretar textos complexos, onde os dados de um aluno (nome, RA, notas) estão distribuídos em várias linhas.
- **Fonte da Verdade:** Trata a lista de alunos colada como a **fonte da verdade atual**.
- **Atualização da Lista de Alunos:** Antes de salvar as notas, o script **sobrescreve o arquivo principal de alunos da turma** (ex: `data/297135.json`), garantindo que a aplicação Java sempre tenha a lista de alunos mais recente.
- **Backup Automático:** Cria um backup com data e hora do arquivo de notas e do arquivo de alunos antes de qualquer modificação, salvando-os na pasta `data/backup/`.
- **Sincronização Opcional:** Após salvar, pergunta se o usuário deseja sincronizar os outros arquivos de notas daquela turma, removendo "alunos fantasmas" (alunos que saíram da turma) para manter a consistência em todo o sistema.

### Como Usar

1.  Execute `./AtualizarNotas.ps1` em um terminal PowerShell.
2.  Selecione a turma, disciplina e trimestre desejados.
3.  Cole o bloco de texto com os dados dos alunos.
4.  Digite `FIM` em uma nova linha e pressione Enter.
5.  Responda à pergunta sobre a sincronização dos outros arquivos.

---

## 2. `AtualizarRecuperacao.ps1`

Este script é uma ferramenta focada, projetada para uma tarefa específica e delicada.

### Propósito

Atualizar **apenas** o campo de nota de recuperação (`recuperacao`) dos alunos em um arquivo de notas já existente, sem alterar as notas N1, N2 ou N3.

### Funcionalidades

- **Menus Interativos:** Permite selecionar o contexto (turma, disciplina, trimestre) de forma segura.
- **Atualização Cirúrgica:** Lê o arquivo JSON existente, encontra os alunos pelo RA e atualiza somente o campo `recuperacao`.
- **Segurança:** O script não cria novos arquivos nem adiciona novos alunos, apenas modifica registros existentes.

---

## 3. `SincronizarAlunos.ps1`

Esta é uma ferramenta de manutenção poderosa para garantir a consistência dos dados em toda a aplicação.

### Propósito

Resolver o problema de "alunos fantasmas" em todos os arquivos de uma turma, usando um arquivo de notas confiável como base.

### Funcionalidades

- **Seleção de Fonte Mestra:** Permite que o usuário escolha um arquivo de notas (`notas_*.json`) que ele considera como a lista de alunos mais atualizada (a "lista mestra").
- **Sincronização Completa:** Com base na lista mestra, o script:
  1.  **Sincroniza a lista principal de alunos** (ex: `data/297135.json`).
  2.  **Sincroniza todos os outros arquivos de notas** daquela turma, removendo os alunos que não constam na lista mestra.
- **Backup Abrangente:** Cria um backup de cada arquivo que é modificado durante o processo.