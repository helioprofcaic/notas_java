package gui.viewmodel;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.Aluno;
import model.Disciplina;
import model.Turma;
import model.Situacao;

/**
 * ViewModel que representa um aluno e suas notas para uma disciplina específica na TableView.
 * Facilita a vinculação de dados (data binding) e a edição.
 */
public class AlunoNotaViewModel {

    private final Aluno aluno;
    private final Disciplina disciplina;
    private final Turma turma;

    private final StringProperty nome;
    private final StringProperty ra;
    private final DoubleProperty n1_t1, n2_t1, n3_t1;
    private final DoubleProperty n1_t2, n2_t2, n3_t2;
    private final DoubleProperty n1_t3, n2_t3, n3_t3;
    private final DoubleProperty rec_t1, rec_t2, rec_t3;
    private final DoubleProperty mediaFinal;
    private final StringProperty situacao;

    public AlunoNotaViewModel(Aluno aluno, Disciplina disciplina, Turma turma) {
        this.aluno = aluno;
        this.disciplina = disciplina;
        this.turma = turma;

        this.nome = new SimpleStringProperty(aluno.getNome());
        this.ra = new SimpleStringProperty(aluno.getRa());

        // Notas do Trimestre 1
        this.n1_t1 = new SimpleDoubleProperty(aluno.getNotaPorDescricao(disciplina.getNomeDisciplina(), "T1 - N1"));
        this.n2_t1 = new SimpleDoubleProperty(aluno.getNotaPorDescricao(disciplina.getNomeDisciplina(), "T1 - N2"));
        this.n3_t1 = new SimpleDoubleProperty(aluno.getNotaPorDescricao(disciplina.getNomeDisciplina(), "T1 - N3"));

        // Notas do Trimestre 2
        this.n1_t2 = new SimpleDoubleProperty(aluno.getNotaPorDescricao(disciplina.getNomeDisciplina(), "T2 - N1"));
        this.n2_t2 = new SimpleDoubleProperty(aluno.getNotaPorDescricao(disciplina.getNomeDisciplina(), "T2 - N2"));
        this.n3_t2 = new SimpleDoubleProperty(aluno.getNotaPorDescricao(disciplina.getNomeDisciplina(), "T2 - N3"));

        // Notas do Trimestre 3
        this.n1_t3 = new SimpleDoubleProperty(aluno.getNotaPorDescricao(disciplina.getNomeDisciplina(), "T3 - N1"));
        this.n2_t3 = new SimpleDoubleProperty(aluno.getNotaPorDescricao(disciplina.getNomeDisciplina(), "T3 - N2"));
        this.n3_t3 = new SimpleDoubleProperty(aluno.getNotaPorDescricao(disciplina.getNomeDisciplina(), "T3 - N3"));

        // Notas de Recuperação
        this.rec_t1 = new SimpleDoubleProperty(aluno.getNotaPorDescricao(disciplina.getNomeDisciplina(), "T1 - REC"));
        this.rec_t2 = new SimpleDoubleProperty(aluno.getNotaPorDescricao(disciplina.getNomeDisciplina(), "T2 - REC"));
        this.rec_t3 = new SimpleDoubleProperty(aluno.getNotaPorDescricao(disciplina.getNomeDisciplina(), "T3 - REC"));

        // Média e Situação
        this.mediaFinal = new SimpleDoubleProperty(aluno.calcularMediaFinalAnual(disciplina.getNomeDisciplina(), turma));
        this.situacao = new SimpleStringProperty(aluno.getSituacaoFinal(disciplina.getNomeDisciplina(), turma).toString());
    }

    // --- Getters para as propriedades (necessário para a TableView) ---

    public StringProperty nomeProperty() { return nome; }
    public StringProperty raProperty() { return ra; }
    public DoubleProperty n1_t1Property() { return n1_t1; }
    public DoubleProperty n2_t1Property() { return n2_t1; }
    public DoubleProperty n3_t1Property() { return n3_t1; }
    public DoubleProperty n1_t2Property() { return n1_t2; }
    public DoubleProperty n2_t2Property() { return n2_t2; }
    public DoubleProperty n3_t2Property() { return n3_t2; }
    public DoubleProperty n1_t3Property() { return n1_t3; }
    public DoubleProperty n2_t3Property() { return n2_t3; }
    public DoubleProperty n3_t3Property() { return n3_t3; }
    public DoubleProperty rec_t1Property() { return rec_t1; }
    public DoubleProperty rec_t2Property() { return rec_t2; }
    public DoubleProperty rec_t3Property() { return rec_t3; }
    public DoubleProperty mediaFinalProperty() { return mediaFinal; }
    public StringProperty situacaoProperty() { return situacao; }

    // --- Getters para os modelos de dados ---

    public Aluno getAluno() {
        return aluno;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    // --- Recalcula a média e situação (útil após a edição de uma nota) ---

    public void recalcular() {
        // Atualiza o modelo de dados subjacente
        aluno.adicionarOuAtualizarNota(disciplina.getNomeDisciplina(), "T1 - N1", n1_t1.get());
        aluno.adicionarOuAtualizarNota(disciplina.getNomeDisciplina(), "T1 - N2", n2_t1.get());
        aluno.adicionarOuAtualizarNota(disciplina.getNomeDisciplina(), "T1 - N3", n3_t1.get());
        aluno.adicionarOuAtualizarNota(disciplina.getNomeDisciplina(), "T2 - N1", n1_t2.get());
        aluno.adicionarOuAtualizarNota(disciplina.getNomeDisciplina(), "T2 - N2", n2_t2.get());
        aluno.adicionarOuAtualizarNota(disciplina.getNomeDisciplina(), "T2 - N3", n3_t2.get());
        aluno.adicionarOuAtualizarNota(disciplina.getNomeDisciplina(), "T3 - N1", n1_t3.get());
        aluno.adicionarOuAtualizarNota(disciplina.getNomeDisciplina(), "T3 - N2", n2_t3.get());
        aluno.adicionarOuAtualizarNota(disciplina.getNomeDisciplina(), "T3 - N3", n3_t3.get());
        aluno.adicionarOuAtualizarNota(disciplina.getNomeDisciplina(), "T1 - REC", rec_t1.get());
        aluno.adicionarOuAtualizarNota(disciplina.getNomeDisciplina(), "T2 - REC", rec_t2.get());
        aluno.adicionarOuAtualizarNota(disciplina.getNomeDisciplina(), "T3 - REC", rec_t3.get());

        // Atualiza as propriedades da UI
        mediaFinal.set(aluno.calcularMediaFinalAnual(disciplina.getNomeDisciplina(), turma));
        situacao.set(aluno.getSituacaoFinal(disciplina.getNomeDisciplina(), turma).toString());
    }
}