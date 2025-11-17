package service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dto.AlunoDTO;
import dto.NotaDTO;
import dto.TurmaDTO;
import model.*; // Importa todas as classes do pacote model

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GerenciadorDeDados {

    private List<Turma> turmas;
    private final String dataDirectoryPath;

    public GerenciadorDeDados(String caminhoArquivoTurmas) {
        this.dataDirectoryPath = new File(caminhoArquivoTurmas).getParent();
        // CORREÇÃO: Separa o carregamento em duas fases para evitar recursão.
        // 1. Carrega a estrutura de turmas e alunos.
        List<Turma> turmasCarregadas = carregarEstruturaBase(caminhoArquivoTurmas);
        this.turmas = turmasCarregadas; // Atribui a lista principal.
        // 2. Agora, com a estrutura base pronta, carrega as notas.
        carregarTodasAsNotas();
    }

    // --- MÉTODOS PÚBLICOS PRINCIPAIS (API do Gerenciador) ---

    public List<Turma> getTurmas() {
        return turmas;
    }

    public Optional<Turma> buscarTurmaPorCodigo(String codigoTurma) {
        return turmas.stream()
                .filter(t -> t.getCodigoTurma().equals(codigoTurma))
                .findFirst();
    }

    public Optional<Aluno> buscarAlunoPorRa(String ra) {
        return turmas.stream()
                .flatMap(t -> t.getAlunos().stream())
                .filter(a -> a.getRa().equals(ra))
                .findFirst();
    }

    public List<String> gerarRelatorioFinalTurma(String codigoTurma, String nomeDisciplina) {
        Optional<Turma> turmaOpt = buscarTurmaPorCodigo(codigoTurma);
        if (turmaOpt.isEmpty()) {
            return Collections.singletonList("ERRO: Turma com código " + codigoTurma + " não encontrada.");
        }

        Turma turma = turmaOpt.get();
        List<String> relatorio = new ArrayList<>();
        relatorio.add(String.format("--- Relatório Final da Turma: %s ---", turma.getNomeTurma()));
        relatorio.add(String.format("--- Disciplina: %s ---\n", nomeDisciplina));

        for (Aluno aluno : turma.getAlunos()) {
            double mediaFinal = aluno.calcularMediaFinalAnual(nomeDisciplina, turma);
            Situacao situacao = aluno.getSituacaoFinal(nomeDisciplina, turma);
            relatorio.add(String.format("Aluno: %-30s | Média Final: %.2f | Situação: %s",
                    aluno.getNome(), mediaFinal, situacao));
        }

        return relatorio;
    }

    public String salvarRelatorioMarkdown(Turma turma, Disciplina disciplina) throws IOException {
        // Define o caminho e nome do arquivo
        String nomeArquivo = "relatorio_" + turma.getCodigoTurma() + "_" + disciplina.getCodigoDisciplina() + ".md";
        String caminhoArquivo = dataDirectoryPath + File.separator + nomeArquivo;

        // Gera o conteúdo do relatório usando o template
        String conteudoMarkdown = gerarConteudoRelatorio(turma, disciplina);

        // Salva o arquivo
        Files.writeString(Paths.get(caminhoArquivo), conteudoMarkdown);

        return caminhoArquivo;
    }

    public void gerarRelatorioPDF(Turma turma, Disciplina disciplina, File arquivoDeSaida) throws IOException {
        String conteudoMarkdown = gerarConteudoRelatorio(turma, disciplina);
        String[] linhas = conteudoMarkdown.split("\\r?\\n");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            PDRectangle mediaBox = page.getMediaBox();

            // Define as fontes
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontItalic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            // Configurações de layout
            float margin = 50;
            float yStart = mediaBox.getHeight() - margin;
            float yPosition = yStart;
            float leading = 15f; // Espaçamento entre linhas
            int pageNumber = 1;

            // --- Escreve o Conteúdo do PDF ---
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            try {
                for (String linha : linhas) {
                    if (yPosition <= margin) {
                        contentStream.close();
                        page = new PDPage();
                        document.addPage(page);
                        contentStream = new PDPageContentStream(document, page);
                        yPosition = yStart;
                    }

                    contentStream.beginText();
                    if (linha.startsWith("# ")) {
                        contentStream.setFont(fontBold, 16);
                        linha = linha.substring(2);
                    } else if (linha.startsWith("## ")) {
                        contentStream.setFont(fontBold, 14);
                        linha = linha.substring(3);
                    } else if (linha.startsWith("|")) {
                        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 9);
                    } else {
                        contentStream.setFont(fontRegular, 10);
                    }
                    contentStream.newLineAtOffset(margin, yPosition);

                    // Remove emojis e outros símbolos não suportados antes de desenhar no PDF
                    linha = linha.replaceAll("[\\p{So}\\p{Cn}]", "").trim();

                    contentStream.showText(linha);
                    contentStream.endText();
                    yPosition -= leading;
                }
            } finally {
                contentStream.close(); // Garante que o último contentStream seja fechado
            }
            document.save(arquivoDeSaida);
        }
    }

    private String gerarConteudoRelatorio(Turma turma, Disciplina disciplina) throws IOException {
        String template;
        try (InputStream templateStream = getClass().getResourceAsStream("/relatorio_template.md")) {
            if (templateStream == null) {
                System.err.println("AVISO: Arquivo de template 'relatorio_template.md' não encontrado. Gerando relatório com formato padrão.");
                // Fallback: Gera um relatório básico se o template não for encontrado
                StringBuilder fallbackContent = new StringBuilder();
                fallbackContent.append("# Relatório Final da Turma: ").append(turma.getNomeTurma()).append("\n\n");
                fallbackContent.append("## Disciplina: ").append(disciplina.getNomeDisciplina()).append("\n\n");
                fallbackContent.append("| Aluno                                | Média Final | Situação          |\n");
                fallbackContent.append("| ------------------------------------ | ----------- | ----------------- |\n");
                template = fallbackContent.toString() + "{{tabela_alunos}}";
            } else {
                template = new String(templateStream.readAllBytes());
            }
        }

        // Preenche os placeholders estáticos
        template = template.replace("{{turma_nome}}", turma.getNomeTurma());
        template = template.replace("{{disciplina_nome}}", disciplina.getNomeDisciplina());
        template = template.replace("{{data_geracao}}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));

        // Gera a tabela de alunos dinamicamente
        StringBuilder tabela = new StringBuilder();
        for (Aluno aluno : turma.getAlunos()) {
            double mediaFinal = aluno.calcularMediaFinalAnual(disciplina.getNomeDisciplina(), turma);
            Situacao situacao = aluno.getSituacaoFinal(disciplina.getNomeDisciplina(), turma);
            tabela.append(String.format("\n| %-36s | %-11.2f | %-17s |", aluno.getNome(), mediaFinal, situacao));
        }

        // Substitui o placeholder da tabela
        return template.replace("{{tabela_alunos}}", tabela.toString());
    }

    public boolean adicionarOuAtualizarNota(String ra, String nomeDisciplina, String descricaoNota, double valor) {
        Optional<Aluno> alunoOpt = buscarAlunoPorRa(ra);
        if (alunoOpt.isPresent()) {
            Aluno aluno = alunoOpt.get();
            Optional<Nota> notaOpt = aluno.getNotas().stream()
                    .filter(n -> n.getDisciplina().equals(nomeDisciplina) && n.getDescricao().equals(descricaoNota))
                    .findFirst();

            if (notaOpt.isPresent()) {
                notaOpt.get().setValor(valor);
            } else {
                aluno.adicionarNota(new Nota(valor, nomeDisciplina, descricaoNota));
            }
            return true;
        }
        return false;
    }

    public void salvarAlteracoesNotas() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String notasDirectoryPath = dataDirectoryPath + File.separator + "notas_json";

        for (Turma turma : turmas) {
            for (Disciplina disciplina : turma.getDisciplinas()) {
                for (int trimestre = 1; trimestre <= 3; trimestre++) {
                    salvarNotasPorTurmaDisciplinaTrimestre(turma, disciplina, trimestre, notasDirectoryPath, gson);
                }
            }
        }
    }

    // --- LÓGICA DE SALVAMENTO ---

    private void salvarNotasPorTurmaDisciplinaTrimestre(Turma turma, Disciplina disciplina, int trimestre, String notasDir, Gson gson) {
        List<NotaDTO> notasDTOList = new ArrayList<>();
        for (Aluno aluno : turma.getAlunos()) {
            NotaDTO dto = new NotaDTO();
            dto.setNome(aluno.getNome());
            dto.setRa(aluno.getRa());

            Map<String, Double> notasDoTrimestre = aluno.getNotas().stream()
                    .filter(n -> n.getDisciplina().equals(disciplina.getNomeDisciplina()) && n.getDescricao().startsWith("T" + trimestre))
                    .collect(Collectors.toMap(Nota::getDescricao, Nota::getValor, (v1, v2) -> v1));

            dto.setNm1(String.valueOf(notasDoTrimestre.getOrDefault("T" + trimestre + " - N1", 0.0)));
            dto.setNm2(String.valueOf(notasDoTrimestre.getOrDefault("T" + trimestre + " - N2", 0.0)));
            dto.setNm3(String.valueOf(notasDoTrimestre.getOrDefault("T" + trimestre + " - N3", 0.0)));
            dto.setRecuperacao(String.valueOf(notasDoTrimestre.getOrDefault("T" + trimestre + " - REC", 0.0)));
            dto.setTotalFaltas("");

            notasDTOList.add(dto);
        }

        // Adicionado o prefixo "notas_" ao nome do arquivo de saída.
        String nomeArquivo = "notas_" + turma.getCodigoTurma() + "_" + disciplina.getCodigoDisciplina() + "_T" + trimestre + ".json";
        String caminhoArquivo = notasDir + File.separator + nomeArquivo;

        try (Writer writer = new FileWriter(caminhoArquivo)) {
            gson.toJson(notasDTOList, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // --- LÓGICA DE CARREGAMENTO ---

    /**
     * Fase 1: Carrega a estrutura de turmas, disciplinas e alunos.
     */
    private List<Turma> carregarEstruturaBase(String caminhoArquivoTurmas) {
        Gson gson = new Gson();
        List<Turma> turmasCarregadas = new ArrayList<>();

        try (Reader reader = new FileReader(caminhoArquivoTurmas)) {
            Type tipoListaTurmaDTO = new TypeToken<ArrayList<TurmaDTO>>(){}.getType();
            List<TurmaDTO> turmasDTO = gson.fromJson(reader, tipoListaTurmaDTO);

            if (turmasDTO != null) {
                for (TurmaDTO dto : turmasDTO) {
                    Turma turma = new Turma(dto.getCodigoTurma(), dto.getNomeTurma());
                    if (dto.getDisciplinas() != null) {
                        for (TurmaDTO.DisciplinaDTO disciplinaDTO : dto.getDisciplinas()) {
                            turma.adicionarDisciplina(new Disciplina(disciplinaDTO.getCodigoDisciplina(), 
                                                                   disciplinaDTO.getNomeDisciplina(), disciplinaDTO.getTipo()));
                        }
                    }
                    turmasCarregadas.add(turma);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Turma turma : turmasCarregadas) {
            carregarAlunosParaTurma(turma, dataDirectoryPath, gson);
        }
        return turmasCarregadas;
    }

    /**
     * Fase 2: Carrega as notas para a estrutura de turmas já existente.
     */
    private void carregarTodasAsNotas() {
        Gson gson = new Gson();
        String notasDirectoryPath = dataDirectoryPath + File.separator + "notas_json";
        for (Turma turma : this.turmas) {
            for (Disciplina disciplina : turma.getDisciplinas()) {
                for (int trimestre = 1; trimestre <= 3; trimestre++) {
                    carregarNotasParaTurmaDisciplina(turma, disciplina, trimestre, notasDirectoryPath, gson);
                }
            }
        }
    }

    private void carregarAlunosParaTurma(Turma turma, String dir, Gson gson) {
        String caminhoArquivo = dir + File.separator + "turmas" + File.separator + turma.getCodigoTurma() + ".json";
        try (Reader reader = new FileReader(caminhoArquivo)) {
            Type tipoListaAlunoDTO = new TypeToken<ArrayList<AlunoDTO>>(){}.getType();
            List<AlunoDTO> alunosDTO = gson.fromJson(reader, tipoListaAlunoDTO);
            if (alunosDTO != null) {
                for (AlunoDTO dto : alunosDTO) {
                    turma.adicionarAluno(new Aluno(dto.getNome(), dto.getRa()));
                }
            }
        } catch (FileNotFoundException e) {
            System.err.printf("        - AVISO: Arquivo de alunos '%s' não encontrado para a turma '%s'.%n",
                    turma.getCodigoTurma() + ".json", turma.getNomeTurma());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void carregarNotasParaTurmaDisciplina(Turma turma, Disciplina disciplina, int trimestre, String notasDir, Gson gson) {
        String nomeArquivo = "notas_" + turma.getCodigoTurma() + "_" + disciplina.getCodigoDisciplina() + "_T" + trimestre + ".json";
        String caminhoArquivo = notasDir + File.separator + nomeArquivo;

        File arquivoNotas = new File(caminhoArquivo);
        if (!arquivoNotas.exists()) return; // Se o arquivo não existe, simplesmente retorna.

        try (Reader reader = new FileReader(caminhoArquivo)) {
            Type tipoListaNotaDTO = new TypeToken<ArrayList<NotaDTO>>(){}.getType();
            List<NotaDTO> notasDTO = gson.fromJson(reader, tipoListaNotaDTO);

            if (notasDTO == null) return;

            for (NotaDTO dto : notasDTO) {
                Optional<Aluno> alunoOpt = buscarAlunoPorRa(dto.getRa());
                if (alunoOpt.isPresent()) {
                    Aluno aluno = alunoOpt.get();
                    adicionarNotaSeValida(aluno, dto.getNm1(), disciplina.getNomeDisciplina(), "T" + trimestre + " - N1");
                    adicionarNotaSeValida(aluno, dto.getNm2(), disciplina.getNomeDisciplina(), "T" + trimestre + " - N2");
                    adicionarNotaSeValida(aluno, dto.getNm3(), disciplina.getNomeDisciplina(), "T" + trimestre + " - N3");
                    adicionarNotaSeValida(aluno, dto.getRecuperacao(), disciplina.getNomeDisciplina(), "T" + trimestre + " - REC");
                } else {
                    System.err.printf("            - AVISO: Aluno com RA '%s' do arquivo de notas não foi encontrado na lista da turma.%n", dto.getRa());
                }
            }

        } catch (FileNotFoundException e) {
            // Silencioso, pois é esperado que nem todas as turmas/disciplinas/trimestres tenham arquivos de notas
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void adicionarNotaSeValida(Aluno aluno, String valorNota, String disciplina, String descricao) {
        if (valorNota != null && !valorNota.trim().isEmpty()) {
            try {
                double valor = Double.parseDouble(valorNota.replace(',', '.'));
                aluno.adicionarNota(new Nota(valor, disciplina, descricao));
            } catch (NumberFormatException e) {
                // Silencioso
            }
        }
    }
}
