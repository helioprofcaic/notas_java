package gui.components;

import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import model.Turma;
import service.GerenciadorDeDados;

/**
 * Painel que exibe uma lista de turmas em uma tabela.
 */
public class TurmaListPane extends VBox {

    private TableView<Turma> tabelaTurmas;

    public TurmaListPane(GerenciadorDeDados gerenciador) {
        this.tabelaTurmas = new TableView<>();

        // Coluna para o Nome da Turma
        TableColumn<Turma, String> nomeCol = new TableColumn<>("Nome da Turma");
        nomeCol.setCellValueFactory(new PropertyValueFactory<>("nomeTurma"));
        nomeCol.setPrefWidth(250); // Ajuste a largura

        // Coluna para o Código da Turma
        TableColumn<Turma, String> codigoCol = new TableColumn<>("Código");
        codigoCol.setCellValueFactory(new PropertyValueFactory<>("codigoTurma"));

        tabelaTurmas.getColumns().add(nomeCol);
        tabelaTurmas.getColumns().add(codigoCol);

        // Carrega os dados do gerenciador
        tabelaTurmas.setItems(FXCollections.observableArrayList(gerenciador.getTurmas()));

        this.getChildren().add(tabelaTurmas);
    }

    public TableView<Turma> getTabelaTurmas() {
        return tabelaTurmas;
    }
}