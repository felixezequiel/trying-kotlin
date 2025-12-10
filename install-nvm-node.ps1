# Script para instalar NVM for Windows e Node.js LTS
# Execute como Administrador

$ErrorActionPreference = "Stop"

Write-Host "=== Instalando NVM for Windows ===" -ForegroundColor Cyan

# Verificar se ja tem NVM instalado
$nvmPath = "$env:APPDATA\nvm"
if (Test-Path $nvmPath) {
    Write-Host "NVM ja esta instalado em $nvmPath" -ForegroundColor Yellow
}
else {
    # Baixar e instalar NVM for Windows
    $nvmVersion = "1.1.12"
    $nvmInstaller = "$env:TEMP\nvm-setup.exe"
    $nvmUrl = "https://github.com/coreybutler/nvm-windows/releases/download/$nvmVersion/nvm-setup.exe"

    Write-Host "Baixando NVM for Windows v$nvmVersion..." -ForegroundColor Green
    Invoke-WebRequest -Uri $nvmUrl -OutFile $nvmInstaller -UseBasicParsing

    Write-Host "Executando instalador..." -ForegroundColor Green
    Write-Host "IMPORTANTE: Siga o instalador e aceite os caminhos padrao." -ForegroundColor Yellow
    Start-Process -FilePath $nvmInstaller -Wait

    # Limpar arquivo temporario
    Remove-Item $nvmInstaller -Force -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "=== Instalacao do NVM concluida ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "PROXIMOS PASSOS:" -ForegroundColor Yellow
Write-Host "1. Feche e reabra o PowerShell/Terminal" -ForegroundColor White
Write-Host "2. Execute: nvm install lts" -ForegroundColor White
Write-Host "3. Execute: nvm use lts" -ForegroundColor White
Write-Host ""
Write-Host "Ou execute o script install-node-lts.ps1 apos reabrir o terminal." -ForegroundColor Green
