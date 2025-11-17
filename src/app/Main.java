package app;

import gui.AppGUI;
import tools.FerramentaImportacao;
import tools.SincronizadorBD;
import tools.RestauradorBD;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean sair = false;

        while (!sair) {
            System.out.println("\n--- Sistema de Gerenciamento de Notas ---");
            System.out.println("Escolha uma opção para iniciar:");
            System.out.println("  [1] Iniciar Interface Gráfica (GUI)");
            System.out.println("  [2] Executar Ferramenta de Importação de Arquivos");
            System.out.println("  [3] Sincronizar Dados com o Banco de Dados (Arquivo Histórico)");
            System.out.println("  [4] Restaurar Dados do Banco de Dados");
            System.out.println("  [0] Sair");

            System.out.print("\nDigite sua escolha: ");
            String escolha = scanner.nextLine();

            switch (escolha) {
                case "1":
                    System.out.println("\nIniciando a Interface Gráfica...");
                    AppGUI.main(args);
                    break;
                case "2":
                    System.out.println("\nIniciando a Ferramenta de Importação...");
                    FerramentaImportacao.main(args);
                    break;
                case "3":
                    System.out.println("\nIniciando o Sincronizador com o Banco de Dados...");
                    SincronizadorBD.main(args);
                    break;
                case "4":
                    System.out.println("\nIniciando a Ferramenta de Restauração...");
                    RestauradorBD.main(args);
                    break;
                case "0":
                    sair = true;
                    System.out.println("\nEncerrando o sistema.");
                    break;
                default:
                    System.out.println("\nOpção inválida. Tente novamente.");
                    break;
            }
        }

        scanner.close();
    }
}
