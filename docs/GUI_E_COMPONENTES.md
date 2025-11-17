# Interface Gráfica (GUI) e Componentes

A interface gráfica da aplicação foi desenvolvida utilizando **JavaFX** para fornecer uma experiência de usuário rica e interativa para o gerenciamento de notas. A GUI é projetada para ser modular, com componentes reutilizáveis que se comunicam com o `GerenciadorDeDados` no backend.

O visual da aplicação é padronizado através de uma folha de estilos (`resources/styles.css`), garantindo uma aparência consistente, moderna e profissional em todos os componentes, com uma paleta de cores coesa e fontes legíveis.

---

## Estrutura Principal (`AppGUI.java`)

A classe `AppGUI.java` é o ponto de entrada da interface gráfica. Ela é responsável por:

- Inicializar o `GerenciadorDeDados` para carregar todas as informações do sistema.
- Montar o layout principal da aplicação usando um `BorderPane`, que é dividido em:
  - **Topo (Top):** Um cabeçalho com o logo, o título da aplicação e um botão para acessar o **Dashboard**.
  - **Esquerda (Left):** O painel com a lista de turmas (`TurmaListPane`).
  - **Centro (Center):** A área de conteúdo principal, que exibe os detalhes da turma selecionada (`TurmaDetailsPane`).

---

## Componentes da UI

### 1. `TurmaListPane.java`

Este componente é responsável por exibir a lista de turmas carregadas pelo `GerenciadorDeDados`.

- **Funcionalidade:** Apresenta uma `TableView` com as colunas "Nome da Turma" e "Código".
- **Interação:** Ao selecionar uma turma na tabela, um evento é disparado, fazendo com que o `AppGUI` atualize a área central da tela para exibir os detalhes da turma selecionada.

### 2. `TurmaDetailsPane.java`

Este é o componente central e mais complexo da aplicação. Ele é exibido quando uma turma é selecionada e organiza as informações em abas (`TabPane`), onde cada aba representa uma disciplina daquela turma.

**Funcionalidades por Aba:**

- **Tabela de Notas (`TableView`):** Exibe uma lista de todos os alunos da turma com suas respectivas notas para aquela disciplina.
- **Edição de Notas:** As células de notas (N1, N2, N3 e REC) são editáveis. O usuário pode dar um duplo-clique, inserir um novo valor e pressionar Enter para confirmar.
- **Cálculo Automático:** Após a edição de uma nota, a média final e a situação do aluno são recalculadas e atualizadas automaticamente na interface.
- **Botão "Salvar Alterações":** Persiste todas as modificações feitas nas notas nos arquivos JSON correspondentes, chamando o método `gerenciador.salvarAlteracoesNotas()`.
- **Botão "Gerar Relatório":** Gera e salva um relatório da turma em formato **Markdown (`.md`)** na pasta `data/`. O relatório utiliza um template (`resources/relatorio_template.md`) para manter um formato padrão e profissional.
- **Botão "Exportar para PDF":** Abre uma janela para salvar um relatório em formato PDF, também baseado no template Markdown.
- **Botão "Análise da Turma":** Abre uma janela com um gráfico de barras que mostra a distribuição de alunos por situação (Aprovado, Reprovado, Recuperação) para a disciplina selecionada.
- **Lógica Condicional:** O painel é inteligente e exibe as colunas de recuperação (`T1-REC`, `T2-REC`, `T3-REC`) apenas para as turmas de 8º e 9º ano.
- **Estilização de Status:** A coluna "Situação" utiliza cores (vermelho para reprovado, verde para aprovado) para facilitar a identificação visual do desempenho dos alunos.

### 3. `AlunoNotaViewModel.java`

Esta classe não é um componente visual, mas é fundamental para o funcionamento da `TurmaDetailsPane`. Ela atua como um **ViewModel**, servindo de ponte entre o modelo de dados do backend (`Aluno`, `Nota`, `Turma`) e a `TableView` da interface.

- **Propriedades JavaFX:** Utiliza `StringProperty` e `DoubleProperty` para permitir que a `TableView` observe e reaja automaticamente a mudanças nos dados (por exemplo, quando uma nota é editada e a média precisa ser recalculada).
- **Encapsulamento:** Encapsula a lógica de buscar as notas de um aluno para uma disciplina específica e de recalcular os totais.

### 4. `AlunoNotesPane.java`

Este componente foi projetado para exibir uma visão completa de um único aluno.

- **Funcionalidade:** Mostra todas as notas de um aluno, agrupadas por disciplina em painéis expansíveis (`TitledPane`).
- **Visão Geral:** Para cada disciplina, ele exibe o nome, a média final e a situação do aluno, permitindo uma análise rápida do seu desempenho geral.

### 5. `DashboardPane.java`

Este é um painel de alto nível que fornece uma visão analítica e agregada de todos os dados históricos contidos no banco de dados H2.

- **Propósito:** Oferecer insights sobre o desempenho geral das turmas e alunos, independentemente da visualização de uma única turma.
- **Fonte de Dados:** Conecta-se diretamente ao banco de dados através do `DashboardService` para buscar e processar os dados.
- **Componentes Visuais:**
  - **Cartões de Métricas (KPIs):** Exibem números chave, como "Total de Turmas", "Total de Alunos" e a "Taxa de Aprovação Geral".
  - **Gráfico de Pizza:** Mostra a distribuição percentual de alunos entre as diferentes turmas.
  - **Gráfico de Barras Empilhadas:** Compara o desempenho (Aprovados, Reprovados, Recuperação) entre as turmas, permitindo uma análise comparativa direta.
- **Lógica de Calendário:** O cálculo das estatísticas leva em consideração o calendário letivo, diferenciando disciplinas anuais de modulares para apresentar uma visão precisa do desempenho em qualquer ponto do ano.

---

## Fluxo de Interação

1.  O usuário inicia a `AppGUI`.
2.  O `GerenciadorDeDados` carrega todas as turmas, alunos e notas dos arquivos JSON.
3.  O `TurmaListPane` exibe a lista de turmas.
4.  O usuário pode clicar no botão **"Dashboard"** para ver a análise geral ou selecionar uma turma na lista.
5.  Ao selecionar uma turma, o `TurmaDetailsPane` é exibido.
6.  Dentro do painel da turma, o usuário pode visualizar/editar notas, salvar alterações, gerar relatórios ou ver a análise específica daquela disciplina.