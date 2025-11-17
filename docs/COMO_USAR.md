# Como Usar a Aplicação "Notas"

Este guia fornece um passo a passo prático para as operações principais da aplicação.

---

## Fluxo de Trabalho Principal

O uso da aplicação é dividido em duas etapas principais:

1.  **Importação de Dados:** Usar a `FerramentaImportacao` para converter planilhas de notas (em formato `.csv`) para o formato `.json` que a aplicação entende.
2.  **Execução da Aplicação:** Rodar a aplicação `Main` que carrega todos os dados e executa a lógica de negócio.

---

## 1. Como Importar Novas Notas

Siga estes passos sempre que tiver uma nova planilha de notas (exportada do Moodle ou de outra fonte) para adicionar ao sistema.

### Passo 1: Prepare o Arquivo CSV

1.  **Exporte sua planilha** para o formato CSV (valores separados por vírgula ou tabulação). A ferramenta atual está otimizada para o formato exportado pelo Moodle (separado por tabulação).

2.  **Renomeie o arquivo `.csv`** para que ele siga um padrão rigoroso. Isso é crucial para que a ferramenta de importação automática funcione.
    
    **Padrão:** `{codigoTurma}_{codigoDisciplina}_T{trimestre}.csv`
    
    -   `{codigoTurma}`: O código numérico da turma (ex: `297513`).
    -   `{codigoDisciplina}`: O código da disciplina, usando underscores (ex: `TECNOLOGIAS_AVA_METODOS_DES_SIST`).
    -   `{trimestre}`: O número do trimestre, precedido por `_T` (ex: `_T1`, `_T2`, `_T3`).
    
    **Exemplos de Nomes de Arquivo Válidos:**
    - `297513_TECNOLOGIAS_AVA_METODOS_DES_SIST_T1.csv`
    - `294815_COMPUTACAO_T2.csv`

### Passo 2: Coloque o Arquivo na Pasta de Importação

1.  Navegue até a pasta `data/` do projeto.
2.  Dentro dela, localize a pasta `importar/`. Se ela não existir, crie-a.
3.  **Mova ou copie seu arquivo `.csv` renomeado** para dentro da pasta `data/importar/`. Você pode colocar vários arquivos de uma vez para processamento em lote.

### Passo 3: Execute a Ferramenta de Importação

1.  No seu IDE (IntelliJ IDEA), abra o arquivo `FerramentaImportacao.java`.
2.  **Execute o método `main`** desta classe (clicando no ícone de "play" ao lado do método).

### O que Acontece a Seguir?

-   **No Console:** Você verá mensagens de log indicando o progresso. A ferramenta informará qual arquivo está sendo processado, os dados que ela extraiu do nome do arquivo (turma, disciplina, trimestre) e se a importação foi bem-sucedida.
-   **Geração do JSON:** Um novo arquivo `.json` será criado na pasta `data/notas_json/`. O nome será derivado do nome do arquivo CSV (ex: `297513_TECNOLOGIAS_AVA_METODOS_DES_SIST_T1.json`).
-   **Arquivamento do CSV:** Após ser processado com sucesso, o arquivo `.csv` original será **movido** da pasta `data/importar/` para a subpasta `data/importar/processados/`. Isso garante que ele não seja importado novamente por engano.

---

## 2. Como Executar a Aplicação Principal

Esta é a aplicação que, no futuro, terá a interface gráfica. Atualmente, ela carrega todos os dados e serve como base para a lógica de negócio.

1.  No seu IDE, abra o arquivo `Main.java`.
2.  **Execute o método `main`** desta classe.

### O que Acontece a Seguir?

-   A aplicação iniciará o `GerenciadorDeDados`, que carregará **todos os dados** dos arquivos JSON (turmas, alunos e as notas que você acabou de importar).
-   O `Main.java` atualmente contém um código de exemplo que demonstra como usar o `GerenciadorDeDados` para:
    1.  Carregar os dados.
    2.  Atualizar uma nota de um aluno específico na memória.
    3.  Salvar (persistir) essa alteração de volta no arquivo JSON correspondente.
-   Este código serve como um modelo para as ações que serão executadas pelos botões e componentes da sua futura interface gráfica.
