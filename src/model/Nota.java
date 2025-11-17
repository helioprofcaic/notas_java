package model;

public class Nota {
    private double valor;
    private String disciplina;
    private String descricao;

    public Nota(double valor, String disciplina, String descricao) {
        this.valor = valor;
        this.disciplina = disciplina;
        this.descricao = descricao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public String getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(String disciplina) {
        this.disciplina = disciplina;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public String toString() {
        return "Nota{" +
                "valor=" + valor +
                ", disciplina='" + disciplina + '\'' +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}
