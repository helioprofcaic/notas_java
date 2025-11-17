package tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dto.AlunoDTO;
import dto.ImportadorDeNotas;
import dto.NotaDTO;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardCopyOption;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Ferramenta de importação em lote.
 * Processa todos os arquivos .csv encontrados na pasta "data/importar"
 * que seguem o padrão de nome: {codigoTurma}_{codigoDisciplina}_T{trimestre}.csv
 *
 * Esta versão agora cruza os nomes dos alunos do CSV com os nomes dos arquivos JSON de alunos
 * para preencher o RA, que não vem no CSV do Moodle.
 */
public class FerramentaImportacao {

    public static void main(String[] args) {
        // --- CAMINHOS ---
        final String DIRETORIO_DATA = "D:/Local/Dev/Java/notas_java/data";
        final String DIRETORIO_IMPORTAR = DIRETORIO_DATA + "/importar";
        final String DIRETORIO_PROCESSADOS = DIRETORIO_IMPORTAR + "/processados";
        final String DIRETORIO_SAIDA_JSON = DIRETORIO_DATA + "/notas_json";

        // --- PREPARAÇÃO DOS DIRETÓRIOS ---
        new File(DIRETORIO_IMPORTAR).mkdirs();
        new File(DIRETORIO_PROCESSADOS).mkdirs();

        System.out.println("Iniciando processador de importações...");
        System.out.println("Procurando arquivos em: " + DIRETORIO_IMPORTAR); // Corrigido o typo aqui

        File pastaImportar = new File(DIRETORIO_IMPORTAR);
        File[] arquivosParaProcessar = pastaImportar.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv") || name.toLowerCase().endsWith(".json"));

        if (arquivosParaProcessar == null || arquivosParaProcessar.length == 0) {
            System.out.println("Nenhum arquivo .csv ou .json encontrado para processar em '" + DIRETORIO_IMPORTAR + "'.");
            return;
        }

        // Regex ajustada para aceitar tanto .csv quanto .json
        Pattern pattern = Pattern.compile("(\\d+)_([A-Z_]+)_T(\\d)\\.(csv|json)", Pattern.CASE_INSENSITIVE);

        int arquivosProcessados = 0;
        for (File arquivo : arquivosParaProcessar) {
            String nomeArquivoSemExtensao = arquivo.getName().substring(0, arquivo.getName().lastIndexOf('.'));
            Matcher matcher = pattern.matcher(arquivo.getName());

            if (matcher.matches()) {
                String codigoTurma = matcher.group(1);
                String codigoDisciplina = matcher.group(2);
                int trimestre = Integer.parseInt(matcher.group(3));

                System.out.printf("\nProcessando arquivo: %s\n", arquivo.getName());
                System.out.printf(" -> Turma: %s, Disciplina: %s, Trimestre: %d\n", codigoTurma, codigoDisciplina, trimestre);

                // --- Carrega o mapa de alunos para a turma ---
                Map<String, String> mapaAlunos = carregarMapaAlunosDaTurma(codigoTurma, DIRETORIO_DATA);
                if (mapaAlunos.isEmpty()) {
                    System.err.printf("AVISO: Não foi possível carregar os alunos para a turma %s. Verifique se o arquivo '%s.json' existe e não está vazio.\n", codigoTurma, codigoTurma);
                    // Continua, mas o RA não será preenchido
                } else {
                    System.out.println("--- DEBUG: Mapa de alunos carregado do JSON para a turma " + codigoTurma + " ---");
                    mapaAlunos.forEach((nomeNormalizado, ra) -> System.out.printf("  - %s -> %s%n", nomeNormalizado, ra));
                    System.out.println("---------------------------------------------------\n");
                }

                // Define o nome do arquivo de saída com o prefixo "notas_"
                String nomeArquivoSaida = "notas_" + codigoTurma + "_" + codigoDisciplina + "_T" + trimestre + ".json";

                try {
                    // Etapa 1: Processar o arquivo (importar CSV ou copiar JSON)
                    if (arquivo.getName().toLowerCase().endsWith(".csv")) {
                        ImportadorDeNotas.importarDeMoodleCSV(
                                arquivo.getAbsolutePath(), mapaAlunos, codigoTurma,
                                codigoDisciplina, trimestre, DIRETORIO_SAIDA_JSON, nomeArquivoSaida
                        );
                    } else if (arquivo.getName().toLowerCase().endsWith(".json")) {
                        // Para JSON, lê o conteúdo, converte os nomes para maiúsculas e salva no destino.
                        Path destino = Paths.get(DIRETORIO_SAIDA_JSON, nomeArquivoSaida);
                        processarArquivoJsonExistente(arquivo.toPath(), destino);
                        System.out.printf(" -> Arquivo JSON '%s' processado e salvo em: %s\n", arquivo.getName(), destino);
                    }

                    // Etapa 2: Mover o arquivo original para a pasta de processados
                    Path sourcePath = arquivo.toPath();
                    Path targetPath = Paths.get(DIRETORIO_PROCESSADOS, arquivo.getName());
                    Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    System.out.printf(" -> Arquivo movido para: %s\n", targetPath);
                    arquivosProcessados++;
                } catch (IOException e) {
                    System.err.println("Erro ao mover o arquivo processado: " + e.getMessage());
                }

            } else {
                System.out.printf("\nArquivo ignorado (nome fora do padrão): %s\n", arquivo.getName());
            }
        }

        System.out.printf("\nProcessamento finalizado. %d arquivo(s) processado(s).\n", arquivosProcessados);
    }

