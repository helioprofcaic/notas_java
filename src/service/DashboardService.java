package service;

import dao.ConexaoBD;
import dao.DisciplinaDAO;
import dao.TurmaDAO;
import model.Aluno;
import model.Disciplina;
import model.Situacao;
import model.Turma;

import java.time.LocalDate;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Serviço para buscar dados agregados do banco de dados para o Dashboard.
 */
public class DashboardService {

    /**
     * Busca todas as turmas e seus respectivos alunos do banco de dados.
     * @return Uma lista de objetos Turma, populada com seus alunos.
     */
    public List<Turma> getDadosCompletosDoBD() {
        List<Turma> turmas = null;
        try (Connection conn = ConexaoBD.conectar()) {
            // Instancia os DAOs com a conexão
            TurmaDAO turmaDAO = new TurmaDAO(conn);
            dao.AlunoDAO alunoDAO = new dao.AlunoDAO(conn); // Adicionado
            DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn); // Adicionado

            // Busca todas as turmas
            turmas = turmaDAO.buscarTodas();

            // Para cada turma, busca seus alunos, disciplinas e notas
            for (Turma turma : turmas) {
                List<Disciplina> disciplinas = disciplinaDAO.buscarPorTurmaId(turma.getId());
                turma.getDisciplinas().addAll(disciplinas);

                List<Aluno> alunos = alunoDAO.buscarPorTurmaId(turma.getId());
                for (Aluno aluno : alunos) {
                    aluno.getNotas().addAll(alunoDAO.buscarNotasPorAlunoId(aluno.getId()));
                }
                turma.getAlunos().addAll(alunos);
            }

        } catch (SQLException e) {
            System.err.println("ERRO ao buscar dados para o dashboard: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // Retorna uma lista vazia em caso de erro
        }
        return turmas;
    }

    /**
     * Calcula as estatísticas gerais de situação dos alunos.
     * @param turmas A lista de turmas com todos os dados carregados.
     * @return Um mapa contendo a contagem de cada situação (APROVADO, REPROVADO, etc.).
     */
    public Map<Situacao, Long> getEstatisticasGeraisDeSituacao(List<Turma> turmas) {
        final int mesAtual = LocalDate.now().getMonthValue();

        return turmas.stream()
                .flatMap(turma -> turma.getAlunos().stream()
                        .flatMap(aluno -> turma.getDisciplinas().stream()
                                .filter(disciplina -> isDisciplinaFinalizada(disciplina, mesAtual)) // Filtra apenas disciplinas finalizadas
                                .map(disciplina -> aluno.getSituacaoFinal(disciplina.getNomeDisciplina(), turma))
                        )
                )
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    /**
     * Calcula as estatísticas de situação dos alunos, agrupadas por turma.
     * @param turmas A lista de turmas com todos os dados carregados.
     * @return Um mapa onde a chave é o nome da turma e o valor é outro mapa com a contagem de cada situação.
     */
    public Map<String, Map<Situacao, Long>> getEstatisticasPorTurma(List<Turma> turmas) {
        final int mesAtual = LocalDate.now().getMonthValue();

        return turmas.stream()
                .collect(Collectors.toMap(
                        Turma::getNomeTurma, // Chave: nome da turma
                turma -> turma.getAlunos().stream()
                    .flatMap(aluno -> turma.getDisciplinas().stream()
                        .filter(disciplina -> isDisciplinaFinalizada(disciplina, mesAtual)) // Filtra apenas disciplinas finalizadas
                        .map(disciplina -> aluno.getSituacaoFinal(disciplina.getNomeDisciplina(), turma))
                    ).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                ));
    }

    /**
     * Verifica se uma disciplina deve ser considerada como "finalizada" com base no mês atual.
     */
    private boolean isDisciplinaFinalizada(Disciplina disciplina, int mesAtual) {
        if ("ANUAL".equalsIgnoreCase(disciplina.getTipo())) {
            // Disciplinas anuais só são consideradas finalizadas em Dezembro.
            return mesAtual == 12;
        }
        if ("MODULAR".equalsIgnoreCase(disciplina.getTipo())) {
            // Lógica para disciplinas modulares. Assumindo 8 módulos em 12 meses,
            // cada módulo dura 1.5 meses.
            // Esta é uma simplificação. Podemos refinar isso se tivermos as datas exatas.
            // Ex: Módulo 1 (Jan-Fev), Módulo 2 (Mar-Abril), etc.
            // Vamos considerar que até Novembro (mês 11), 6 módulos já fecharam.
            // Esta lógica pode ser ajustada para ser mais precisa.
            return true; // Para o exemplo, vamos considerar todas as modulares.
                         // Uma lógica mais precisa dependeria de um mapeamento de disciplina -> mês de conclusão.
        }
        return true; // Comportamento padrão: considera a disciplina.
    }
}
