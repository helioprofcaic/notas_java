package model;

public class Disciplina {
    private String codigoDisciplina;
    private String nomeDisciplina;
    private String tipo; // "ANUAL" ou "MODULAR"

    public Disciplina(String codigoDisciplina, String nomeDisciplina, String tipo) {
        this.codigoDisciplina = codigoDisciplina;
        this.nomeDisciplina = nomeDisciplina;
        this.tipo = (tipo != null) ? tipo : "ANUAL"; // Padrão para ANUAL se não for especificado
    }

    public String getCodigoDisciplina() {
        return codigoDisciplina;
    }

    public void setCodigoDisciplina(String codigoDisciplina) {
        this.codigoDisciplina = codigoDisciplina;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public void setNomeDisciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Disciplina{" +
                "codigoDisciplina='" + codigoDisciplina + '\'' +
                ", nomeDisciplina='" + nomeDisciplina + '\'' +
                ", tipo='" + tipo + '\'' +
                '}';
    }
}
