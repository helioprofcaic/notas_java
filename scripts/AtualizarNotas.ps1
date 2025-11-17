<#
.SYNOPSIS
    Processa um bloco de texto com notas de alunos, extrai os dados e atualiza/cria o arquivo JSON correspondente.
.DESCRIPTION
    Este script lê a configuração de turmas e disciplinas, apresenta menus interativos para o usuário selecionar o contexto,
    e então pede para que o usuário cole um bloco de texto (copiado de uma plataforma externa).
    O script utiliza Regex para extrair RA, Nome, N1, N2 e N3 de cada aluno.
    Ele sempre substitui o arquivo de notas antigo, criando um backup com data e hora antes.
    Opcionalmente, replica as notas inseridas para os outros trimestres, seguindo a mesma lógica de backup e substituição.
.EXAMPLE
    PS > ./AtualizarNotas.ps1

    O script então guiará o usuário através dos menus de seleção.
#>

# --- CONFIGURAÇÃO ---
# Encontra os diretórios de forma dinâmica, baseado na localização do script.
# A variável $PSScriptRoot é automática no PowerShell e contém o diretório do script.
$DiretorioData = Join-Path -Path $PSScriptRoot -ChildPath "data"
$CaminhoArquivoTurmas = Join-Path -Path $DiretorioData -ChildPath "turmas-com-disciplinas.json"
$DiretorioSaidaJson = Join-Path -Path $DiretorioData -ChildPath "notas_json"


# Garante que o diretório de saída exista
if (-not (Test-Path $DiretorioSaidaJson)) {
    New-Item -Path $DiretorioSaidaJson -ItemType Directory | Out-Null
}

# --- CARREGAR DADOS PARA OS MENUS ---
if (-not (Test-Path $CaminhoArquivoTurmas)) {
    Write-Host "ERRO: Arquivo de configuração de turmas não encontrado em '$CaminhoArquivoTurmas'." -ForegroundColor Red
    exit
}
$Turmas = Get-Content -Path $CaminhoArquivoTurmas -Raw | ConvertFrom-Json

# --- INTERAÇÃO COM O USUÁRIO (MENUS) ---

Write-Host "--- Ferramenta de Atualização de Notas ---" -ForegroundColor Yellow

# 1. Menu de Seleção de Turma
Write-Host "`nSelecione a Turma:" -ForegroundColor Cyan
for ($i = 0; $i -lt $Turmas.Count; $i++) {
    Write-Host ("  [{0}] - {1} ({2})" -f ($i + 1), $Turmas[$i].nomeTurma, $Turmas[$i].codigoTurma)
}
$escolhaTurma = Read-Host "Escolha o número da turma"
$indiceTurma = [int]$escolhaTurma - 1

if ($indiceTurma -lt 0 -or $indiceTurma -ge $Turmas.Count) {
    Write-Host "ERRO: Seleção de turma inválida." -ForegroundColor Red
    exit
}
$turmaSelecionada = $Turmas[$indiceTurma]
$CodigoTurma = $turmaSelecionada.codigoTurma

# 2. Menu de Seleção de Disciplina
Write-Host "`nSelecione a Disciplina para a turma '$($turmaSelecionada.nomeTurma)':" -ForegroundColor Cyan
for ($i = 0; $i -lt $turmaSelecionada.disciplinas.Count; $i++) {
    Write-Host ("  [{0}] - {1}" -f ($i + 1), $turmaSelecionada.disciplinas[$i].nomeDisciplina)
}
$escolhaDisciplina = Read-Host "Escolha o número da disciplina"
$indiceDisciplina = [int]$escolhaDisciplina - 1

if ($indiceDisciplina -lt 0 -or $indiceDisciplina -ge $turmaSelecionada.disciplinas.Count) {
    Write-Host "ERRO: Seleção de disciplina inválida." -ForegroundColor Red
    exit
}
$disciplinaSelecionada = $turmaSelecionada.disciplinas[$indiceDisciplina]
$CodigoDisciplina = $disciplinaSelecionada.codigoDisciplina

