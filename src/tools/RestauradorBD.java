package tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.ConexaoBD;
import dao.AlunoDAO;
import dao.DisciplinaDAO;
import dao.TurmaDAO;
import dto.AlunoDTO;
import dto.NotaDTO;
import model.Aluno;
import model.Disciplina;
import model.Turma;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RestauradorBD {

    public static void main(String[] args) {
        System.out.println("--- INICIANDO RESTAURAÇÃO A PARTIR DO BANCO DE DADOS ---");

        try (Connection conn = ConexaoBD.conectar()) {
            TurmaDAO turmaDAO = new TurmaDAO(conn);
            DisciplinaDAO disciplinaDAO = new DisciplinaDAO(conn);
            AlunoDAO alunoDAO = new AlunoDAO(conn);

            // 1. Carrega todos os dados do banco de dados para a memória
            System.out.println("\nCarregando dados do banco de dados...");
            List<Turma> turmas = turmaDAO.buscarTodas();
            for (Turma turma : turmas) {
                List<Disciplina> disciplinas = disciplinaDAO.buscarPorTurmaId(turma.getId());
                disciplinas.forEach(turma::adicionarDisciplina);

                List<Aluno> alunos = alunoDAO.buscarPorTurmaId(turma.getId());
                for (Aluno aluno : alunos) {
                    aluno.getNotas().addAll(alunoDAO.buscarNotasPorAlunoId(aluno.getId()));
                    turma.adicionarAluno(aluno);
                }
            }
            System.out.println("Dados carregados: " + turmas.size() + " turmas encontradas.");

            // 2. Salva os arquivos JSON a partir dos dados carregados
            System.out.println("\nRecriando arquivos JSON...");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String dataDir = "D:/Local/Dev/Java/notas_java/data";

            for (Turma turma : turmas) {
                // Salva a lista de alunos da turma
                List<AlunoDTO> alunosDTO = turma.getAlunos().stream()
                        .map(a -> new AlunoDTO(a.getNome(), a.getRa()))
                        .collect(Collectors.toList());
                String caminhoAlunos = dataDir + File.separator + "turmas" + File.separator + turma.getCodigoTurma() + ".json";
                salvarJson(alunosDTO, caminhoAlunos, gson);

                // Salva os arquivos de notas
                for (Disciplina disciplina : turma.getDisciplinas()) {
                    for (int trimestre = 1; trimestre <= 3; trimestre++) {
                        // Reutiliza a lógica de salvar notas do GerenciadorDeDados
                        // (A lógica foi copiada aqui para manter a ferramenta independente)
                        salvarNotasPorTurmaDisciplinaTrimestre(turma, disciplina, trimestre, dataDir + "/notas_json", gson);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ERRO CRÍTICO durante a restauração do banco de dados: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n--- RESTAURAÇÃO CONCLUÍDA ---");
    }

    private static void salvarJson(Object data, String caminho, Gson gson) {
        try (Writer writer = new FileWriter(caminho)) {
            gson.toJson(data, writer);
            System.out.println(" -> Arquivo salvo: " + caminho);
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo JSON: " + e.getMessage());
        }
    }

    // Método copiado e adaptado de GerenciadorDeDados para salvar os DTOs de notas
    private static void salvarNotasPorTurmaDisciplinaTrimestre(Turma turma, Disciplina disciplina, int trimestre, String notasDir, Gson gson) {
        List<NotaDTO> notasDTOList = new ArrayList<>();
        for (Aluno aluno : turma.getAlunos()) {
            NotaDTO dto = new NotaDTO();
            dto.setNome(aluno.getNome());
            dto.setRa(aluno.getRa());

            Map<String, Double> notasDoTrimestre = aluno.getNotas().stream()
                .filter(n -> n.getDisciplina().equals(disciplina.getNomeDisciplina()) && n.getDescricao().startsWith("T" + trimestre))
                .collect(Collectors.toMap(n -> n.getDescricao(), n -> n.getValor(), (v1, v2) -> v1));

            dto.setNm1(String.valueOf(notasDoTrimestre.getOrDefault("T" + trimestre + " - N1", 0.0)));
            dto.setNm2(String.valueOf(notasDoTrimestre.getOrDefault("T" + trimestre + " - N2", 0.0)));
            dto.setNm3(String.valueOf(notasDoTrimestre.getOrDefault("T" + trimestre + " - N3", 0.0)));
            dto.setRecuperacao(String.valueOf(notasDoTrimestre.getOrDefault("T" + trimestre + " - REC", 0.0)));
            dto.setTotalFaltas("");

            notasDTOList.add(dto);
        }

        String nomeArquivo = "notas_" + turma.getCodigoTurma() + "_" + disciplina.getCodigoDisciplina() + "_T" + trimestre + ".json";
        String caminhoArquivo = notasDir + File.separator + nomeArquivo;
        salvarJson(notasDTOList, caminhoArquivo, gson);
    }
}