package gui.components;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import model.Aluno;
import model.Nota;
import model.Turma;
import model.Situacao;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Componente de UI que exibe todas as notas de um aluno, agrupadas por disciplina.
 * Também mostra a média final e situação por disciplina.
 */
public class AlunoNotesPane extends VBox {

    private Aluno aluno;
    private Turma turma;

    public AlunoNotesPane(Aluno aluno, Turma turma) {
        this.aluno = aluno;
        this.turma = turma;
        this.setSpacing(10);

        if (aluno == null) {
            this.getChildren().add(new Label("Nenhum aluno selecionado."));
            return;
        }

        this.getChildren().add(new Label("Notas de: " + aluno.getNome() + " (RA: " + aluno.getRa() + ")"));


        // Agrupa as notas do aluno por disciplina
        Map<String, List<Nota>> notasPorDisciplina = aluno.getNotas().stream()
                .collect(Collectors.groupingBy(Nota::getDisciplina));

        if (notasPorDisciplina.isEmpty()) {
            this.getChildren().add(new Label("Nenhuma nota encontrada para este aluno."));
        } else {
            for (Map.Entry<String, List<Nota>> entry : notasPorDisciplina.entrySet()) {
                String nomeDisciplina = entry.getKey();
                List<Nota> notasDaDisciplina = entry.getValue();

                // Calcula média final e situação para a disciplina
                double mediaFinal = aluno.calcularMediaFinalAnual(nomeDisciplina, turma);
                Situacao situacao = aluno.getSituacaoFinal(nomeDisciplina, turma);

                // Cria um TitledPane para cada disciplina
                TitledPane disciplinaPane = new TitledPane();
                disciplinaPane.setText(String.format("%s (Média Final: %.2f - Situação: %s)",
                        nomeDisciplina, mediaFinal, situacao));
                disciplinaPane.setCollapsible(true); // Pode ser expandido/colapsado
                disciplinaPane.setExpanded(false); // Começa colapsado

                // Tabela para as notas da disciplina
                TableView<Nota> tabelaNotas = new TableView<>();
                tabelaNotas.setPrefHeight(notasDaDisciplina.size() * 25 + 25); // Ajusta altura da tabela

                TableColumn<Nota, String> colDescricao = new TableColumn<>("Descrição");
                colDescricao.setCellValueFactory(new PropertyValueFactory<>("descricao"));
                colDescricao.setPrefWidth(150);

                TableColumn<Nota, Double> colValor = new TableColumn<>("Valor");
                colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
                colValor.setPrefWidth(80);

                tabelaNotas.getColumns().addAll(colDescricao, colValor);
                tabelaNotas.setItems(FXCollections.observableArrayList(notasDaDisciplina));

                disciplinaPane.setContent(tabelaNotas);
                this.getChildren().add(disciplinaPane);
            }
        }
    }
}
