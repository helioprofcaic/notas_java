package tools;

import model.Aluno;
import model.Disciplina;
import model.Nota;
import model.Turma; // Garante que a classe Turma correta seja importada
import dao.ConexaoBD;
import dao.AlunoDAO;
import dao.TurmaDAO;
import dao.DisciplinaDAO;
import service.GerenciadorDeDados;
// import GerenciadorDeDados; // REMOVIDO: GerenciadorDeDados está no pacote padrão, não precisa de import
import java.sql.Connection;
import java.sql.SQLException;

import java.util.List;

/**
 * Classe principal para a ferramenta de sincronização.
 * Lê todos os dados dos arquivos JSON e os persiste no banco de dados H2.
 */
public class SincronizadorBD {

    public static void main(String[] args) {
        System.out.println("--- INICIANDO SINCRONIZAÇÃO PARA O BANCO DE DADOS ---");

        // 1. Garante que as tabelas do banco de dados existam.
        ConexaoBD.criarTabelasSeNaoExistirem();

        // 2. Carrega todos os dados dos arquivos JSON para a memória.
        System.out.println("\nCarregando dados dos arquivos JSON...");
        String filePath = "D:/Local/Dev/Java/notas_java/data/turmas-com-disciplinas.json";
        GerenciadorDeDados gerenciador = new GerenciadorDeDados(filePath); // Agora deve resolver
        List<Turma> turmas = gerenciador.getTurmas(); // Aqui o erro deve ser resolvido
        System.out.println("Dados carregados com sucesso: " + turmas.size() + " turmas encontradas.");

        // 3. Abre uma única conexão e inicia uma transação
        try (Connection conn = ConexaoBD.conectar()) {
            // Desativa o auto-commit para controlar a transação manualmente
            conn.setAutoCommit(false);

            // 4. Instancia os DAOs, passando a mesma conexão para todos
            TurmaDAO turmaDAO = new TurmaDAO(conn);
            DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);
            AlunoDAO alunoDAO = new AlunoDAO(conn);

            // 5. Limpa os dados antigos
            System.out.println("\nLimpando dados antigos do banco de dados...");
            turmaDAO.limparTabelas();
            System.out.println("Dados antigos removidos.");

            // 6. Itera sobre as turmas e salva tudo em lote
            System.out.println("\nSincronizando novos dados...");
            int totalAlunos = 0;
            int totalNotas = 0;
            for (Turma turma : turmas) {
                turmaDAO.inserir(turma); // O ID será definido no objeto turma
                if (turma.getId() != 0) { // Garante que a turma foi inserida
                    disciplinaDAO.inserirEmLote(turma.getDisciplinas(), turma.getId());
                    alunoDAO.adicionarAlunosParaLote(turma.getAlunos(), turma.getId());
                    totalAlunos += turma.getAlunos().size();
                }
            }

            // 7. Executa todos os lotes e commita a transação
            System.out.println("Finalizando inserções no banco de dados...");
            disciplinaDAO.executarLote();
            // Executa o lote de alunos e depois usa os IDs retornados para as notas
            alunoDAO.executarLoteDeAlunos();
            totalNotas = alunoDAO.adicionarNotasParaLote(turmas);
            alunoDAO.executarLoteDeNotas();
            alunoDAO.fecharStatements();
            
            conn.commit();

            System.out.println(turmas.size() + " turmas, " + totalAlunos + " alunos e " + totalNotas + " notas foram sincronizados.");

        } catch (SQLException e) {
            System.err.println("ERRO CRÍTICO durante a sincronização com o banco de dados: " + e.getMessage());
            e.printStackTrace();
            // Em um cenário real, aqui ocorreria um rollback da transação.
            // O try-with-resources já fecha a conexão, descartando a transação.
        }

        System.out.println("\n--- SINCRONIZAÇÃO CONCLUÍDA ---");
    }
}