# 3. Menu de Seleção de Trimestre
Write-Host "`nSelecione o Trimestre:" -ForegroundColor Cyan
Write-Host "  [1] - 1º Trimestre"
Write-Host "  [2] - 2º Trimestre"
Write-Host "  [3] - 3º Trimestre"
$Trimestre = Read-Host "Escolha o número do trimestre"

if ($Trimestre -notin @('1', '2', '3')) {
    Write-Host "ERRO: Seleção de trimestre inválida." -ForegroundColor Red
    exit
}

Write-Host "`n--- Resumo da Seleção ---"
Write-Host "Turma:      $($turmaSelecionada.nomeTurma)"
Write-Host "Disciplina: $($disciplinaSelecionada.nomeDisciplina)"
Write-Host "Trimestre:  $Trimestre"

Write-Host "`nCole o texto com as notas abaixo. Quando terminar, digite 'FIM' em uma nova linha e pressione Enter." -ForegroundColor Cyan

$entradaDeTextoArray = @()
while (($linha = Read-Host) -ne 'FIM') {
    $entradaDeTextoArray += $linha
}

# --- PROCESSAMENTO ---

# Junta todas as linhas em um único bloco de texto
$blocoDeTexto = $entradaDeTextoArray -join "`r`n"

# Regex para capturar os dados de um aluno em múltiplas linhas.
# Grupo 1: Nome do Aluno
# Grupo 2: RA
# Grupo 3: Nota 1
# Grupo 4: Nota 2
# Grupo 5: Nota 3
# A quarta linha de nota (média) é ignorada.
$regex = '(?m)^(.+?)\s*\r?\nCódigo RA:\s*(\d+)\s*\r?\n\s*\r?\n([\d\.,-]+)\s*\r?\n([\d\.,-]+)\s*\r?\n([\d\.,-]+)\s*\r?\n[\d\.,-]+'

$regexMatches = [regex]::Matches($blocoDeTexto, $regex)

$novasNotas = @{}

foreach ($match in $regexMatches) {
    $ra = $match.Groups[2].Value.Trim()

    # Cria um objeto para o aluno com os dados extraídos
    $alunoNota = [PSCustomObject]@{
        nome        = $match.Groups[1].Value.Trim().ToUpper() # Converte nome para maiúsculas
        ra          = $ra
        nm1         = $match.Groups[3].Value.Trim().Replace(',', '.')
        nm2         = $match.Groups[4].Value.Trim().Replace(',', '.')
        nm3         = $match.Groups[5].Value.Trim().Replace(',', '.')
        recuperacao = "0" # Define a recuperação como "0" por padrão
        totalFaltas = "" # Campo existe no DTO, então o adicionamos
    }

    # Adiciona ao dicionário usando o RA como chave para fácil acesso
    $novasNotas[$ra] = $alunoNota
}

if ($novasNotas.Count -eq 0) {
    Write-Host "AVISO: Nenhum dado de aluno válido foi encontrado no texto colado. Verifique o formato." -ForegroundColor Yellow
    exit
}

Write-Host ("`n{0} registros de alunos foram processados do texto." -f $novasNotas.Count) -ForegroundColor Green

# --- GERAÇÃO/ATUALIZAÇÃO DO JSON ---

Write-Host "`nModo 'Substituir' ativado. O arquivo de notas será reescrito com os alunos da lista colada." -ForegroundColor Yellow

$NomeArquivoJson = "notas_${CodigoTurma}_${CodigoDisciplina}_T${Trimestre}.json"
$CaminhoCompleto = Join-Path -Path $DiretorioSaidaJson -ChildPath $NomeArquivoJson

$notasParaSalvar = $novasNotas.Values | Sort-Object -Property nome
$sucessoSalvar = $false

# --- Lógica de Backup ---
$DiretorioBackup = Join-Path -Path $DiretorioSaidaJson -ChildPath "backup"
if (-not (Test-Path $DiretorioBackup)) {
    New-Item -Path $DiretorioBackup -ItemType Directory | Out-Null
}

