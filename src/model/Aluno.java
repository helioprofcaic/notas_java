package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Aluno {
    private int id; // ID do banco de dados
    private String nome;
    private String ra;
    private String email;
    private List<Nota> notas;

    // Constantes para as médias de corte
    public static final double MEDIA_APROVACAO = 6.0;
    public static final double MEDIA_RECUPERACAO = 4.0;

    public Aluno(String nome, String ra) {
        this.nome = nome;
        this.ra = ra;
        this.email = null;
        this.notas = new ArrayList<>();
    }

    // Getters e Setters para o ID
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public void adicionarNota(Nota nota) {
        this.notas.add(nota);
    }

    public double getNotaPorDescricao(String disciplina, String descricao) {
        return notas.stream()
                .filter(n -> n.getDisciplina().equalsIgnoreCase(disciplina) && n.getDescricao().equals(descricao))
                .findFirst()
                .map(Nota::getValor)
                .orElse(0.0);
    }

    public void adicionarOuAtualizarNota(String disciplina, String descricao, double valor) {
        // Tenta encontrar uma nota existente para atualizar
        Optional<Nota> notaExistente = notas.stream()
                .filter(n -> n.getDisciplina().equalsIgnoreCase(disciplina) && n.getDescricao().equals(descricao))
                .findFirst();

        if (notaExistente.isPresent()) {
            // Atualiza a nota existente
            notaExistente.get().setValor(valor);
        } else {
            // Adiciona uma nova nota se não existir
            adicionarNota(new Nota(valor, disciplina, descricao));
        }
    }

    public double calcularMediaTrimestral(String disciplina, int trimestre, Turma turma) {
        List<Nota> notasDoTrimestre = notas.stream()
                .filter(n -> n.getDisciplina().equalsIgnoreCase(disciplina) && n.getDescricao().startsWith("T" + trimestre))
                .filter(n -> !n.getDescricao().contains("REC")) // Exclui a nota de recuperação do cálculo inicial
                .collect(Collectors.toList());

        if (notasDoTrimestre.isEmpty()) {
            return 0.0;
        }

        double mediaOriginal = notasDoTrimestre.stream().mapToDouble(Nota::getValor).sum() / notasDoTrimestre.size();

        // Lógica da Recuperação para 8º e 9º ano
        boolean isAlunoElegivel = turma.getNomeTurma().contains("8º ANO") || turma.getNomeTurma().contains("9º ANO");

        if (isAlunoElegivel && mediaOriginal < MEDIA_APROVACAO) {
            double notaRecuperacao = getNotaPorDescricao(disciplina, "T" + trimestre + " - REC");
            // A média do trimestre se torna a maior nota entre a média original e a recuperação.
            return Math.max(mediaOriginal, notaRecuperacao);
        }

        return mediaOriginal;
    }

    public double calcularMediaFinalAnual(String disciplina) {
        // Este método agora não pode ser chamado sem o contexto da Turma.
        // Mantido por compatibilidade, mas idealmente seria removido ou adaptado.
        // Para a GUI, usaremos o método sobrecarregado abaixo.
        return 0; // Ou lançar uma exceção
    }

    public double calcularMediaFinalAnual(String disciplina, Turma turma) {
        double somaDasMediasTrimestrais = 0;
        somaDasMediasTrimestrais += calcularMediaTrimestral(disciplina, 1, turma);
        somaDasMediasTrimestrais += calcularMediaTrimestral(disciplina, 2, turma);
        somaDasMediasTrimestrais += calcularMediaTrimestral(disciplina, 3, turma);
        
        return somaDasMediasTrimestrais / 3.0;
    }

    public Situacao getSituacaoFinal(String disciplina, Turma turma) {
        double mediaFinal = calcularMediaFinalAnual(disciplina, turma);

        if (mediaFinal >= MEDIA_APROVACAO) {
            return Situacao.APROVADO;
        } else if (mediaFinal >= MEDIA_RECUPERACAO) {
            return Situacao.RECUPERACAO_FINAL;
        } else {
            return Situacao.REPROVADO;
        }
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getRa() { return ra; }
    public void setRa(String ra) { this.ra = ra; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<Nota> getNotas() { return notas; }

    @Override
    public String toString() {
        return "\n    Aluno{" +
                "nome='" + nome + '\'' +
                ", ra='" + ra + '\'' +
                ", email='" + (email != null ? email : "N/A") + '\'' +
                ", notas=" + notas +
                '}';
    }
}
