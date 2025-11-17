# Notas - Sistema de Gerenciamento de Notas

## Visão Geral

**Notas** é uma aplicação Java de desktop para auxiliar professores no gerenciamento de notas de alunos em diversas turmas e disciplinas. O sistema foi projetado para carregar dados de arquivos JSON e CSV, centralizar a lógica de negócio e, futuramente, ser a base para uma interface gráfica (GUI).

---

## Estrutura do Projeto

```
notas_java/
├── data/
│   ├── importar/         # (1) Coloque os arquivos .csv para importação aqui
│   │   └── processados/  # Arquivos .csv são movidos para cá após o processamento
│   ├── notas_json/       # Arquivos .json de notas gerados pela ferramenta
│   ├── 297513.json       # Exemplo de arquivo JSON com a lista de alunos de uma turma
│   └── turmas-com-disciplinas.json # Arquivo principal com a estrutura de turmas
├── docs/
│   └── README.md         # Esta documentação
├── lib/
│   └── gson-2.10.1.jar   # Biblioteca externa para manipulação de JSON
└── src/
    ├── dto/              # Data Transfer Objects - Classes que mapeiam os dados dos arquivos
    │   ├── AlunoDTO.java
    │   ├── NotaDTO.java
    │   ├── TurmaDTO.java
    │   └── ImportadorDeNotas.java
    ├── Aluno.java        # Classes de domínio (lógica de negócio)
    ├── Disciplina.java
    ├── Nota.java
    ├── Turma.java
    ├── FerramentaImportacao.java # Ponto de entrada para a ferramenta de importação
    ├── GerenciadorDeDados.java   # Cérebro da aplicação, centraliza toda a lógica
    └── Main.java               # Ponto de entrada da aplicação principal
```

---

## Funcionalidades

- **Carregamento de Dados:** Carrega todas as informações de turmas, disciplinas, alunos e notas a partir de arquivos JSON.
- **Gerenciamento Centralizado:** A classe `GerenciadorDeDados` atua como uma API interna para acessar e manipular os dados.
- **Busca Eficiente:** Métodos para buscar turmas por código e alunos por RA.
- **Persistência de Dados:** Capacidade de salvar alterações (como novas notas) de volta para os arquivos JSON.
- **Ferramenta de Importação Automatizada:** Uma ferramenta poderosa para converter notas de arquivos CSV (exportados do Moodle) para o formato JSON que a aplicação utiliza.

---

## Como Usar

### 1. Ferramenta de Importação de Notas

Use esta ferramenta para gerar os arquivos JSON de notas a partir de planilhas salvas em formato `.csv`.

1.  **Nomeie seu arquivo CSV** seguindo o padrão: `{codigoTurma}_{codigoDisciplina}_T{trimestre}.csv`.
    -   **Exemplo:** `297513_TECNOLOGIAS_AVA_METODOS_DES_SIST_T1.csv`
2.  **Coloque o arquivo** (ou vários arquivos) na pasta `data/importar/`.
3.  **Execute o método `main`** da classe `FerramentaImportacao.java`.
4.  A ferramenta irá processar cada arquivo, gerar o `.json` correspondente na pasta `data/notas_json/`, e mover o `.csv` processado para `data/importar/processados/`.

### 2. Aplicação Principal

Use esta aplicação para carregar e manipular os dados.

1.  **Execute o método `main`** da classe `Main.java`.
2.  Atualmente, a classe `Main` contém um exemplo de como usar o `GerenciadorDeDados` para carregar tudo, atualizar uma nota e salvar as alterações. Este será o ponto de partida para a construção da interface gráfica.
