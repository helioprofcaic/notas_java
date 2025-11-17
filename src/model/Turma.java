package model;

import java.util.ArrayList;
import java.util.List;

public class Turma {
    private int id; // ID do banco de dados
    private String codigoTurma;
    private String nomeTurma;
    private List<Disciplina> disciplinas;
    private List<Aluno> alunos;

    public Turma(String codigoTurma, String nomeTurma) {
        this.codigoTurma = codigoTurma;
        this.nomeTurma = nomeTurma;
        this.disciplinas = new ArrayList<>();
        this.alunos = new ArrayList<>();
    }

    // Getters e Setters para o ID
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public void adicionarDisciplina(Disciplina disciplina) {
        this.disciplinas.add(disciplina);
    }

    public void adicionarAluno(Aluno aluno) {
        this.alunos.add(aluno);
    }

    public String getCodigoTurma() {
        return codigoTurma;
    }

    public void setCodigoTurma(String codigoTurma) {
        this.codigoTurma = codigoTurma;
    }

    public String getNomeTurma() {
        return nomeTurma;
    }

    public void setNomeTurma(String nomeTurma) {
        this.nomeTurma = nomeTurma;
    }

    public List<Disciplina> getDisciplinas() {
        return disciplinas;
    }

    public List<Aluno> getAlunos() {
        return alunos;
    }

    @Override
    public String toString() {
        return "Turma{" +
                "codigoTurma='" + codigoTurma + '\'' +
                ", nomeTurma='" + nomeTurma + '\'' +
                ", disciplinas=" + disciplinas +
                ", alunos=" + alunos +
                '}';
    }
}
