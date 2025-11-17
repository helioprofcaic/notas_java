<#
.SYNOPSIS
    Sincroniza os arquivos de notas de uma turma, removendo alunos que não estão mais matriculados.
.DESCRIPTION
    Este script de manutenção garante a integridade dos dados. Ele solicita que o usuário escolha uma turma
    e, em seguida, escolha um ARQUIVO DE NOTAS para ser usado como a "fonte da verdade" (lista mestra).

    O script então sincroniza todos os outros arquivos de notas da turma e também a lista principal de alunos
    (ex: 297135.json), removendo quaisquer "alunos fantasmas" que não estejam na lista mestra.
    Um backup de cada arquivo modificado é criado antes de qualquer alteração.
.EXAMPLE
    PS > ./SincronizarAlunos.ps1

    O script então guiará o usuário através dos menus de seleção.
#>

# --- CONFIGURAÇÃO ---
# Encontra os diretórios de forma dinâmica, baseado na localização do script.
# A variável $PSScriptRoot é automática no PowerShell e contém o diretório do script.
$DiretorioData = Join-Path -Path $PSScriptRoot -ChildPath "data"
$CaminhoArquivoTurmas = Join-Path -Path $DiretorioData -ChildPath "turmas-com-disciplinas.json"
$DiretorioNotas = Join-Path -Path $DiretorioData -ChildPath "notas_json"
$DiretorioBackup = Join-Path -Path $DiretorioData -ChildPath "backup" # Movido para a pasta 'data'

# Garante que o diretório de backup exista
if (-not (Test-Path $DiretorioBackup)) {
    New-Item -Path $DiretorioBackup -ItemType Directory | Out-Null
}

# --- CARREGAR DADOS PARA O MENU ---
if (-not (Test-Path $CaminhoArquivoTurmas)) {
    Write-Host "ERRO: Arquivo de configuração de turmas não encontrado em '$CaminhoArquivoTurmas'." -ForegroundColor Red
    exit
}
$Turmas = Get-Content -Path $CaminhoArquivoTurmas -Raw | ConvertFrom-Json -ErrorAction Stop

# --- INTERAÇÃO COM O USUÁRIO ---

Write-Host "--- Ferramenta de Sincronização de Alunos ---" -ForegroundColor Yellow
Write-Host "Esta ferramenta usa um arquivo de notas como base para remover 'alunos fantasmas' de uma turma."

# 1. Menu de Seleção de Turma
Write-Host "`nSelecione a Turma que deseja sincronizar:" -ForegroundColor Cyan
for ($i = 0; $i -lt $Turmas.Count; $i++) {
    Write-Host ("  [{0}] - {1} ({2})" -f ($i + 1), $Turmas[$i].nomeTurma, $Turmas[$i].codigoTurma)
}
$escolhaTurma = Read-Host "Escolha o número da turma" | ForEach-Object { [int]$_ - 1 }

if ($escolhaTurma -lt 0 -or $escolhaTurma -ge $Turmas.Count) {
    Write-Host "ERRO: Seleção de turma inválida." -ForegroundColor Red
    exit
}
$turmaSelecionada = $Turmas[$escolhaTurma]
$CodigoTurma = $turmaSelecionada.codigoTurma

# 2. Menu de Seleção do Arquivo Mestre
$arquivosDeNotas = Get-ChildItem -Path $DiretorioNotas -Filter "notas_${CodigoTurma}_*.json"
if ($arquivosDeNotas.Count -eq 0) {
    Write-Host "Nenhum arquivo de notas encontrado para esta turma. Nenhuma ação necessária." -ForegroundColor Yellow
    exit
}

Write-Host "`nSelecione o arquivo de notas que será usado como LISTA MESTRA:" -ForegroundColor Cyan
for ($i = 0; $i -lt $arquivosDeNotas.Count; $i++) {
    Write-Host ("  [{0}] - {1}" -f ($i + 1), $arquivosDeNotas[$i].Name)
}
$escolhaArquivoMestre = Read-Host "Escolha o número do arquivo mestre" | ForEach-Object { [int]$_ - 1 }

if ($escolhaArquivoMestre -lt 0 -or $escolhaArquivoMestre -ge $arquivosDeNotas.Count) {
    Write-Host "ERRO: Seleção de arquivo inválida." -ForegroundColor Red
    exit
}
$arquivoMestre = $arquivosDeNotas[$escolhaArquivoMestre]

