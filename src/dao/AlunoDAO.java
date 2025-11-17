package dao;

import model.Aluno;
import model.Nota;
import model.Turma;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AlunoDAO {

    private Connection conn;
    private PreparedStatement pstmtAlunoLote;
    private PreparedStatement pstmtNotaLote;

    public AlunoDAO(Connection conn) throws SQLException {
        this.conn = conn;
        this.pstmtAlunoLote = conn.prepareStatement("INSERT INTO aluno (nome, ra, turma_id) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
        this.pstmtNotaLote = conn.prepareStatement("INSERT INTO nota (disciplina, descricao, valor, aluno_id) VALUES (?, ?, ?, ?)");
    }

    public List<Aluno> buscarPorTurmaId(int turmaId) {
        String sql = "SELECT * FROM aluno WHERE turma_id = ?";
        List<Aluno> alunos = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, turmaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Aluno aluno = new Aluno(rs.getString("nome"), rs.getString("ra"));
                aluno.setId(rs.getInt("id"));
                alunos.add(aluno);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar alunos: " + e.getMessage());
        }
        return alunos;
    }

    public void adicionarAlunosParaLote(List<Aluno> alunos, int turmaId) throws SQLException {
        for (Aluno aluno : alunos) {
            pstmtAlunoLote.setString(1, aluno.getNome());
            pstmtAlunoLote.setString(2, aluno.getRa());
            pstmtAlunoLote.setInt(3, turmaId);
            pstmtAlunoLote.addBatch();
        }
    }

    public void executarLoteDeAlunos() throws SQLException {
        pstmtAlunoLote.executeBatch();
    }

    public int adicionarNotasParaLote(List<Turma> turmas) throws SQLException {
        int totalNotasAdicionadas = 0;
        // Após executar o lote de alunos, obtemos os IDs gerados
        ResultSet rs = pstmtAlunoLote.getGeneratedKeys();

        // Itera sobre todas as turmas e todos os alunos para associar o ID correto
        for (Turma turma : turmas) {
            for (Aluno aluno : turma.getAlunos()) {
                if (rs.next()) {
                    int alunoId = rs.getInt(1);
                    aluno.setId(alunoId); // Define o ID no objeto

                    // Adiciona as notas deste aluno ao lote de notas
                    for (Nota nota : aluno.getNotas()) {
                        pstmtNotaLote.setString(1, nota.getDisciplina());
                        pstmtNotaLote.setString(2, nota.getDescricao());
                        pstmtNotaLote.setDouble(3, nota.getValor());
                        pstmtNotaLote.setInt(4, alunoId);
                        pstmtNotaLote.addBatch();
                        totalNotasAdicionadas++;
                    }
                }
            }
        }
        rs.close();
        return totalNotasAdicionadas;
    }

    public List<Nota> buscarNotasPorAlunoId(int alunoId) {
        String sql = "SELECT * FROM nota WHERE aluno_id = ?";
        List<Nota> notas = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, alunoId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                notas.add(new Nota(rs.getDouble("valor"), rs.getString("disciplina"), rs.getString("descricao")));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar notas: " + e.getMessage());
        }
        return notas;
    }

    public void executarLoteDeNotas() throws SQLException {
        pstmtNotaLote.executeBatch();
    }

    public void fecharStatements() throws SQLException {
        if (pstmtAlunoLote != null) {
            pstmtAlunoLote.close();
        }
        if (pstmtNotaLote != null) {
            pstmtNotaLote.close();
        }
    }
}