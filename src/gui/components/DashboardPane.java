package gui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.Situacao;
import model.Turma;
import service.DashboardService;

import java.util.Map;
import java.util.List;

/**
 * Painel de Dashboard que exibe métricas e gráficos a partir dos dados do banco de dados.
 */
public class DashboardPane extends VBox {

    private DashboardService dashboardService;

    public DashboardPane() {
        this.dashboardService = new DashboardService();
        this.setPadding(new Insets(20));
        this.setSpacing(20);
        this.getStyleClass().add("details-pane"); // Reutiliza o estilo

        Label titulo = new Label("Dashboard - Visão Geral do Arquivo Histórico");
        titulo.getStyleClass().add("details-title");

        // Carrega os dados do banco
        List<Turma> turmas = dashboardService.getDadosCompletosDoBD();

        // --- Layout Principal com GridPane para mais controle ---
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);

        // --- Painel de Métricas (KPIs) ---
        HBox kpiPane = createKpiPane(turmas);
        grid.add(kpiPane, 0, 0, 2, 1); // Ocupa 2 colunas na primeira linha

        // --- Painel de Gráficos ---
        PieChart pieChart = createPieChart(turmas);
        // Novo gráfico de barras empilhadas
        StackedBarChart<String, Number> stackedBarChart = createStackedBarChart(turmas);

        grid.add(pieChart, 0, 1);
        grid.add(stackedBarChart, 1, 1);

        // Configura o crescimento dos elementos no grid
        GridPane.setHgrow(pieChart, Priority.SOMETIMES);
        GridPane.setHgrow(stackedBarChart, Priority.ALWAYS);

        this.getChildren().addAll(titulo, grid);
    }

    private HBox createKpiPane(List<Turma> turmas) {
        int totalTurmas = turmas.size();
        long totalAlunos = turmas.stream().mapToLong(t -> t.getAlunos().size()).sum();
        long totalNotas = turmas.stream()
                .flatMap(t -> t.getAlunos().stream())
                .mapToLong(a -> a.getNotas().size())
                .sum();

        Map<Situacao, Long> statsGerais = dashboardService.getEstatisticasGeraisDeSituacao(turmas);
        long aprovados = statsGerais.getOrDefault(Situacao.APROVADO, 0L);
        long totalSituacoes = statsGerais.values().stream().mapToLong(Long::longValue).sum();
        double taxaAprovacao = (totalSituacoes == 0) ? 0 : ((double) aprovados / totalSituacoes) * 100;

        VBox kpiTurmas = createKpiCard("Total de Turmas", String.valueOf(totalTurmas));
        VBox kpiAlunos = createKpiCard("Total de Alunos", String.valueOf(totalAlunos));
        VBox kpiNotas = createKpiCard("Total de Notas Registradas", String.valueOf(totalNotas));
        VBox kpiAprovacao = createKpiCard("Taxa de Aprovação Geral", String.format("%.1f%%", taxaAprovacao));

        HBox hbox = new HBox(20, kpiTurmas, kpiAlunos, kpiNotas, kpiAprovacao);
        return hbox;
    }

    private VBox createKpiCard(String titulo, String valor) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: #777;");

        Label lblValor = new Label(valor);
        lblValor.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2A3F54;");

        VBox card = new VBox(5, lblTitulo, lblValor);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 5; -fx-background-radius: 5;");
        return card;
    }

    private PieChart createPieChart(List<Turma> turmas) {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Distribuição de Alunos por Turma");

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Turma turma : turmas) {
            pieChartData.add(new PieChart.Data(turma.getNomeTurma(), turma.getAlunos().size()));
        }
        pieChart.setData(pieChartData);
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(false);

        // Adiciona um tooltip para mostrar o valor ao passar o mouse
        pieChart.getData().forEach(data -> {
            String percentage = String.format("%.1f%%", (data.getPieValue() / turmas.stream().mapToLong(t -> t.getAlunos().size()).sum()) * 100);
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip(
                    String.format("%s: %d aluno(s) (%s)", data.getName(), (int) data.getPieValue(), percentage)
            );
            javafx.scene.control.Tooltip.install(data.getNode(), tooltip);
        });

        return pieChart;
    }

    private StackedBarChart<String, Number> createStackedBarChart(List<Turma> turmas) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Turmas");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Nº de Ocorrências de Status");

        StackedBarChart<String, Number> barChart = new StackedBarChart<>(xAxis, yAxis);
        barChart.setTitle("Desempenho por Turma");
        barChart.setLegendVisible(true);

        // Obtém as estatísticas do serviço
        Map<String, Map<Situacao, Long>> statsPorTurma = dashboardService.getEstatisticasPorTurma(turmas);

        XYChart.Series<String, Number> seriesAprovados = new XYChart.Series<>();
        seriesAprovados.setName("Aprovados");
        XYChart.Series<String, Number> seriesRecuperacao = new XYChart.Series<>();
        seriesRecuperacao.setName("Recuperação");
        XYChart.Series<String, Number> seriesReprovados = new XYChart.Series<>();
        seriesReprovados.setName("Reprovados");

        statsPorTurma.forEach((nomeTurma, stats) -> {
            seriesAprovados.getData().add(new XYChart.Data<>(nomeTurma, stats.getOrDefault(Situacao.APROVADO, 0L)));
            seriesRecuperacao.getData().add(new XYChart.Data<>(nomeTurma, stats.getOrDefault(Situacao.RECUPERACAO_FINAL, 0L)));
            seriesReprovados.getData().add(new XYChart.Data<>(nomeTurma, stats.getOrDefault(Situacao.REPROVADO, 0L)));
        });

        barChart.getData().addAll(seriesAprovados, seriesRecuperacao, seriesReprovados);

        return barChart;
    }
}
