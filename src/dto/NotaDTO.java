package dto;

// DTO para desserialização dos dados de notas do JSON.
public class NotaDTO {
    private String nome;
    private String ra;
    private String nm1;
    private String nm2;
    private String nm3;
    private String recuperacao;
    private String totalFaltas;

    // Getters
    public String getNome() {
        return nome;
    }

    public String getRa() {
        return ra;
    }

    public String getNm1() {
        return nm1;
    }

    public String getNm2() {
        return nm2;
    }

    public String getNm3() {
        return nm3;
    }

    public String getRecuperacao() {
        return recuperacao;
    }

    public String getTotalFaltas() {
        return totalFaltas;
    }

    // Setters
    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setRa(String ra) {
        this.ra = ra;
    }

    public void setNm1(String nm1) {
        this.nm1 = nm1;
    }

    public void setNm2(String nm2) {
        this.nm2 = nm2;
    }

    public void setNm3(String nm3) {
        this.nm3 = nm3;
    }

    public void setRecuperacao(String recuperacao) {
        this.recuperacao = recuperacao;
    }

    public void setTotalFaltas(String totalFaltas) {
        this.totalFaltas = totalFaltas;
    }
}
