package dao;

import model.Disciplina;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

    private Connection conn;
    private PreparedStatement pstmtLote;

    public DisciplinaDAO(Connection conn) throws SQLException {
        this.conn = conn;
        String sql = "INSERT INTO disciplina (codigo, nome, turma_id) VALUES (?, ?, ?)";
        this.pstmtLote = conn.prepareStatement(sql);
    }

    public void inserirEmLote(List<Disciplina> disciplinas, int turmaId) throws SQLException {
        for (Disciplina disciplina : disciplinas) {
            pstmtLote.setString(1, disciplina.getCodigoDisciplina());
            pstmtLote.setString(2, disciplina.getNomeDisciplina());
            pstmtLote.setInt(3, turmaId);
            pstmtLote.addBatch();
        }
    }

    public void executarLote() throws SQLException {
        if (pstmtLote != null) {
            pstmtLote.executeBatch();
            pstmtLote.close();
        }
    }

    public List<Disciplina> buscarPorTurmaId(int turmaId) {
        String sql = "SELECT * FROM disciplina WHERE turma_id = ?";
        List<Disciplina> disciplinas = new ArrayList<>();
        // Usa a conexão da classe, mas cria um novo PreparedStatement para esta consulta
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, turmaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                disciplinas.add(new Disciplina(rs.getString("codigo"), rs.getString("nome"), rs.getString("tipo")));
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar disciplinas: " + e.getMessage());
        }
        return disciplinas;
    }
}
