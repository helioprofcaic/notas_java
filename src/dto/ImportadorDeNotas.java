package dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportadorDeNotas {

    public static void importarDeMoodleCSV(String caminhoArquivo, Map<String, String> mapaAlunos, String codigoTurma, String codigoDisciplina, int trimestre, String diretorioSaida, String nomeArquivoSaida) {
        try {
            List<String> linhas = Files.readAllLines(Paths.get(caminhoArquivo));
            List<NotaDTO> notasDTOList = new ArrayList<>();

            for (int i = 1; i < linhas.size(); i++) {
                String linha = linhas.get(i);
                if (linha.trim().isEmpty()) continue;

                String[] colunas = linha.split("\t");

                if (colunas.length > 7) {
                    String nome = colunas[1].trim();
                    String sobrenome = colunas[2].trim().replace("\"", "");
                    String nomeCompleto = (nome + " " + sobrenome).toUpperCase(); // Converte para caixa alta aqui
                    String nomeNormalizado = normalizarNome(nomeCompleto);

                    String ra = mapaAlunos.get(nomeNormalizado);

                    if (ra == null) {
                        System.err.printf("  -> AVISO: Aluno '%s' não encontrado. O RA ficará em branco.\n", nomeCompleto);
                        ra = "";
                    }

                    NotaDTO dto = new NotaDTO();
                    dto.setNome(nomeCompleto);
                    dto.setRa(ra);
                    dto.setNm1(colunas[3].trim().replace(",", "."));
                    dto.setNm2(colunas[5].trim().replace(",", "."));
                    dto.setNm3(colunas[7].trim().replace(",", "."));
                    dto.setTotalFaltas("");
                    
                    notasDTOList.add(dto);
                }
            }
            gerarArquivoJSON(notasDTOList, diretorioSaida, nomeArquivoSaida);
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo do Moodle: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Normaliza um nome para comparação: converte para maiúsculas, remove espaços extras e remove acentos.
     * @param nome O nome a ser normalizado.
     * @return O nome normalizado.
     */
    public static String normalizarNome(String nome) {
        String nomeSemAcentos = Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return nomeSemAcentos.trim().toUpperCase().replaceAll("\\s+", " ");
    }
    
    public static void importarDeCSV(String caminhoArquivoCSV, String codigoTurma, String codigoDisciplina, int trimestre, String diretorioSaida) {
        try {
            List<String> linhas = Files.readAllLines(Paths.get(caminhoArquivoCSV));
            List<NotaDTO> notasDTOList = new ArrayList<>();

            for (int i = 1; i < linhas.size(); i++) {
                String linha = linhas.get(i);
                if (linha.trim().isEmpty()) continue;

                String[] colunas = linha.split(",");

                if (colunas.length >= 5) {
                    NotaDTO dto = new NotaDTO();
                    dto.setNome(colunas[0].trim());
                    dto.setRa(colunas[1].trim());
                    dto.setNm1(colunas[2].trim());
                    dto.setNm2(colunas[3].trim());
                    dto.setNm3(colunas[4].trim());
                    dto.setTotalFaltas((colunas.length > 5) ? colunas[5].trim() : "");
                    notasDTOList.add(dto);
                }
            }
            gerarArquivoJSON(notasDTOList, diretorioSaida, "notas_" + codigoTurma + "_" + codigoDisciplina + "_T" + trimestre + ".json");
        } catch (IOException e) {
            System.err.println("Erro ao ler o arquivo CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void importarDeTextoTabulado(String dadosTabulados, String codigoTurma, String codigoDisciplina, int trimestre, String diretorioSaida) {
        List<NotaDTO> notasDTOList = new ArrayList<>();
        String[] linhas = dadosTabulados.split("\\r?\\n");

        for (int i = 1; i < linhas.length; i++) {
            String linha = linhas[i];
            if (linha.trim().isEmpty()) continue;

            String[] colunas = linha.split("\t");

            if (colunas.length >= 5) {
                NotaDTO dto = new NotaDTO();
                dto.setNome(colunas[0].trim());
                dto.setRa(colunas[1].trim());
                dto.setNm1(colunas[2].trim());
                dto.setNm2(colunas[3].trim());
                dto.setNm3(colunas[4].trim());
                dto.setTotalFaltas((colunas.length > 5) ? colunas[5].trim() : "");
                notasDTOList.add(dto);
            }
        }
        gerarArquivoJSON(notasDTOList, diretorioSaida, "notas_" + codigoTurma + "_" + codigoDisciplina + "_T" + trimestre + ".json");
    }

    private static void gerarArquivoJSON(List<NotaDTO> notasDTOList, String diretorioSaida, String nomeArquivoJson) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String caminhoCompleto = diretorioSaida + File.separator + nomeArquivoJson;

        try (Writer writer = new FileWriter(caminhoCompleto)) {
            gson.toJson(notasDTOList, writer);
            System.out.printf(" -> Arquivo JSON gerado com sucesso em: %s\n", caminhoCompleto);
        } catch (IOException e) {
            System.err.println("Erro ao escrever o arquivo JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
