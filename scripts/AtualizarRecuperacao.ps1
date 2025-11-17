<#
.SYNOPSIS
    Atualiza APENAS o campo de nota de recuperação dos alunos em um arquivo JSON existente.
.DESCRIPTION
    Este script apresenta menus para selecionar a turma, disciplina e trimestre.
    Ele então pede ao usuário para colar um bloco de texto contendo os nomes dos alunos, RAs e suas notas de recuperação.
    O script extrai o RA e a primeira nota de cada aluno, encontra o arquivo JSON correspondente,
    e atualiza o campo "recuperacao" para cada aluno encontrado.
    Este script NÃO cria novos arquivos nem adiciona novos alunos; ele apenas atualiza registros existentes.
.EXAMPLE
    PS > ./AtualizarRecuperacao.ps1
    
    O script então guiará o usuário através dos menus de seleção.
#>

# --- CONFIGURAÇÃO ---
# Encontra os diretórios de forma dinâmica, baseado na localização do script.
# A variável $PSScriptRoot é automática no PowerShell e contém o diretório do script.
$DiretorioData = Join-Path -Path $PSScriptRoot -ChildPath "data"
$CaminhoArquivoTurmas = Join-Path -Path $DiretorioData -ChildPath "turmas-com-disciplinas.json"
$DiretorioSaidaJson = Join-Path -Path $DiretorioData -ChildPath "notas_json"

# --- CARREGAR DADOS PARA OS MENUS ---
if (-not (Test-Path $CaminhoArquivoTurmas)) {
    Write-Host "ERRO: Arquivo de configuração de turmas não encontrado em '$CaminhoArquivoTurmas'." -ForegroundColor Red
    exit
}
$Turmas = Get-Content -Path $CaminhoArquivoTurmas -Raw | ConvertFrom-Json

# --- INTERAÇÃO COM O USUÁRIO (MENUS) ---

Write-Host "--- Ferramenta de Atualização de Notas de RECUPERAÇÃO ---" -ForegroundColor Yellow

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
Write-Host "`nSelecione o Trimestre da recuperação:" -ForegroundColor Cyan
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

Write-Host "`nCole o texto com as notas de recuperação abaixo. Quando terminar, digite 'FIM' em uma nova linha e pressione Enter." -ForegroundColor Cyan

$entradaDeTextoArray = @()
while (($linha = Read-Host) -ne 'FIM') {
    $entradaDeTextoArray += $linha
}

# --- PROCESSAMENTO DO TEXTO ---

$blocoDeTexto = $entradaDeTextoArray -join "`r`n"

# Regex para capturar o nome, RA e a PRIMEIRA nota que aparece após a linha em branco.
$regex = '(?m)^(.+?)\s*\r?\nCódigo RA:\s*(\d+)\s*\r?\n\s*\r?\n([\d\.,]+)'

$regexMatches = [regex]::Matches($blocoDeTexto, $regex)

$notasRecuperacao = @{}

foreach ($match in $regexMatches) {
    $ra = $match.Groups[2].Value.Trim()
    $nota = $match.Groups[3].Value.Trim().Replace(',', '.')
    $notasRecuperacao[$ra] = $nota
}

if ($notasRecuperacao.Count -eq 0) {
    Write-Host "AVISO: Nenhuma nota de recuperação válida foi encontrada no texto colado. Verifique o formato." -ForegroundColor Yellow
    exit
}

Write-Host ("`n{0} notas de recuperação foram processadas do texto." -f $notasRecuperacao.Count) -ForegroundColor Green

# --- ATUALIZAÇÃO DO JSON ---

$NomeArquivoJson = "notas_${CodigoTurma}_${CodigoDisciplina}_T${Trimestre}.json"
$CaminhoCompleto = Join-Path -Path $DiretorioSaidaJson -ChildPath $NomeArquivoJson

if (-not (Test-Path $CaminhoCompleto)) {
    Write-Host "ERRO: O arquivo de notas '$NomeArquivoJson' não foi encontrado. Este script serve apenas para ATUALIZAR um arquivo existente." -ForegroundColor Red
    exit
}

$notasDoArquivo = Get-Content -Path $CaminhoCompleto -Raw | ConvertFrom-Json
$alunosAtualizados = 0

# Itera sobre cada aluno no arquivo JSON
foreach ($aluno in $notasDoArquivo) {
    # Verifica se o RA do aluno está na lista de notas de recuperação que foram coladas
    if ($notasRecuperacao.ContainsKey($aluno.ra)) {
        # Se estiver, atualiza o campo 'recuperacao'
        $aluno.recuperacao = $notasRecuperacao[$aluno.ra]
        $alunosAtualizados++
    }
}

# Salva o arquivo JSON com os dados atualizados
try {
    # A lista já está ordenada, então não precisamos reordenar
    $notasDoArquivo | ConvertTo-Json -Depth 5 | Set-Content -Path $CaminhoCompleto -Encoding UTF8
    
    if ($alunosAtualizados -gt 0) {
        Write-Host ("`nSUCESSO: Arquivo '{0}' foi atualizado com {1} nota(s) de recuperação." -f $NomeArquivoJson, $alunosAtualizados) -ForegroundColor Green
    } else {
        Write-Host "`nINFO: Nenhuma nota foi atualizada. Os RAs do texto colado não foram encontrados no arquivo de notas." -ForegroundColor Yellow
    }
}
catch {
    Write-Host "ERRO: Falha ao salvar o arquivo JSON. Detalhes: $_" -ForegroundColor Red
}