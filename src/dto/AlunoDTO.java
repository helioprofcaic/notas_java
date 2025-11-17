package dto;

// DTO (Data Transfer Object) para desserialização dos dados do aluno do JSON.
// Usar um DTO torna o código mais robusto a mudanças no formato do JSON.
// Torná-lo imutável (campos final e sem setters) é uma boa prática.
public class AlunoDTO {
    private final String nome;
    private final String ra;

    // Construtor para inicializar os campos imutáveis.
    // A biblioteca Gson pode usar este construtor ou reflexão para criar o objeto.
    public AlunoDTO(String nome, String ra) {
        this.nome = nome;
        this.ra = ra;
    }

    public String getNome() {
        return nome;
    }

    public String getRa() {
        return ra;
    }
}
