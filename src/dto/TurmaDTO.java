package dto;

import java.util.List;

public class TurmaDTO {
    private String codigoTurma;
    private String nomeTurma;
    private List<DisciplinaDTO> disciplinas;

    // Getters para TurmaDTO
    public String getCodigoTurma() {
        return codigoTurma;
    }

    public String getNomeTurma() {
        return nomeTurma;
    }

    public List<DisciplinaDTO> getDisciplinas() {
        return disciplinas;
    }

    public static class DisciplinaDTO {
        private String codigoDisciplina;
        private String nomeDisciplina;
        private String tipo;

        // Getters para DisciplinaDTO
        public String getCodigoDisciplina() {
            return codigoDisciplina;
        }

        public String getNomeDisciplina() {
            return nomeDisciplina;
        }

        public String getTipo() {
            return tipo;
        }
    }
}