    /**
     * Carrega os alunos de um arquivo JSON de turma e cria um mapa Nome Normalizado -> RA.
     * @param codigoTurma O código da turma para carregar o arquivo de alunos.
     * @param dataDirectoryPath O caminho para o diretório 'data'.
     * @return Um mapa de nomes normalizados para RA, ou um mapa vazio se houver erro.
     */
    private static Map<String, String> carregarMapaAlunosDaTurma(String codigoTurma, String dataDirectoryPath) {
        Map<String, String> mapaAlunos = new HashMap<>();
        String caminhoArquivoAlunos = dataDirectoryPath + File.separator + "turmas" + File.separator + codigoTurma + ".json";
        Gson gson = new Gson();

        try (FileReader reader = new FileReader(caminhoArquivoAlunos)) {
            Type tipoListaAlunoDTO = new TypeToken<ArrayList<AlunoDTO>>(){}.getType();
            List<AlunoDTO> alunosDTO = gson.fromJson(reader, tipoListaAlunoDTO);

            if (alunosDTO != null) {
                for (AlunoDTO dto : alunosDTO) {
                    // Converte o nome para maiúsculas antes de normalizar e usar como chave.
                    String nomeEmCaixaAlta = dto.getNome().toUpperCase();
                    mapaAlunos.put(ImportadorDeNotas.normalizarNome(nomeEmCaixaAlta), dto.getRa());
                }
            }
        } catch (IOException e) {
            // Não imprime mais um erro, pois o aviso na função principal é mais informativo.
        }
        return mapaAlunos;
    }

    /**
     * Lê um arquivo JSON de notas, converte todos os nomes de alunos para caixa alta e salva em um novo local.
     * @param origem O caminho do arquivo JSON original.
     * @param destino O caminho onde o novo arquivo JSON será salvo.
     * @throws IOException Se ocorrer um erro de leitura ou escrita.
     */
    private static void processarArquivoJsonExistente(Path origem, Path destino) throws IOException {
        Gson gson = new Gson();
        Type tipoListaNotaDTO = new TypeToken<ArrayList<NotaDTO>>(){}.getType();

        // Lê o arquivo JSON original
        List<NotaDTO> notas;
        try (FileReader reader = new FileReader(origem.toFile())) {
            notas = gson.fromJson(reader, tipoListaNotaDTO);
        }

        // Converte os nomes para caixa alta
        if (notas != null) {
            notas.forEach(dto -> dto.setNome(dto.getNome().toUpperCase()));
        }

        // Salva o novo JSON com os nomes em caixa alta
        Files.writeString(destino, new GsonBuilder().setPrettyPrinting().create().toJson(notas), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