if (Test-Path $CaminhoCompleto) {
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $NomeArquivoBackup = $NomeArquivoJson.Replace(".json", "_backup_$timestamp.json")
    $CaminhoBackup = Join-Path -Path $DiretorioBackup -ChildPath $NomeArquivoBackup

    try {
        Move-Item -Path $CaminhoCompleto -Destination $CaminhoBackup
        Write-Host "Backup do arquivo anterior salvo em: $CaminhoBackup" -ForegroundColor Cyan
    } catch {
        Write-Host "ERRO: Falha ao criar backup do arquivo anterior. A operação foi cancelada. Detalhes: $_" -ForegroundColor Red
        exit
    }
}

# --- Salva o novo arquivo ---
try {
    $notasParaSalvar | ConvertTo-Json -Depth 5 | Set-Content -Path $CaminhoCompleto -Encoding UTF8
    Write-Host ("`nSUCESSO: Arquivo '{0}' foi salvo com {1} registros." -f $NomeArquivoJson, $notasParaSalvar.Count) -ForegroundColor Green
    $sucessoSalvar = $true
}
catch {
    Write-Host "ERRO: Falha ao salvar o novo arquivo JSON. Detalhes: $_" -ForegroundColor Red
}


# --- REPLICAÇÃO OPCIONAL PARA OUTROS TRIMESTRES ---

if ($sucessoSalvar) {
    $replicar = Read-Host "`nDeseja replicar as NOTAS inseridas para os outros trimestres? (s/n)"
    if ($replicar -eq 's') {

        # Itera sobre os trimestres que NÃO foram o selecionado
        foreach ($trimestreAlvo in @('1', '2', '3') | Where-Object { $_ -ne $Trimestre }) {
            $NomeArquivoAlvo = "notas_${CodigoTurma}_${CodigoDisciplina}_T${trimestreAlvo}.json"
            $CaminhoAlvo = Join-Path -Path $DiretorioSaidaJson -ChildPath $NomeArquivoAlvo

            $notasAlvo = @()
            $notasAlvoAtualizadas = @() # Nova lista para construir o resultado

            # Carrega o arquivo do trimestre alvo, se ele existir
            if (Test-Path $CaminhoAlvo) {
                $notasAlvo = Get-Content -Path $CaminhoAlvo -Raw | ConvertFrom-Json
            } else {
                Write-Host "INFO: Arquivo para o T${trimestreAlvo} não existe. Ele será criado com as notas inseridas." -ForegroundColor Cyan
                # Se o arquivo não existe, a lista de notas alvo é a própria lista de notas inseridas
                $notasAlvo = $notasParaSalvar
            }

            # Itera sobre as notas do arquivo de destino para decidir se mantém ou atualiza
            foreach ($notaExistente in $notasAlvo) {
                # Verifica se a nota existente deve ser atualizada com uma nota recém-colada
                if ($notasParaSalvar.Where({$_.ra -eq $notaExistente.ra}).Count -gt 0) {
                    # Encontra a nota correspondente na lista de notas inseridas
                    $notaAtualizada = $notasParaSalvar.Where({$_.ra -eq $notaExistente.ra})[0]
                    $notasAlvoAtualizadas += $notaAtualizada
                } else {
                    # Mantém a nota antiga se o aluno não estava na lista colada
                    $notasAlvoAtualizadas += $notaExistente
                }
            }

            # --- Lógica de Backup para replicação ---
            if (Test-Path $CaminhoAlvo) {
                $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
                $NomeBackupAlvo = $NomeArquivoAlvo.Replace(".json", "_backup_$timestamp.json")
                $CaminhoBackupAlvo = Join-Path -Path $DiretorioBackup -ChildPath $NomeBackupAlvo
                Move-Item -Path $CaminhoAlvo -Destination $CaminhoBackupAlvo -ErrorAction SilentlyContinue
            }

            try {
                $notasAlvoAtualizadas | Sort-Object -Property nome | ConvertTo-Json -Depth 5 | Set-Content -Path $CaminhoAlvo -Encoding UTF8
                Write-Host ("SUCESSO: As notas foram replicadas para o arquivo '{0}'." -f $NomeArquivoAlvo) -ForegroundColor Green
            } catch {
                Write-Host ("ERRO: Falha ao replicar dados para o arquivo '{0}'. Detalhes: $_" -f $NomeArquivoAlvo) -ForegroundColor Red
            }
        }
    }
}
