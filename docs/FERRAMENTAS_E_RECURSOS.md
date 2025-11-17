# Ferramentas e Recursos do Backend

Este documento detalha os principais componentes de backend da aplicação "Notas", suas responsabilidades e capacidades.

---

## 1. O Cérebro da Aplicação: `GerenciadorDeDados.java`

O `GerenciadorDeDados` é a classe central da aplicação. Ele atua como uma **fachada (Facade)** para toda a lógica de negócio e acesso aos dados. Qualquer interface de usuário (GUI ou outra) deve interagir primariamente com esta classe.

### Responsabilidades:
- **Carregamento Inicial:** Carrega todos os dados dos arquivos JSON para a memória na inicialização.
- **Acesso aos Dados:** Fornece métodos seguros para consultar os dados carregados.
- **Modificação de Dados:** Permite a alteração de dados (como notas) na memória.
- **Persistência:** Orquestra o salvamento das alterações de volta para os arquivos JSON.
- **Geração de Relatórios:** Utiliza os dados em memória para gerar relatórios consolidados.

### Métodos Principais (API Interna):
- `getTurmas()`: Retorna a lista completa de turmas.
- `buscarTurmaPorCodigo(String codigo)`: Encontra uma turma específica.
- `buscarAlunoPorRa(String ra)`: Encontra um aluno específico em qualquer turma.
- `adicionarOuAtualizarNota(...)`: Adiciona ou edita uma nota de um aluno na memória.
- `salvarAlteracoesNotas()`: Salva **todas** as alterações de notas de volta para os arquivos `.json`.
- `gerarRelatorioFinalTurma(...)`: Gera um relatório de fim de ano para uma turma/disciplina.

---

## 2. Lógica de Domínio e Cálculos

A lógica de negócio está encapsulada diretamente nas classes de domínio, seguindo os princípios da Orientação a Objetos.

### `Aluno.java`
A classe `Aluno` não é apenas um contêiner de dados; ela "sabe" realizar operações relacionadas a si mesma.
- `calcularMediaTrimestral(disciplina, trimestre)`: Calcula a média de um aluno em uma disciplina para um trimestre específico.
- `calcularMediaFinalAnual(disciplina)`: Calcula a média final do aluno na disciplina, usando as médias dos três trimestres.
- `getSituacaoFinal(disciplina)`: Retorna a situação do aluno (`APROVADO`, `RECUPERACAO_FINAL`, `REPROVADO`) com base na sua média final.

### `Situacao.java`
Um `enum` que define os possíveis status de um aluno, evitando o uso de "magic strings" e tornando o código mais seguro e legível.

---

## 3. Ferramenta de Importação Automatizada

Para facilitar a entrada de dados no sistema, foi criada uma ferramenta de importação desacoplada da aplicação principal.

### `FerramentaImportacao.java`
- **Ponto de Entrada:** É a classe que deve ser executada para iniciar o processo de importação.
- **Processador em Lote:** Ela varre a pasta `data/importar/` em busca de arquivos `.csv`.
- **Extração de Metadados:** A ferramenta lê o nome do arquivo (que deve seguir o padrão `{turma}_{disciplina}_T{trimestre}.csv`) para extrair automaticamente o código da turma, da disciplina e o trimestre.
- **Orquestração:** Para cada arquivo, ela orquestra a leitura do JSON de alunos, a criação do mapa de consulta (Nome -> RA) e chama o `ImportadorDeNotas` para fazer o trabalho pesado.
- **Arquivamento:** Move os arquivos `.csv` processados para a pasta `data/importar/processados/` para evitar reprocessamento.

### `dto/ImportadorDeNotas.java`
- **O "Trabalhador":** Contém a lógica para ler os arquivos `.csv` linha por linha.
- **Cruzamento de Dados:** Recebe o mapa (Nome -> RA) e o utiliza para encontrar o RA de cada aluno.
- **Normalização:** Possui o método `normalizarNome()`, que remove acentos e padroniza maiúsculas/espaços, tornando a correspondência de nomes muito mais robusta.
- **Geração do JSON:** Cria os objetos `NotaDTO` e os serializa para o arquivo `.json` final na pasta `data/notas_json/`.
