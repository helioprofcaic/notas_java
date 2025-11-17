package dao;

import model.Turma;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para a entidade Turma.
 * Encapsula todo o acesso ao banco de dados para a tabela 'turma'.
 */
public class TurmaDAO {

    private Connection conn;

    public TurmaDAO(Connection conn) {
        this.conn = conn;
    }

    public List<Turma> buscarTodas() {
        String sql = "SELECT * FROM turma";
        List<Turma> turmas = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Turma turma = new Turma(rs.getString("codigo"), rs.getString("nome"));
                turma.setId(rs.getInt("id"));
                turmas.add(turma);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar turmas: " + e.getMessage());
        }
        return turmas;
    }

    public int inserir(Turma turma) {
        String sql = "INSERT INTO turma(codigo, nome) VALUES(?, ?)";
        int id = -1;
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, turma.getCodigoTurma());
            pstmt.setString(2, turma.getNomeTurma());
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
                turma.setId(id); // Define o ID no objeto
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inserir turma: " + e.getMessage());
        }
        return id;
    }

    public void limparTabelas() {
        // A ordem é importante para respeitar as chaves estrangeiras
        String sql = "DELETE FROM nota; DELETE FROM aluno; DELETE FROM disciplina; DELETE FROM turma;";
        try (Statement stmt = conn.createStatement()) {
            // H2 permite múltiplos comandos separados por ponto e vírgula
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Erro ao limpar tabelas: " + e.getMessage());
        }
    }
}
