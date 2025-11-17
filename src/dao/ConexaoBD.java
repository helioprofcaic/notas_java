package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexaoBD {

    private static final String URL = "jdbc:h2:D:/Local/Dev/Java/notas_java/data/notas_backup";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void criarTabelasSeNaoExistirem() {
        String sqlTurma = "CREATE TABLE IF NOT EXISTS turma (" +
                " id INT AUTO_INCREMENT PRIMARY KEY," +
                " codigo VARCHAR(255) NOT NULL UNIQUE," +
                " nome VARCHAR(255) NOT NULL);";

        String sqlDisciplina = "CREATE TABLE IF NOT EXISTS disciplina (" +
                " id INT AUTO_INCREMENT PRIMARY KEY," +
                " codigo VARCHAR(255) NOT NULL," +
                " nome VARCHAR(255) NOT NULL," +
                " turma_id INT," +
                " FOREIGN KEY (turma_id) REFERENCES turma(id) ON DELETE CASCADE);";

        String sqlAluno = "CREATE TABLE IF NOT EXISTS aluno (" +
                " id INT AUTO_INCREMENT PRIMARY KEY," +
                " nome VARCHAR(255) NOT NULL," +
                " ra VARCHAR(255) NOT NULL," +
                " turma_id INT," +
                " FOREIGN KEY (turma_id) REFERENCES turma(id) ON DELETE CASCADE);";

        String sqlNota = "CREATE TABLE IF NOT EXISTS nota (" +
                " id INT AUTO_INCREMENT PRIMARY KEY," +
                " disciplina VARCHAR(255) NOT NULL," +
                " descricao VARCHAR(255) NOT NULL," +
                " valor DOUBLE NOT NULL," +
                " aluno_id INT," +
                " FOREIGN KEY (aluno_id) REFERENCES aluno(id) ON DELETE CASCADE);";

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlTurma);
            stmt.execute(sqlDisciplina);
            stmt.execute(sqlAluno);
            stmt.execute(sqlNota);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