Write-Host "`nUsando '$($arquivoMestre.Name)' como a fonte da verdade para os alunos matriculados." -ForegroundColor Green

# 3. Carregar a lista oficial de alunos do arquivo mestre
$listaMestra = Get-Content -Path $arquivoMestre.FullName -Raw | ConvertFrom-Json
$rasOficiais = @{}
$listaMestra.ForEach({ $rasOficiais[$_.ra] = $true })

Write-Host "Lista mestra com $($rasOficiais.Count) alunos carregada."

# 4. Sincronizar a lista principal de alunos da turma (ex: 297135.json)
$caminhoListaPrincipal = Join-Path -Path $DiretorioData -ChildPath "$($CodigoTurma).json"
$listaPrincipalNova = $listaMestra | ForEach-Object { [PSCustomObject]@{ nome = $_.nome; ra = $_.ra } } | Sort-Object -Property nome

if (Test-Path $caminhoListaPrincipal) {
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $nomeBackup = "${CodigoTurma}_sync-backup_$timestamp.json"
    $caminhoBackup = Join-Path -Path $DiretorioBackup -ChildPath $nomeBackup
    Move-Item -Path $caminhoListaPrincipal -Destination $caminhoBackup -ErrorAction SilentlyContinue
    Write-Host "`nBackup da lista de alunos principal salvo em: $caminhoBackup" -ForegroundColor Cyan
}

$listaPrincipalNova | ConvertTo-Json -Depth 5 | Set-Content -Path $caminhoListaPrincipal -Encoding UTF8
Write-Host "SUCESSO: A lista principal de alunos da turma foi sincronizada." -ForegroundColor Green

# 5. Iterar sobre TODOS os arquivos de notas (incluindo o mestre, para garantir a ordenação) e sincronizar
Write-Host "`nIniciando sincronização dos arquivos de notas..."
foreach ($arquivo in $arquivosDeNotas) {
    Write-Host "Verificando arquivo: $($arquivo.Name)..." -ForegroundColor Cyan
    $notasAtuais = Get-Content -Path $arquivo.FullName -Raw | ConvertFrom-Json

    # Filtra a lista, mantendo apenas os alunos cujo RA está na lista oficial
    $notasSincronizadas = $notasAtuais | Where-Object { $rasOficiais.ContainsKey($_.ra) }

    $contagemOriginal = $notasAtuais.Count
    $contagemSincronizada = $notasSincronizadas.Count

    if ($contagemOriginal -eq $contagemSincronizada) {
        Write-Host "  -> Nenhum aluno fantasma encontrado. O arquivo já está sincronizado."
        # Mesmo que não haja remoções, vamos salvar para garantir a ordenação
    }

    $alunosRemovidos = $contagemOriginal - $contagemSincronizada
    if ($alunosRemovidos -gt 0) {
        Write-Host "  -> ATENÇÃO: $alunosRemovidos aluno(s) fantasma(s) serão removidos." -ForegroundColor Yellow
    }

    # --- Lógica de Backup ---
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $nomeBackup = $arquivo.Name.Replace(".json", "_sync-backup_$timestamp.json")
    $caminhoBackup = Join-Path -Path ($DiretorioNotas + "/backup") -ChildPath $nomeBackup

    try {
        # Cria o diretório de backup de notas se não existir
        if (-not (Test-Path ($DiretorioNotas + "/backup"))) {
            New-Item -Path ($DiretorioNotas + "/backup") -ItemType Directory | Out-Null
        }

        Move-Item -Path $arquivo.FullName -Destination $caminhoBackup
        Write-Host "  -> Backup do arquivo de notas salvo em: $caminhoBackup"

        # Salva o novo arquivo sincronizado e ordenado
        $notasSincronizadas | Sort-Object -Property nome | ConvertTo-Json -Depth 5 | Set-Content -Path $arquivo.FullName -Encoding UTF8
        Write-Host "  -> SUCESSO: Arquivo sincronizado salvo com $contagemSincronizada alunos." -ForegroundColor Green

    } catch {
        Write-Host "  -> ERRO: Falha ao processar o arquivo $($arquivo.Name). Detalhes: $_" -ForegroundColor Red
    }
}

Write-Host "`nProcesso de sincronização finalizado." -ForegroundColor Green


