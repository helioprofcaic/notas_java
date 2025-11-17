package gui;

import gui.components.DashboardPane;
import gui.components.TurmaDetailsPane; // Importa o novo componente
import gui.components.TurmaListPane;   // Importa o novo componente
import javafx.geometry.Pos;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Turma;
import service.GerenciadorDeDados;

/**
 * Classe principal para a interface gráfica (GUI) da aplicação "Notas".
 * Atua como o contêiner principal e orquestrador dos componentes de UI.
 */
public class AppGUI extends Application {

    private GerenciadorDeDados gerenciador;
    private BorderPane rootLayout; // Referência ao layout raiz para atualizar o centro
    private StackPane initialCenterPane; // Mensagem inicial do centro

    @Override
    public void start(Stage primaryStage) {
        // Inicializa o GerenciadorDeDados e carrega os dados reais
        String filePath = "D:/Local/Dev/Java/notas_java/data/turmas-com-disciplinas.json";
        this.gerenciador = new GerenciadorDeDados(filePath);

        rootLayout = new BorderPane();

        // --- Região TOP (Cabeçalho) ---
        Label headerLabel = new Label("Sistema de Gerenciamento de Notas");
        headerLabel.getStyleClass().add("header-label");

        // Cria o painel do cabeçalho
        HBox headerPane = new HBox();
        headerPane.getStyleClass().add("header-pane");

        // Tenta carregar e adicionar o logo. Se não encontrar, continua sem ele.
        try {
            // O logo deve estar na pasta 'src/resources/'
            Image logo = new Image(getClass().getResourceAsStream("/logo.png"));
            ImageView logoView = new ImageView(logo);
            logoView.setFitHeight(40);
            logoView.setPreserveRatio(true);
            headerPane.getChildren().add(logoView); // Adiciona o logo ao cabeçalho
        } catch (Exception e) {
            System.err.println("Arquivo de logo não encontrado. Verifique a pasta 'resources'.");
        }

        // Espaçador para empurrar o botão para a direita
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button btnDashboard = new Button("Dashboard");
        btnDashboard.setOnAction(e -> rootLayout.setCenter(new DashboardPane()));

        headerPane.getChildren().addAll(headerLabel, spacer, btnDashboard); // Adiciona o texto ao cabeçalho
        rootLayout.setTop(headerPane);

        // Região LEFT (Esquerda) - Usa o novo TurmaListPane
        TurmaListPane turmaListPane = new TurmaListPane(gerenciador);
        rootLayout.setLeft(turmaListPane);

        // Região CENTER (Centro) - Conteúdo Principal (inicialmente vazio ou com mensagem)
        Label initialCenterLabel = new Label("Selecione uma turma na lista à esquerda para ver os detalhes.");
        initialCenterPane = new StackPane(initialCenterLabel); // Atribui ao campo da classe
        initialCenterPane.setStyle("-fx-background-color: #F0F8FF; -fx-padding: 10px;");
        rootLayout.setCenter(initialCenterPane);

        // Adiciona um listener para a seleção da tabela no TurmaListPane
        turmaListPane.getTabelaTurmas().getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        // Quando uma turma é selecionada, cria e exibe o TurmaDetailsPane
                        TurmaDetailsPane turmaDetailsPane = new TurmaDetailsPane(newValue, gerenciador);
                        rootLayout.setCenter(turmaDetailsPane);
                    } else {
                        // Se nada estiver selecionado, volta para a mensagem inicial
                        rootLayout.setCenter(initialCenterPane);
                    }
                });

        Scene scene = new Scene(rootLayout, 1024, 768);
        // Carrega a folha de estilos CSS
        try {
            String cssPath = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("AVISO: Arquivo de estilos 'styles.css' não encontrado. A aplicação continuará com o visual padrão.");
        }

        primaryStage.setTitle("Sistema de Notas");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
