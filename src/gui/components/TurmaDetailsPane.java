package gui.components;

import gui.viewmodel.AlunoNotaViewModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;import javafx.scene.control.Alert;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.util.converter.NumberStringConverter;
import model.Aluno;
import javafx.stage.FileChooser;
import model.Disciplina;
import model.Turma;
import service.GerenciadorDeDados;

import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * Painel que exibe os detalhes de uma turma, com abas para cada disciplina.
 */
public class TurmaDetailsPane extends VBox {

    private final GerenciadorDeDados gerenciador;
    private final Turma turma;

    public TurmaDetailsPane(Turma turma, GerenciadorDeDados gerenciador) {
        this.turma = turma;
        this.gerenciador = gerenciador;

        this.setPadding(new Insets(10));
        this.setSpacing(10);

        this.getStyleClass().add("details-pane");

        Label tituloTurma = new Label("Detalhes da Turma: " + turma.getNomeTurma());
        tituloTurma.getStyleClass().add("details-title");

        TabPane tabPaneDisciplinas = new TabPane();

        // Cria uma aba para cada disciplina da turma
        for (Disciplina disciplina : turma.getDisciplinas()) {
            Tab tab = new Tab(disciplina.getNomeDisciplina());
            tab.setClosable(false);

            // --- Tabela de Alunos e Notas (movida para cima para estar no escopo dos botões) ---
            TableView<AlunoNotaViewModel> tabelaAlunos = new TableView<>();
            tabelaAlunos.setEditable(true); // Permite a edição

            // --- Barra de Botões ---
            Button btnSalvar = new Button("Salvar Alterações");
            btnSalvar.getStyleClass().add("button-save"); // Estilo específico para salvar
            btnSalvar.setOnAction(e -> salvarAlteracoes());

            Button btnRelatorio = new Button("Gerar Relatório");
            btnRelatorio.setOnAction(e -> {
                try {
                    String caminhoArquivo = gerenciador.salvarRelatorioMarkdown(turma, disciplina);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Relatório salvo com sucesso em:\n" + caminhoArquivo);
                    alert.setHeaderText(null);
                    alert.showAndWait();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Ocorreu um erro ao salvar o relatório: " + ex.getMessage());
                    alert.setHeaderText("Falha ao Salvar");
                    alert.showAndWait();
                }
            });

            Button btnExportarPDF = new Button("Exportar para PDF");
            btnExportarPDF.setOnAction(e -> {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Salvar Relatório em PDF");
                fileChooser.setInitialFileName("Relatorio_" + turma.getCodigoTurma() + "_" + disciplina.getCodigoDisciplina() + ".pdf");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

                File file = fileChooser.showSaveDialog(getScene().getWindow());
                if (file != null) {
                    try {
                        gerenciador.gerarRelatorioPDF(turma, disciplina, file);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Relatório PDF gerado com sucesso!");
                        alert.setHeaderText(null);
                        alert.showAndWait();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Ocorreu um erro ao gerar o arquivo PDF: " + ex.getMessage());
                        alert.setHeaderText("Falha na Exportação");
                        alert.showAndWait();
                    }
                }
            });

            Button btnAnalise = new Button("Análise da Turma");
            btnAnalise.setOnAction(e -> {
                // Coleta os dados para o gráfico a partir dos ViewModels
                ObservableList<AlunoNotaViewModel> items = tabelaAlunos.getItems();
                long aprovados = items.stream().filter(vm -> vm.situacaoProperty().get().equals("APROVADO")).count();
                long reprovados = items.stream().filter(vm -> vm.situacaoProperty().get().equals("REPROVADO")).count();
                long recuperacao = items.stream().filter(vm -> vm.situacaoProperty().get().equals("RECUPERACAO_FINAL")).count();

                exibirGraficoAnalise(aprovados, reprovados, recuperacao, disciplina.getNomeDisciplina());
            });


            HBox bottomBar = new HBox(10, btnAnalise, btnRelatorio, btnExportarPDF, btnSalvar); // Adiciona espaçamento e os dois botões
            bottomBar.setAlignment(Pos.CENTER_RIGHT);
            bottomBar.setPadding(new Insets(10, 0, 0, 0));

            // Colunas de identificação
            TableColumn<AlunoNotaViewModel, String> nomeCol = new TableColumn<>("Aluno");
            nomeCol.setCellValueFactory(cellData -> cellData.getValue().nomeProperty());
            nomeCol.setPrefWidth(220);

            TableColumn<AlunoNotaViewModel, String> raCol = new TableColumn<>("RA");
            raCol.setCellValueFactory(cellData -> cellData.getValue().raProperty());
            raCol.setPrefWidth(100);

            // Colunas de Notas (T1, T2, T3)
            TableColumn<AlunoNotaViewModel, Number> n1t1Col = createEditableNotaColumn("T1-N1", "n1_t1");
            TableColumn<AlunoNotaViewModel, Number> n2t1Col = createEditableNotaColumn("T1-N2", "n2_t1");
            TableColumn<AlunoNotaViewModel, Number> n3t1Col = createEditableNotaColumn("T1-N3", "n3_t1");
            TableColumn<AlunoNotaViewModel, Number> n1t2Col = createEditableNotaColumn("T2-N1", "n1_t2");
            TableColumn<AlunoNotaViewModel, Number> n2t2Col = createEditableNotaColumn("T2-N2", "n2_t2");
            TableColumn<AlunoNotaViewModel, Number> n3t2Col = createEditableNotaColumn("T2-N3", "n3_t2");
            TableColumn<AlunoNotaViewModel, Number> n1t3Col = createEditableNotaColumn("T3-N1", "n1_t3");
            TableColumn<AlunoNotaViewModel, Number> n2t3Col = createEditableNotaColumn("T3-N2", "n2_t3"); 
            TableColumn<AlunoNotaViewModel, Number> n3t3Col = createEditableNotaColumn("T3-N3", "n3_t3");

            // Colunas de Média e Situação
            TableColumn<AlunoNotaViewModel, Number> mediaCol = new TableColumn<>("Média");
            mediaCol.setCellValueFactory(cellData -> cellData.getValue().mediaFinalProperty()); 
            mediaCol.setCellFactory(tc -> new TextFieldTableCell<>(new NumberStringConverter("0.00")));

            TableColumn<AlunoNotaViewModel, String> situacaoCol = new TableColumn<>("Situação");
            situacaoCol.setCellValueFactory(cellData -> cellData.getValue().situacaoProperty());
            // Adiciona uma CellFactory para colorir o texto do status
            situacaoCol.setCellFactory(column -> {
                return new TableCell<AlunoNotaViewModel, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setText(null);
                            getStyleClass().removeAll("status-aprovado", "status-reprovado", "status-recuperacao");
                        } else {
                            setText(item);
                            getStyleClass().removeAll("status-aprovado", "status-reprovado", "status-recuperacao");

                            if (item.equals("REPROVADO")) getStyleClass().add("status-reprovado");
                            else if (item.equals("APROVADO")) getStyleClass().add("status-aprovado");
                            else if (item.equals("RECUPERACAO_FINAL")) getStyleClass().add("status-recuperacao");
                        }
                    }
                };
            });

            tabelaAlunos.getColumns().addAll(nomeCol, raCol, n1t1Col, n2t1Col, n3t1Col, n1t2Col, n2t2Col, n3t2Col, n1t3Col, n2t3Col, n3t3Col, mediaCol, situacaoCol);

            // Adiciona colunas de recuperação APENAS para 8º e 9º ano
            boolean isTurmaElegivel = turma.getNomeTurma().contains("8º ANO") || turma.getNomeTurma().contains("9º ANO");
            if (isTurmaElegivel) {
                TableColumn<AlunoNotaViewModel, Number> recT1Col = createEditableNotaColumn("T1-REC", "rec_t1");
                TableColumn<AlunoNotaViewModel, Number> recT2Col = createEditableNotaColumn("T2-REC", "rec_t2");
                TableColumn<AlunoNotaViewModel, Number> recT3Col = createEditableNotaColumn("T3-REC", "rec_t3");
                // Adiciona as colunas de recuperação após as notas de cada trimestre
                tabelaAlunos.getColumns().add(5, recT1Col);
                tabelaAlunos.getColumns().add(9, recT2Col);
                tabelaAlunos.getColumns().add(13, recT3Col);
            }

            // Carrega os dados na tabela
            List<AlunoNotaViewModel> viewModels = turma.getAlunos().stream()
                    .map(aluno -> new AlunoNotaViewModel(aluno, disciplina, turma))
                    .collect(Collectors.toList());
            tabelaAlunos.setItems(FXCollections.observableArrayList(viewModels));

            VBox content = new VBox(10, new Label("Notas da disciplina:"), tabelaAlunos, bottomBar);
            content.setPadding(new Insets(10));
            
            tab.setContent(content);
            tabPaneDisciplinas.getTabs().add(tab);
        }

        this.getChildren().addAll(tituloTurma, tabPaneDisciplinas);
    }

    /**
     * Cria uma coluna de nota editável para a TableView.
     */
    private TableColumn<AlunoNotaViewModel, Number> createEditableNotaColumn(String title, String propertyName) {
        TableColumn<AlunoNotaViewModel, Number> col = new TableColumn<>(title);
        
        // Vincula a coluna à propriedade correta no ViewModel
        col.setCellValueFactory(cellData -> {
            try {
                // Usa reflexão para obter a propriedade dinamicamente (ex: n1_t1Property())
                return (javafx.beans.value.ObservableValue<Number>) AlunoNotaViewModel.class.getMethod(propertyName + "Property").invoke(cellData.getValue());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });

        // Usa um TextFieldTableCell para permitir a edição
        col.setCellFactory(TextFieldTableCell.forTableColumn(new NumberStringConverter()));

        // Define o que acontece quando a edição é confirmada (usuário pressiona Enter)
        col.setOnEditCommit(event -> {
            AlunoNotaViewModel viewModel = event.getRowValue();
            // Atualiza o valor na propriedade do ViewModel
            ((javafx.beans.property.DoubleProperty) event.getTableColumn().getCellObservableValue(viewModel)).set(event.getNewValue().doubleValue());
            // Recalcula a média e a situação
            viewModel.recalcular();
        });

        return col;
    }

    private void salvarAlteracoes() {
        gerenciador.salvarAlteracoesNotas();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText("Todas as alterações foram salvas com sucesso nos arquivos JSON!");
        alert.showAndWait();
    }

    /**
     * Cria e exibe uma nova janela com um gráfico de barras da situação dos alunos.
     */
    private void exibirGraficoAnalise(long aprovados, long reprovados, long recuperacao, String nomeDisciplina) {
        Stage chartStage = new Stage();
        chartStage.setTitle("Análise de Desempenho - " + nomeDisciplina);

        // Define os eixos do gráfico
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Situação");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nº de Alunos");

        // Cria o gráfico de barras
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Distribuição de Alunos por Situação Final");
        barChart.setLegendVisible(false); // Não precisa de legenda para uma única série

        // Cria a série de dados
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Aprovados", aprovados));
        series.getData().add(new XYChart.Data<>("Recuperação", recuperacao));
        series.getData().add(new XYChart.Data<>("Reprovados", reprovados));

        barChart.getData().add(series);

        // Aplica estilos aos nós do gráfico
        series.getData().get(0).getNode().setStyle("-fx-bar-fill: #26B99A;"); // Aprovados (Verde)
        series.getData().get(1).getNode().setStyle("-fx-bar-fill: #F0AD4E;"); // Recuperação (Laranja)
        series.getData().get(2).getNode().setStyle("-fx-bar-fill: #D9534F;"); // Reprovados (Vermelho)

        Scene scene = new Scene(barChart, 800, 600);
        chartStage.setScene(scene);
        chartStage.show();
    }
}